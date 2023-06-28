package it.uniupo.oggettiusati.fragment

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.Priority
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment(private val isAdmin: Boolean) : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    private var myAnnunciHome = HashMap<String, Annuncio>()
    private var myListenerAnnunciHome: MutableList<ListenerRegistration> = mutableListOf()

    //--- Variabili utili per filtrare gli annunci ---
    private var titoloAnnuncio: String? = null
    private var disponibilitaSpedire: Boolean? = null
    private var prezzoSuperiore: Int? = null
    private var prezzoInferiore: Int? = null

    val userId = auth.currentUser!!.uid

    var distanceSlider :Slider? = null
    var testoDistanza :TextView? = null
    var testoPrezzo :TextView? = null
    private lateinit var radioGroupPrezzo :RadioGroup
    private var priceSlider :RangeSlider? = null
    var prezzoMin :Slider? = null
    var prezzoMax :Slider? = null
    private var shippingSwitch : SwitchCompat? = null

    private val LOCATION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentRootView = inflater.inflate(R.layout.fragment_home, container, false)

        lateinit var username: String
        val userRef = database.collection(UserLoginActivity.Utente.nomeCollection).document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                username = document.get("nome").toString()
            } else {
                Log.w("document error", "Error: document is null")
            }

            Toast.makeText(activity, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        return fragmentRootView

//        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        distanceSlider = view?.findViewById(R.id.distanceSlider)
        testoDistanza = view?.findViewById(R.id.maxDistance)
        testoPrezzo = view?.findViewById(R.id.priceRange)
        radioGroupPrezzo = view?.findViewById(R.id.rGroup_prezzo)!!
        priceSlider = view?.findViewById(R.id.price_range_slider)
        prezzoMin = view?.findViewById(R.id.price_min_slider)
        prezzoMax = view?.findViewById(R.id.price_max_slider)
        shippingSwitch = view?.findViewById(R.id.shipping_switch)

        runBlocking {

            //Recupero tutti gli annunci, preferiti, per la notifica.
            FavoritesFragment.recuperaAnnunciPreferitiFirebaseFirestore(
                auth.uid!!,
                requireActivity()
            )

            val myDocumentiRef: Set<DocumentSnapshot> = if(isAdmin)
                UserLoginActivity.recuperaAnnunciFiltratiPossibileRichiesta(null, null, null, null, null, null)
            else
                UserLoginActivity.recuperaAnnunciFiltrati(null, null, null, null, null, null)


            myAnnunciHome = UserLoginActivity.recuperaAnnunci(myDocumentiRef)

            //-- Definisco i nuovi listener, per i documenti che ho ora nella Home --
            myListenerAnnunciHome = subscribeRealTimeDatabase(myDocumentiRef,myListenerAnnunciHome,myAnnunciHome)

            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)
            //this will pass the ArrayList to our Adapter
            requireView().findViewById<TextView>(R.id.info_home).text = if(myAnnunciHome.size > 0) "${myAnnunciHome.size} oggetti " else "Non sono presenti oggetti nel sistema"
            val adapter = CustomAdapter(myAnnunciHome, R.layout.card_view_design, isAdmin)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

        distanceSlider?.isEnabled = false
        priceSlider?.isEnabled = false
        prezzoMin?.isEnabled = false
        prezzoMax?.isEnabled = false

        var updTxt = "Distanza max: ${distanceSlider?.value}km"
        testoDistanza?.text = updTxt

        setEnabledRadioGroup(radioGroupPrezzo, false)

        val selezionePrezzo = requireView().findViewById<CheckBox>(R.id.select_price)

        updTxt = "Fascia di prezzo: ${priceSlider!!.values[0]}€ - ${priceSlider!!.values[1]}€"
        testoPrezzo?.text = updTxt

        prezzoMin?.setLabelFormatter { value -> "$value €"; }

        prezzoMin?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Prezzo min: ${prezzoMin!!.value}€"
                testoPrezzo?.text = updTxt
            }
        })

        prezzoMax?.setLabelFormatter { value -> "$value €"; }

        prezzoMax?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Prezzo max: ${prezzoMax!!.value}€"
                testoPrezzo?.text = updTxt
            }
        })

        selezionePrezzo.setOnClickListener {
            togglePrezzo(radioGroupPrezzo, priceSlider, prezzoMin, prezzoMax, selezionePrezzo.isChecked)
            testoPrezzo?.isEnabled = selezionePrezzo.isChecked
        }

        val idPrezzoRange = R.id.prezzo_range
        val idPrezzoMin = R.id.prezzo_min
        val idPrezzoMax = R.id.prezzo_max
        val filtraPrezzoRange = requireView().findViewById<RadioButton>(idPrezzoRange)
        val filtraPrezzoMin = requireView().findViewById<RadioButton>(idPrezzoMin)
        val filtraPrezzoMax = requireView().findViewById<RadioButton>(idPrezzoMax)
        filtraPrezzoRange.isChecked = true

        filtraPrezzoRange.setOnClickListener {
            enableFirstSliderPrezzo(priceSlider, prezzoMin, prezzoMax)
            updTxt = "Fascia di prezzo: ${priceSlider!!.values[0]}€ - ${priceSlider!!.values[1]}€"
            testoPrezzo?.text = updTxt
        }
        filtraPrezzoMin.setOnClickListener {
            enableFirstSliderPrezzo(prezzoMin, priceSlider, prezzoMax)
            updTxt = "Prezzo min: ${prezzoMin?.value}€"
            testoPrezzo?.text = updTxt
        }
        filtraPrezzoMax.setOnClickListener {
            enableFirstSliderPrezzo(prezzoMax, priceSlider, prezzoMin)
            updTxt = "Prezzo max: ${prezzoMax?.value}€"
            testoPrezzo?.text = updTxt
        }

        //---

        val buttonRicerca = view?.findViewById<ImageButton>(R.id.searchButton)
        val casellaRicerca = view?.findViewById<EditText>(R.id.search)
        val selezionaDistanza = view?.findViewById<CheckBox>(R.id.select_distance)

        val selezionaSpedizione = view?.findViewById<CheckBox>(R.id.select_shipping)

        shippingSwitch?.isChecked = false

        listOf(
            selezionaDistanza,
            selezionaSpedizione,
            selezionePrezzo
        ).forEach {
            it?.isChecked = false
        }
        selezionaDistanza?.setOnClickListener {
            val permission = ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE)
            }
            distanceSlider?.isEnabled = selezionaDistanza.isChecked
            testoDistanza?.isEnabled = selezionaDistanza.isChecked
        }

        selezionaSpedizione?.setOnClickListener {
            shippingSwitch?.isEnabled = selezionaSpedizione.isChecked
        }

        buttonRicerca?.setOnClickListener {

            var recuperaTitolo: String? = casellaRicerca?.text.toString()

            if(recuperaTitolo!!.isEmpty())
                recuperaTitolo = null

            if(selezionePrezzo.isChecked) {
                when (radioGroupPrezzo.checkedRadioButtonId) {
                    idPrezzoRange -> recuperaAnnunciPrezzoRange(priceSlider!!.values[1].toInt(), priceSlider!!.values[0].toInt())
                    idPrezzoMin -> recuperaAnnunciPrezzoSuperiore(prezzoMin?.value?.toInt()!!)
                    idPrezzoMax -> recuperaAnnunciPrezzoInferiore(prezzoMax?.value?.toInt()!!)
                }
            }
            else
                recuperaAnnunciPrezzoRange(null, null)

            if(selezionaSpedizione!!.isChecked)
                recuperaAnnunciDisponibilitaSpedire(shippingSwitch?.isChecked!!)
            else
                recuperaAnnunciDisponibilitaSpedire(null)

            runBlocking {

                //-- posizione utente ---
                val posizioneUtente = Location("provider")
                MainScope().launch {

                if (selezionaDistanza!!.isChecked) {
                        withContext(Dispatchers.Main){
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

                            val lastUserLocation = fusedLocationClient.lastLocation.await()
                            if(lastUserLocation != null) {
                                posizioneUtente.latitude = lastUserLocation.latitude //44.922
                                posizioneUtente.longitude = lastUserLocation.longitude //8.617
                            } else {
                                posizioneUtente.latitude = 44.922
                                posizioneUtente.longitude = 8.617
                            }


                            val currentUserLocation = fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                                override fun onCanceledRequested(listener: OnTokenCanceledListener): CancellationToken {
                                    return CancellationTokenSource().token
                                }

                                override fun isCancellationRequested(): Boolean {
                                    return false
                                }
                            }).await()

                            if(lastUserLocation != null) {
                                posizioneUtente.latitude = currentUserLocation.latitude //44.922
                                posizioneUtente.longitude = currentUserLocation.longitude //8.617
                            } else {
                                posizioneUtente.latitude = 44.922
                                posizioneUtente.longitude = 8.617
                            }

                            //Toast.makeText(activity, "${posizioneUtente.latitude} ${posizioneUtente.longitude}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val myDocumentiRef: Set<DocumentSnapshot> = if (selezionaDistanza.isChecked)
                        if(isAdmin)
                            UserLoginActivity.recuperaAnnunciFiltratiPossibileRichiesta(recuperaTitolo, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, posizioneUtente, distanceSlider?.value?.toInt()!!)
                        else
                            UserLoginActivity.recuperaAnnunciFiltrati(recuperaTitolo, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, posizioneUtente, distanceSlider?.value?.toInt()!!)
                    else
                        if(isAdmin)
                            UserLoginActivity.recuperaAnnunciFiltratiPossibileRichiesta(recuperaTitolo, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore,null, null)
                        else
                            UserLoginActivity.recuperaAnnunciFiltrati(recuperaTitolo, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore,null, null)

                    myAnnunciHome = UserLoginActivity.recuperaAnnunci(myDocumentiRef)

                    //-- Definisco i nuovi listener, per i documenti che ho ora nella Home --
                    myListenerAnnunciHome = subscribeRealTimeDatabase(myDocumentiRef,myListenerAnnunciHome,myAnnunciHome)
                    requireView().findViewById<TextView>(R.id.info_home).text = if(myAnnunciHome.size > 0) "${myAnnunciHome.size} risultati di ricerca " else "Nessun oggetto corrisponde ai parametri"
                    val adapterRicerca = CustomAdapter(myAnnunciHome, R.layout.card_view_design, isAdmin)
                    val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
                    //setting the Adapter with the recyclerView
                    recyclerVu?.adapter = adapterRicerca

                }
            }
        }

        distanceSlider?.setLabelFormatter { value -> "$value km"; }

        distanceSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //...
            }

            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Distanza max: ${distanceSlider!!.value}km"
                testoDistanza?.text = updTxt
            }
        })

        priceSlider!!.setLabelFormatter { value -> "${value.toInt()} €"; }

        priceSlider!!.addOnChangeListener { _, _, _ ->
            updTxt = "Fascia di prezzo: ${priceSlider!!.values[0]}€ - ${priceSlider!!.values[1]}€"
            testoPrezzo?.text = updTxt
        }

        val filterButton = view?.findViewById<ImageButton>(R.id.filters)

        filterButton?.setOnClickListener {
            val filterLay = view?.findViewById<LinearLayout>(R.id.filterElements)
            if(filterLay?.isVisible == false) {
                filterLay.visibility = View.VISIBLE
            } else {
                if (filterLay != null) { filterLay.visibility = View.GONE } else { Log.w("d-filter", "filterLay e' null") }
            }
        }

        //salva ricerca
        val btnSalvaRicerca = activity?.findViewById<Button>(R.id.salva_ricerca)

        btnSalvaRicerca?.setOnClickListener {
            runBlocking {

                val recuperaTitolo = casellaRicerca?.text.toString()

                if(recuperaTitolo.isEmpty())
                    titoloAnnuncio = null

                val distMax: Int? = if (selezionaDistanza!!.isChecked)
                    distanceSlider?.value?.toInt()
                else
                    null

                if(selezionePrezzo.isChecked) {
                    when (radioGroupPrezzo.checkedRadioButtonId) {
                        idPrezzoRange -> {
                            prezzoSuperiore = priceSlider!!.values[1].toInt()
                            prezzoInferiore =  priceSlider!!.values [0].toInt()
                        }
                        idPrezzoMin -> {
                            prezzoSuperiore = prezzoMin?.value?.toInt()!!
                            prezzoInferiore = null
                        }
                        idPrezzoMax -> {
                            prezzoSuperiore = null
                            prezzoInferiore = prezzoMax?.value?.toInt()
                        }
                    }
                }
                else {
                    prezzoSuperiore = null
                    prezzoInferiore = null
                }

                disponibilitaSpedire = if(selezionaSpedizione!!.isChecked)
                    shippingSwitch?.isChecked
                else
                    null

                val posizioneUtente = Location("provider")
                posizioneUtente.latitude = 44.922
                posizioneUtente.longitude = 8.617

                inserisciRicercaSuFirebaseFirestore(auth.uid!!, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, distMax, posizioneUtente)
            }
        }

        activity?.findViewById<Button>(R.id.reset_ricerca)?.setOnClickListener {
            casellaRicerca?.setText("")
            selezionaDistanza?.isChecked = false
            selezionePrezzo?.isChecked = false
            selezionaSpedizione?.isChecked = false
            disabilitaTuttiIFiltri()
        }


    }

    private fun disabilitaTuttiIFiltri() {
        distanceSlider?.isEnabled = false
        testoDistanza?.isEnabled = false
        togglePrezzo(radioGroupPrezzo, priceSlider, prezzoMin, prezzoMax, false)
        testoPrezzo?.isEnabled = false
        shippingSwitch?.isEnabled = false
    }

    private fun enableFirstSliderPrezzo(firstSlider: View?, secondSlider: View?, thirdSlider: View?) {
        firstSlider?.visibility = View.VISIBLE
        listOf(secondSlider, thirdSlider).forEach {
            it?.visibility = View.GONE
        }
    }

    private fun togglePrezzo(radioGroupPrezzo: RadioGroup, rangeSliderPrezzo: View?, sliderMinPrezzo: View?, sliderMaxPrezzo: View?, enabled :Boolean) {
        setEnabledRadioGroup(radioGroupPrezzo, enabled)
        listOf(rangeSliderPrezzo, sliderMinPrezzo, sliderMaxPrezzo).forEach {
            it?.isEnabled = enabled
        }
    }

    private fun setEnabledRadioGroup(radioGroup: RadioGroup, enabled: Boolean) {
        for (i in 0 until radioGroup.childCount) {
            (radioGroup.getChildAt(i)).isEnabled = enabled
        }
    }

    private fun requestPermission(permissioType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permissioType), requestCode)
    }

    /**
     * Imposta il limite inferiore di prezzo per il recupero degli annunci
     *
     * @author Amato Luca
     * @param prezzoMinore prezzo inferiore desiderato
     */
    fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int){

        this.prezzoInferiore = prezzoMinore
        this.prezzoSuperiore = null
    }

    /**
     * Imposta il limite superiore di prezzo per il recupero degli annunci
     *
     * @author Amato Luca
     * @param prezzoSuperiore Prezzo superiore desiderato
     */
    fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int) {

        this.prezzoInferiore = null
        this.prezzoSuperiore = prezzoSuperiore
    }

    /**
     * Imposta i limiti di prezzo per il recupero degli annunci
     *
     * @author Amato Luca
     * @param prezzoMinore prezzo minore desiderato
     * @param prezzoSuperiore prezzo superiore desiderato
     */
    fun recuperaAnnunciPrezzoRange(prezzoMinore: Int?, prezzoSuperiore: Int?){

        this.prezzoInferiore = prezzoMinore
        this.prezzoSuperiore = prezzoSuperiore
    }

    /**
     * Imposta la disponibilità di spedizione per il recupero degli annunci
     *
     * @author Amato Luca
     * @param disponibilitaSpedire True se l'annuncio deve essere spedito mentre False se non deve ed infine null se non fa differenza
     */
    fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean?) {
        this.disponibilitaSpedire = disponibilitaSpedire
    }

    /**
     * Gestisce la sottoscrizione ai cambiamenti in tempo reale dei documenti nel database.
     *
     * @author Amato Luca
     * @param myDocumentiRef Riferimento al documento nel database
     * @param myListenerPrec Lista dei listener ai documenti precedenti, da rimuovere
     * @param myAnnunci Mappa degli annunci in cui aggiornare i documenti modificati.
     * @return Lista dei listener aggiornati.
     */
    private fun subscribeRealTimeDatabase(
        myDocumentiRef: Set<DocumentSnapshot>,
        myListenerPrec: MutableList<ListenerRegistration>,
        myAnnunci: HashMap<String,Annuncio>
    ): MutableList<ListenerRegistration> {

        //-- Elimino i listener per i documenti che avevo precedentemente nella home --
        for(myListenerPrecedente in myListenerPrec)
            myListenerPrecedente.remove()

        val myListener: MutableList<ListenerRegistration> = mutableListOf()

        for(myDocumentoRef in myDocumentiRef){
            myListener.add(myDocumentoRef.reference.addSnapshotListener{ snapshot, exception ->

                if (exception != null) {

                    Log.e("Errore subscribeRealTimeDatabase", exception.toString())
                    // Gestisci l'errore
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Il documento è stato modificato, sostituiscilo !
                    myAnnunci[snapshot.id] = runBlocking {  UserLoginActivity.documentoAnnuncioToObject(snapshot) }
                }
            }
            )
        }
        return myListener
    }

    /**
     * Inserisce una ricerca che l'utente ha voluto salvare
     *
     * @author Amato Luca
     * @param idUtente Identificativo dell'utente
     * @param titoloAnnuncio Sottostringa che deve essere contenuto nel titolo del annuncio, parametro opzionale
     * @param disponibilitaSpedire Indica se l'oggetto dell'annuncio è disponibile per la spedizione, parametro opzionale
     * @param prezzoSuperiore Limite superiore, parametro opzionale
     * @param prezzoInferiore Limite inferiore, parametro opzionale
     * @param distanzaMax distanza massima tra utente e annuncio, parametro opzionale
     * @param posizioneUtente posizione dell'utente
     */
    private suspend fun inserisciRicercaSuFirebaseFirestore(
        idUtente: String,
        titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoInferiore: Int?, distanzaMax : Int?, posizioneUtente: Location
    ) {

        val myCollectionUtente = this.database.collection(UserLoginActivity.Utente.nomeCollection)

        val myDocumento = myCollectionUtente.document(idUtente)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myAnnunciFiltrati = UserLoginActivity.recuperaAnnunciFiltrati(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, posizioneUtente, distanzaMax)

        val myRicerca = hashMapOf(
            "titoloAnnuncio" to titoloAnnuncio,
            "disponibilitaSpedire" to disponibilitaSpedire,
            "prezzoSuperiore" to prezzoSuperiore,
            "prezzoMinore" to prezzoInferiore,
            "numeroAnnunci" to myAnnunciFiltrati.size,
            "distanzaMax" to distanzaMax
        )

        myCollectionRicerca.add(myRicerca).await()
    }


    /**
     * Elimina una ricerca dell'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @param idRicerca Identificativo della ricerca
     */
    suspend fun eliminaRicercaFirebaseFirestore(userId : String, idRicerca: String){

        val myCollection = this.database.collection(UserLoginActivity.Utente.nomeCollection)

        val myDocumento = myCollection.document(userId)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myDocumentRicerca = myCollectionRicerca.document(idRicerca)

        myDocumentRicerca.delete().await()
    }
}
