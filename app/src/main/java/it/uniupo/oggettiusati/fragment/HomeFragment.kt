package it.uniupo.oggettiusati.fragment

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.net.UnknownServiceException

class HomeFragment : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    var myAnnunciHome = HashMap<String, Annuncio>()
    var myListenerAnnunciHome: ListenerRegistration? = null

    //Vado a specificare su che collection lavoro
    private var queryRisultato: Query = this.database.collection(Annuncio.nomeCollection)

    //--- Variabili utili per filtrare gli annunci ---
    private var titoloAnnuncio: String? = null
    private var disponibilitaSpedire: Boolean? = null
    private var prezzoSuperiore: Int? = null
    private var prezzoMinore: Int? = null

    private var ultimoAnnuncioId: String? = null

    //--- Variabile utile per salvare utente, id ---
    //var userId: String = "userIdProva"

    val userId = auth.currentUser!!.uid

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
            FavoritesFragment.recuperaAnnunciPreferitiFirebaseFirestore(auth.uid!!, activity!!)

            recuperaAnnunciPerMostrarliNellaHome(1)

            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(myAnnunciHome, R.layout.card_view_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }


        //---

        val buttonRicercaTitolo = view?.findViewById<ImageButton>(R.id.searchButton)
        val casellaRicerca = view?.findViewById<EditText>(R.id.search)

        buttonRicercaTitolo?.setOnClickListener {

            val recuperaTitolo = casellaRicerca?.text.toString()

            if(recuperaTitolo.isEmpty())
                recuperaAnnunciTitolo(null)
            else
                recuperaAnnunciTitolo(recuperaTitolo)

            runBlocking {
                recuperaAnnunciPerMostrarliNellaHome(1)

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
        val distanceSlider = view?.findViewById<Slider>(R.id.distanceSlider)

        distanceSlider?.setLabelFormatter { value -> "$value km"; }

        distanceSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //...
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val distanceEditText = view?.findViewById<TextView>(R.id.maxDistance)
                val updTxt = "Distanza max: ${distanceSlider.value}km"
                distanceEditText?.text = updTxt
            }
        })


        val priceSlider = view?.findViewById<RangeSlider>(R.id.priceSlider)
        priceSlider?.setLabelFormatter { value -> "${value.toInt()} €"; }

        priceSlider?.addOnChangeListener { _, _, _ ->
            val priceEditText = view?.findViewById<TextView>(R.id.priceRange)
            val updTxt = "Fascia di prezzo: ${priceSlider.values[0]}€ - ${priceSlider.values[1]}€"
            priceEditText?.text = updTxt
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

        Toast.makeText(activity, "Sei nella sezione home", Toast.LENGTH_SHORT).show()
    }



    //Ogni pagina, mostra 10 annunci alla volta, questo metodo mi ritorna 10 annunci alla volta, in base ai parametri specificati dal utente
    suspend fun recuperaAnnunciPerMostrarliNellaHome(numeroPagina: Int): HashMap<String, Annuncio>? {

        if(numeroPagina == 1) {

            this.myListenerAnnunciHome?.remove()

            this.queryRisultato = UserLoginActivity.definisciQuery(this.titoloAnnuncio, this.disponibilitaSpedire, this.prezzoSuperiore, this.prezzoMinore)

            val myDocumenti = this.queryRisultato.orderBy(FieldPath.documentId()).limit(10).get().await()

            this.myAnnunciHome = UserLoginActivity.recuperaAnnunci(myDocumenti, true)

            //this.myListenerAnnunciHome = subscribeRealTimeDatabase(queryRisultato, myAnnunciHome, false)

            this.myListenerAnnunciHome = subscribeRealTimeDatabase(this.queryRisultato, this.myAnnunciHome)

            return myAnnunciHome
        } else if(numeroPagina>1 && myAnnunciHome.isNotEmpty()) {

            this.myListenerAnnunciHome?.remove()

            this.queryRisultato = this.queryRisultato.orderBy(FieldPath.documentId()).startAfter(this.ultimoAnnuncioId).limit(10)

            val myDocumenti = this.queryRisultato.get().await()

            this.myAnnunciHome = UserLoginActivity.recuperaAnnunci(myDocumenti, true)

            this.myListenerAnnunciHome = subscribeRealTimeDatabase(this.queryRisultato, this.myAnnunciHome)

            return this.myAnnunciHome
        } else
            return null
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
        this.prezzoMinore = null
    }

    //Fissano un limite inferiore
    fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int){

        this.prezzoMinore = prezzoMinore
        this.prezzoSuperiore = null
    }

    //Fissano un limite superiore
    fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int) {

        this.prezzoMinore = null
        this.prezzoSuperiore = prezzoSuperiore
    }

    // Fissano un range in cui l'annuncio deve essere compreso tra il prezzo minore e quello maggiore.
    fun recuperaAnnunciPrezzoRange(prezzoMinore: Int, prezzoSuperiore: Int){

        this.prezzoMinore = prezzoMinore
        this.prezzoSuperiore = prezzoSuperiore
    }

    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean) {
        this.disponibilitaSpedire = disponibilitaSpedire
    }


    fun subscribeRealTimeDatabase(query: Query, myAnnunci: HashMap<String, Annuncio>): ListenerRegistration {

        val  listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                val a = UserLoginActivity.documentoAnnuncioToObject(myDocumentoAnnuncio.document)

                //tengo in memoria, i 10 annunci, sempre aggiornati!
                myAnnunci[a.getAnnuncioId()] = a
            }
        }
        return listenerRegistration
    }

    //--- Mi notifica quando il numero di annunci, che rispettano i criteri cambia ---
    suspend fun inserisciRicercaSuFirebaseFirestore(
        idUtente: String,
        titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?
    ): String {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumento = myCollectionUtente.document(idUtente)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        //numero di documenti che corrisponde alla ricerca effettuata dal utente.
        val numeroAnnunci = UserLoginActivity.definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore).get().await().documents.size

        val myRicerca = hashMapOf(
            "titoloAnnuncio" to titoloAnnuncio,
            "disponibilitaSpedire" to disponibilitaSpedire,
            "prezzoSuperiore" to prezzoSuperiore,
            "prezzoMinore" to prezzoMinore,
            "numeroAnnunci" to numeroAnnunci
        )

        return myCollectionRicerca.add(myRicerca).await().id
    }

    //--- ATTENZIONE: Non implementato il mostrare solo 10 annunci! ---
    public suspend fun recuperaAnnunciLocalizzazione(
        posizioneUtente: Location,
        distanzaMax: Int
    ): HashMap<String, Annuncio> {

        var myAnnunci = HashMap<String, Annuncio>()

        //Recupero il documento e creo Annuncio, utilizzo il metodo per capire se la distanza è rispettata
        for (myDocument in queryRisultato.get().await().documents) {

            val myAnnuncio = UserLoginActivity.documentoAnnuncioToObject(myDocument)

            if (myAnnuncio.distanzaMinore(posizioneUtente, distanzaMax))
                myAnnunci[myAnnuncio.getAnnuncioId()] = myAnnuncio
        }

        return myAnnunci
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