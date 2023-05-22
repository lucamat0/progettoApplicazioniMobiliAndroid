package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

class HomeFragment : Fragment() {
    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    //--- HashMap che mi memorizza gli annunci preferiti, del utente loggato e invia una notifica quando varia uno di questi. ---
    var myAnnunciPreferiti = HashMap<String, Annuncio>()
    var myListenerAnnunciPreferiti: ListenerRegistration? = null

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    var myAnnunciHome = HashMap<String, Annuncio>()
    var myListenerAnnunciHome: ListenerRegistration? = null

    //Vado a specificare la collection, su cui lavoro.
    val myCollection = this.database.collection(Annuncio.nomeCollection)

    private var queryRisultato: Query = myCollection

    //--- Variabili utili per filtrare gli annunci ---
    private var titoloAnnuncio: String? = null
    private var disponibilitaSpedire: Boolean? = null
    private var prezzoSuperiore: Int? = null
    private var prezzoMinore: Int? = null

    private var ultimoAnnuncioId: String? = null

    //--- Variabile utile per salvare utente, id ---
    //var userId: String = "userIdProva"

    val userId = auth.currentUser!!.uid

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //...
//    }

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

            recuperaAnnunciPerMostrarliNellaHome(1)

            recuperaAnnunciPreferitiFirebaseFirestore(userId)

            controllaStatoRicercheAnnunci(userId)

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

    private fun definisciQuery(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?): Query {

        //Quando ad un annuncio non è assegnato un acquirente, non vogliamo mostrare nella home degli annunci che sono già stati venduti.
        var myQuery = myCollection.whereEqualTo("userIdAcquirente", null)

        if(titoloAnnuncio != null)
            myQuery = myQuery.whereEqualTo("titolo", titoloAnnuncio)
        //siamo nel caso in cui deve essere compreso
        if(prezzoSuperiore != null && prezzoMinore != null)
            myQuery = myQuery.orderBy("prezzo").whereGreaterThan("prezzo", prezzoMinore).whereLessThan("prezzo", prezzoSuperiore)
        else {
            if(prezzoSuperiore != null)
                myQuery = myQuery.orderBy("prezzo").whereGreaterThan("prezzo", prezzoSuperiore)
            else if(prezzoMinore != null)
                myQuery = myQuery.orderBy("prezzo").whereLessThan("prezzo", prezzoMinore)
        }
        if(disponibilitaSpedire != null)
            myQuery = myQuery.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)


        return myQuery
    }

    //Ogni pagina, mostra 10 annunci alla volta, questo metodo mi ritorna 10 annunci alla volta, in base ai parametri specificati dal utente
    suspend fun recuperaAnnunciPerMostrarliNellaHome(numeroPagina: Int): HashMap<String, Annuncio>? {

        if(numeroPagina == 1) {

            myListenerAnnunciHome?.remove()

            queryRisultato = definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore)

            val myDocumenti = queryRisultato.orderBy(FieldPath.documentId()).limit(10).get().await()

            myAnnunciHome = recuperaAnnunci(myDocumenti)

            this.myListenerAnnunciHome = subscribeRealTimeDatabase(queryRisultato, myAnnunciHome, false)

            return myAnnunciHome
        } else if(numeroPagina>1 && myAnnunciHome.isNotEmpty()) {

            myListenerAnnunciHome?.remove()

            queryRisultato = queryRisultato.orderBy(FieldPath.documentId()).startAfter(ultimoAnnuncioId).limit(10)

            this.myListenerAnnunciHome = subscribeRealTimeDatabase(queryRisultato, myAnnunciHome, false)

            val myDocumenti = queryRisultato.get().await()

            //Log.d("MOSTRA HOME LAST", ultimoAnnuncioId.toString())

            myAnnunciHome = recuperaAnnunci(myDocumenti)

            return myAnnunciHome
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

    fun subscribeRealTimeDatabase(query: Query, myAnnunci: HashMap<String, Annuncio>, preferiti: Boolean): ListenerRegistration {

        val  listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                val a = documentoAnnuncioToObject(myDocumentoAnnuncio.document)

                //Log.d("CAMBIO DOCUMENTO", "Il documento ${a.toString()} è cambiato!")

                myAnnunci[a.getAnnuncioId()] = a

                if(preferiti)
//                    Toast.makeText(activity, "Il documento ${a.getAnnuncioId()} è cambiato!", Toast.LENGTH_LONG).show()
                else {
//                    val adapter = CustomAdapter(myAnnunciHome, R.layout.card_view_design)
//
//                    val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
//
//                    //setting the Adapter with the recyclerView
//                    recyclerVu?.adapter = adapter
                }

                //Log.d("CONTENUTO ARRAYLIST", myAnnunciPreferiti.toString())
            }
        }

        //Log.d("CONTENUTO ARRAYLIST", myAnnunci.toString())

        return listenerRegistration
    }

    //In base alla query che viene passata, questa funzione mi filtra gli annunci e mi ritorna un arrayList di annunci.
    private fun recuperaAnnunci(myDocumenti: QuerySnapshot): HashMap<String, Annuncio> {

        //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
        val myAnnunci = HashMap<String, Annuncio>()

        for (myDocumentoAnnuncio in myDocumenti.documents) {
            myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)

            ultimoAnnuncioId = myDocumentoAnnuncio.id
        }

        return myAnnunci
    }

    suspend fun controllaStatoRicercheAnnunci(userId: String): Boolean {

        val myCollection = this.database.collection("utente")

        val myDocumentoUtente = myCollection.document(userId)

        val myCollectionRicerca = myDocumentoUtente.collection("ricerca")

        val myDocumentiRicerca = myCollectionRicerca.get().await()

        for(myDocumento in myDocumentiRicerca.documents) {

            val titoloAnnuncio = myDocumento.get("titoloAnnuncio") as String?
            val disponibilitaSpedire = myDocumento.getBoolean("disponibilitaSpedire")

            val prezzoSuperiore = (myDocumento.get("prezzoSuperiore") as Long?)?.toInt()

            val prezzoMinore = (myDocumento.get("prezzoMinore") as Long?)?.toInt()

            val numeroAnnunciRicerca = (myDocumento.get("numeroAnnunci") as Long).toInt()

            val query = definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore)

            val numeroAnnunci = query.get().await().size()

            if( numeroAnnunci > numeroAnnunciRicerca) {
                Toast.makeText(
                    activity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono aumentati!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(userId, myDocumento.id, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore, numeroAnnunci)

                return true
            } else if(numeroAnnunci < numeroAnnunciRicerca) {
                Toast.makeText(
                    activity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono diminuiti!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(userId, myDocumento.id, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore, numeroAnnunci)

                return true
            }
        }
        return false
    }

    private suspend fun aggiornaRicerca(userId: String, idRicerca: String, titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?, numeroAnnunci: Int) {

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myRicerca = myCollectionRicerca.document(idRicerca)

        myRicerca.update("titoloAnnuncio", titoloAnnuncio,"disponibilitaSpedire", disponibilitaSpedire, "prezzoSuperiore", prezzoSuperiore, "prezzoMinore", prezzoMinore, "numeroAnnunci", numeroAnnunci).await()
    }

    suspend fun recuperaAnnunciPreferitiFirebaseFirestore(userId: String): HashMap<String, Annuncio> {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumentUtente = myCollectionUtente.document(userId)

        val myDocumentiPreferiti = myDocumentUtente.collection("preferito").get().await()

        if(myDocumentiPreferiti.documents.size > 0) {

            aggiornaListenerPreferiti(myDocumentiPreferiti)

            return myAnnunciPreferiti
        }
        return HashMap()
    }

    //Listener dei preferiti si aggiorna quando, inseriamo un nuovo elemento nei preferiti, oppure quando andiamo a recuperare i preferiti.
    //Da qui, ogni modifica effettuata sugli annunci ci viene notificata, provvisoriamente con un Toast.
    private suspend fun aggiornaListenerPreferiti(myDocumentiPreferiti: QuerySnapshot) {

        val myListaId = mutableListOf<String>()

        for (myPreferito in myDocumentiPreferiti.documents)
            myListaId.add(myPreferito.get("annuncioId") as String)

        val myCollectionAnnuncio = this.database.collection(Annuncio.nomeCollection)

        //--- Inizio informazioni per il mantenimento delle informazioni, filtrate, aggiornate ---
        val query = myCollectionAnnuncio.whereIn(FieldPath.documentId(), myListaId)

        myAnnunciPreferiti = recuperaAnnunci(query.get().await())

        myListenerAnnunciPreferiti?.remove()

        myListenerAnnunciPreferiti = subscribeRealTimeDatabase(query, myAnnunciPreferiti, true)
    }


    suspend fun inserisciRicercaSuFirebaseFirestore(
        idUtente: String,
        titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?
    ): String {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumento = myCollectionUtente.document(idUtente)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        //numero di documenti che corrisponde alla ricerca effettuata dal utente.
        val numeroAnnunci = definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore).get().await().documents.size

        val myRicerca = hashMapOf(
            "titoloAnnuncio" to titoloAnnuncio,
            "disponibilitaSpedire" to disponibilitaSpedire,
            "prezzoSuperiore" to prezzoSuperiore,
            "prezzoMinore" to prezzoMinore,
            "numeroAnnunci" to numeroAnnunci
        )

        return myCollectionRicerca.add(myRicerca).await().id
    }


    //Devo recuperare annuncio
    // --- Da fare --
//
//    public suspend fun recuperaAnnunciLocalizzazione(
//        posizioneUtente: Location,
//        distanzaMax: Int
//    ): HashMap<String, Annuncio> {
//
//        var myHashMap = recuperaTuttiAnnunci()
//
//        var myAnnunci = HashMap<String, Annuncio>()
//
//        for ((key, value) in myHashMap) {
//            if (value.distanzaMinore(posizioneUtente, distanzaMax))
//                myAnnunci[key] = value
//        }
//
//        return myAnnunci
//    }
//

    // suspend fun eliminaRicercaFirebaseFirestore(userId : String, idRicerca: String){
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
//
    fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

        val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

        val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita")

        return Annuncio(
            myDocumentoAnnuncio.get("userId") as String,
            myDocumentoAnnuncio.get("titolo") as String,
            myDocumentoAnnuncio.get("descrizione") as String,
            myDocumentoAnnuncio.get("prezzo") as Double,
            (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
            myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
            myDocumentoAnnuncio.get("categoria") as String,
            myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
            myDocumentoAnnuncio.getLong("timeStampInizioVendita") as Long,
            timeStampFineVendita,
            userIdAcquirente,
            myDocumentoAnnuncio.id)
    }
}
