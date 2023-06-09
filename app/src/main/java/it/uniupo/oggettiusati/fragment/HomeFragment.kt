package it.uniupo.oggettiusati.fragment

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    var myAnnunciHome = HashMap<String, Annuncio>()
    var myListenerAnnunciHome: MutableList<ListenerRegistration> = mutableListOf()


    //--- Variabili utili per filtrare gli annunci ---
    private var titoloAnnuncio: String? = null
    private var disponibilitaSpedire: Boolean? = null
    private var prezzoSuperiore: Int? = null
    private var prezzoInferiore: Int? = null

    //--- Variabile utile per salvare utente, id ---
    //var userId: String = "userIdProva"

    val userId = auth.currentUser!!.uid

    companion object {

        fun recuperaAnnunciLocalizzazione(
            posizioneUtente: Location,
            distanzaMax: Int,
            myAnnunciDaFiltrare: HashMap<String, Annuncio>
        ): HashMap<String, Annuncio> {

            val myAnnunciFiltrati = HashMap<String, Annuncio>()

            //Recupero il documento e creo Annuncio, utilizzo il metodo per capire se la distanza è rispettata
            for (myAnnuncio in myAnnunciDaFiltrare.values) {
                if (myAnnuncio.distanzaMinore(posizioneUtente, distanzaMax))
                    Log.d("test","annuncio vicino")
                    myAnnunciFiltrati[myAnnuncio.getAnnuncioId()] = myAnnuncio
            }

            return myAnnunciFiltrati
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentRootView = inflater.inflate(R.layout.fragment_home, container, false)

        lateinit var username: String
        val userRef = database.collection("utente").document(userId)
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

        runBlocking {

            //Recupero tutti gli annunci, preferiti, per la notifica.
            FavoritesFragment.recuperaAnnunciPreferitiFirebaseFirestore(
                auth.uid!!,
                requireActivity()
            )

            recuperaAnnunciPerMostrarliNellaHome()

            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(myAnnunciHome, R.layout.card_view_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

        val distanceSlider = view?.findViewById<Slider>(R.id.distanceSlider)
//        distanceSlider?.isEnabled = false
        val testoDistanza = view?.findViewById<TextView>(R.id.maxDistance)

        var updTxt = "Distanza max: ${distanceSlider?.value}km"
        testoDistanza?.text = updTxt

        val testoPrezzo = view?.findViewById<TextView>(R.id.priceRange)
        val radioGroupPrezzo = view?.findViewById<RadioGroup>(R.id.rGroup_prezzo)!!
        setEnabledRadioGroup(radioGroupPrezzo, false)


        val priceSlider = view?.findViewById<RangeSlider>(R.id.price_range_slider)
        val selezionePrezzo = requireView().findViewById<CheckBox>(R.id.select_price)
        val prezzoMin = view?.findViewById<Slider>(R.id.price_min_slider)
        val prezzoMax = view?.findViewById<Slider>(R.id.price_max_slider)

        updTxt = "Fascia di prezzo: ${priceSlider!!.values[0]}€ - ${priceSlider.values[1]}€"
        testoPrezzo?.text = updTxt

        prezzoMin?.setLabelFormatter { value -> "$value €"; }

        prezzoMin?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Prezzo min: ${prezzoMin.value}€"
                testoPrezzo?.text = updTxt
            }
        })

        prezzoMax?.setLabelFormatter { value -> "$value €"; }

        prezzoMax?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Prezzo max: ${prezzoMax.value}€"
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
             updTxt = "Fascia di prezzo: ${priceSlider.values[0]}€ - ${priceSlider.values[1]}€"
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
        val shippingSwitch = view?.findViewById<SwitchCompat>(R.id.shipping_switch)
        shippingSwitch?.isChecked = false

        listOf(
            selezionaDistanza,
            selezionaSpedizione,
            selezionePrezzo
        ).forEach {
            it?.isChecked = false
        }

        selezionaDistanza?.setOnClickListener {
            distanceSlider?.isEnabled = selezionaDistanza.isChecked
            testoDistanza?.isEnabled = selezionaDistanza.isChecked
        }

        selezionaSpedizione?.setOnClickListener {
            shippingSwitch?.isEnabled = selezionaSpedizione.isChecked
        }

        val recuperaTitolo = casellaRicerca?.text.toString()
        buttonRicerca?.setOnClickListener {



            if(recuperaTitolo.isEmpty())
                recuperaAnnunciTitolo(null)
            else
                recuperaAnnunciTitolo(recuperaTitolo)

            if(selezionePrezzo.isChecked) {
                when (radioGroupPrezzo.checkedRadioButtonId) {
                    idPrezzoRange -> recuperaAnnunciPrezzoRange(priceSlider.values[1].toInt(), priceSlider.values[0].toInt())
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

                //-- Location simulata x test ---
                var posizioneUtente: Location = Location("provider")
                posizioneUtente.latitude = 44.922
                posizioneUtente.longitude = 8.617

                if (selezionaDistanza!!.isChecked) {

                    myAnnunciHome = recuperaAnnunciPerMostrarliNellaHome()

                    myAnnunciHome = recuperaAnnunciLocalizzazione(
                        posizioneUtente,
                        distanceSlider?.value?.toInt()!!,
                        myAnnunciHome
                    )
                }
                else
                    myAnnunciHome = recuperaAnnunciPerMostrarliNellaHome()


                val adapterRicerca = CustomAdapter(myAnnunciHome, R.layout.card_view_design)
                val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
                //setting the Adapter with the recyclerView
                recyclerVu?.adapter = adapterRicerca
            }
        }

        //---

//        //logica bottone logout
//        val logoutButton = view?.findViewById<Button>(R.id.logout)
//        logoutButton?.setOnClickListener {
//            Toast.makeText(activity, "Uscita...", Toast.LENGTH_SHORT).show()
//            FirebaseAuth.getInstance().signOut()
//            startActivity(Intent(activity, MainActivity::class.java))
//        }

        // --- Inizio metodi relativi ai filtri ---


        distanceSlider?.setLabelFormatter { value -> "$value km"; }

        distanceSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //...
            }

            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Distanza max: ${distanceSlider.value}km"
                testoDistanza?.text = updTxt
            }
        })

        priceSlider.setLabelFormatter { value -> "${value.toInt()} €"; }

        priceSlider.addOnChangeListener { _, _, _ ->
             updTxt = "Fascia di prezzo: ${priceSlider.values[0]}€ - ${priceSlider.values[1]}€"
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
                val distMax :Int?

                if(recuperaTitolo.isEmpty())
                    titoloAnnuncio = null
                else
                    titoloAnnuncio = casellaRicerca?.text.toString()

                if (selezionaDistanza!!.isChecked)
                    distMax = distanceSlider?.value?.toInt()
                else
                    distMax = null
                if(selezionePrezzo.isChecked) {
                    when (radioGroupPrezzo.checkedRadioButtonId) {
                        idPrezzoRange -> {
                            prezzoSuperiore = priceSlider.values[1].toInt()
                            prezzoInferiore =  priceSlider . values [0].toInt()
                        }
                        idPrezzoMin -> {
                            prezzoSuperiore = null
                            prezzoInferiore = prezzoMin?.value?.toInt()!!
                        }
                        idPrezzoMax -> {
                            prezzoSuperiore = prezzoMax?.value?.toInt()
                            prezzoInferiore = null
                        }
                    }
                }
                else {
                    prezzoSuperiore = null
                    prezzoInferiore = null
                }

                if(selezionaSpedizione!!.isChecked)
                    disponibilitaSpedire = shippingSwitch?.isChecked
                else
                    disponibilitaSpedire = null

                var posizioneUtente: Location = Location("provider")
                posizioneUtente.latitude = 44.922
                posizioneUtente.longitude = 8.617

                inserisciRicercaSuFirebaseFirestore(auth.uid!!, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, distMax, posizioneUtente)
            }
        }

        Toast.makeText(activity, "Sei nella sezione home", Toast.LENGTH_SHORT).show()
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


    //Ogni pagina, mostra 10 annunci alla volta, questo metodo mi ritorna 10 annunci alla volta, in base ai parametri specificati dal utente
    suspend fun recuperaAnnunciPerMostrarliNellaHome(): HashMap<String, Annuncio> {

            //-- Recupero i riferimenti ai miei documenti --
            val myDocumentiRef = UserLoginActivity.definisciQuery(this.titoloAnnuncio, this.disponibilitaSpedire, this.prezzoSuperiore, this.prezzoInferiore)

            //-- Trasmormo il riferimento ai documenti in Annunci --
            this.myAnnunciHome = UserLoginActivity.recuperaAnnunci(myDocumentiRef)

            //-- Elimino i listener per i documenti che avevo precedentemente nella home --
            for(myListenerPrecedente in myListenerAnnunciHome)
                myListenerPrecedente.remove()

            //-- Definisco i nuovi listener, per i documenti che ho ora nella Home --
            this.myListenerAnnunciHome = subscribeRealTimeDatabase(myDocumentiRef, myAnnunciHome)

            return myAnnunciHome
    }

    //Recupera gli annunci che contengono una sequernza/sottosequenza nel titolo del annuncio.
    fun recuperaAnnunciTitolo(nomeAnnuncio: String?) {

        this.titoloAnnuncio = nomeAnnuncio
    }


    //Sospendo il metodo, per aspettare che la lista dei documenti sia stata recuperata e insirita nel arrayList
    fun recuperaTuttiAnnunci() {

        this.titoloAnnuncio = null
        this.disponibilitaSpedire = null
        this.prezzoSuperiore = null
        this.prezzoInferiore = null
        this.disponibilitaSpedire = null
    }

    //Fissano un limite inferiore
    fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int){

        this.prezzoInferiore = prezzoMinore
        this.prezzoSuperiore = null
    }

    //Fissano un limite superiore
    fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int) {

        this.prezzoInferiore = null
        this.prezzoSuperiore = prezzoSuperiore
    }

    // Fissano un range in cui l'annuncio deve essere compreso tra il prezzo minore e quello maggiore.
    fun recuperaAnnunciPrezzoRange(prezzoMinore: Int?, prezzoSuperiore: Int?){

        this.prezzoInferiore = prezzoMinore
        this.prezzoSuperiore = prezzoSuperiore
    }

    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean?) {
        this.disponibilitaSpedire = disponibilitaSpedire
    }

    fun subscribeRealTimeDatabase(
        myDocumentiRef: Set<DocumentSnapshot>,
        myAnnunciHome: HashMap<String, Annuncio>
    ): MutableList<ListenerRegistration> {

        var myListener: MutableList<ListenerRegistration> = mutableListOf()

        for(myDocumentoRef in myDocumentiRef){
                myListener.add(myDocumentoRef.reference.addSnapshotListener{ snapshot, exception ->

                        if (exception != null) {

                            Log.e("Errore subscribeRealTimeDatabase", exception.toString())
                            // Gestisci l'errore
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            // Il documento è stato modificato, sostituiscilo !
                            myAnnunciHome[snapshot.id] = UserLoginActivity.documentoAnnuncioToObject(snapshot)
                        }
                    }
                )
            }
        return myListener
    }


    //--- Mi notifica quando il numero di annunci, che rispettano i criteri cambia ---
    suspend fun inserisciRicercaSuFirebaseFirestore(
        idUtente: String,
        titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?, distanzaMax : Int?, posizioneUtente: Location
    ): String {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumento = myCollectionUtente.document(idUtente)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myDocumentAnnunci = UserLoginActivity.recuperaAnnunci(UserLoginActivity.definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore))

        var numeroAnnunci = myDocumentAnnunci.size
        if(distanzaMax != null)
            numeroAnnunci = recuperaAnnunciLocalizzazione(posizioneUtente, distanzaMax, myDocumentAnnunci).size

        val myRicerca = hashMapOf(
            "titoloAnnuncio" to titoloAnnuncio,
            "disponibilitaSpedire" to disponibilitaSpedire,
            "prezzoSuperiore" to prezzoSuperiore,
            "prezzoMinore" to prezzoMinore,
            "numeroAnnunci" to numeroAnnunci,
            "distanzaMax" to distanzaMax
        )

        return myCollectionRicerca.add(myRicerca).await().id
    }




//     suspend fun eliminaRicercaFirebaseFirestore(userId : String, idRicerca: String){
//
//        val myCollection = this.database.collection("utente")
//
//        val myDocumento = myCollection.document(userId)
//
//        val myCollectionRicerca = myDocumento.collection("ricerca")
//
//        val myDocumentRicerca = myCollectionRicerca.document(idRicerca)
//
//        myDocumentRicerca.delete().await()
//    }

}
