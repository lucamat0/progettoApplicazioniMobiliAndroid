package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.ArrayList


open class UserLoginActivity : AppCompatActivity() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    public val auth = FirebaseAuth.getInstance()
    public val database = Firebase.firestore
    //--- Fine informazioni per il collegamento con firebase firestore ---

    //--- HashMap che mi memorizza gli annunci preferiti, del utente loggato e invia una notifica quando varia uno di questi. ---
    public var myAnnunciPreferiti = HashMap<String, Annuncio>()
    public var myListenerAnnunciPreferiti: ListenerRegistration? = null

    //--- Variabili utili per filtrare gli annunci ---

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    public var myAnnunciHome = HashMap<String, Annuncio>()
    public var myListenerAnnunciHome: ListenerRegistration? = null

    //Vado a specificare la collection, su cui lavoro.
    val myCollection = this.database.collection(Annuncio.nomeCollection);

    private var queryRisultato: Query = myCollection

    private var titoloAnnuncio: String? = null
    private var disponibilitaSpedire: Boolean? = null
    private var prezzoSuperiore: Integer? = null
    private var prezzoMinore: Integer? = null

    private var ultimoAnnuncioId: String? = null

    //--- Variabile utile per salvare utente, id ---
    var userId: String = "userIdProva"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

        //userId = auth.currentUser!!.uid

        runBlocking {

            recuperaAnnunciPreferitiFirebaseFirestore(userId)

            //recuperaAnnunciPerMostrarliNellaHome(1)

            //subscribeRealTimeDatabase(queryRisultato,myAnnunciHome)
        }

        lateinit var username: String
        val userRef = database.collection("utente").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                username = document.get("nome").toString()
            } else {
                Log.w("document error", "Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        //getting the recyclerView by its id
        val recyclerVu = findViewById<RecyclerView>(R.id.recyclerview)

        //this creates a vertical layout Manager
        recyclerVu.layoutManager = LinearLayoutManager(this)

        //ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        //This loop will create 20 Views containing
        //the image with the count of view
        for (i in 1..20){
            data.add(ItemsViewModel(R.drawable.ic_launcher_background, "Nome Oggetto anche Lungo $i"))
        }

        //this will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        //setting the Adapter with the recyclerView
        recyclerVu.adapter = adapter

        //logica bottone logout
        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }


        val distanceSlider = findViewById<Slider>(R.id.distanceSlider)
//        distanceSlider.setLabelFormatter {  }

//        val slider = Slider(this)
//        slider.setLabelFormatter(object : LabelFormatter() {
//            fun getFormattedValue(value: Float): String? {
//                return "MY STRING"
//            }
//        })

        distanceSlider.setLabelFormatter { value -> "$value km"; }

        distanceSlider.addOnSliderTouchListener(object :Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {
                //...
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val distanceEditText = findViewById<TextView>(R.id.maxDistance)
                val updTxt = "Distanza max: ${distanceSlider.value}km"
                distanceEditText.text = updTxt
            }
        })


        val priceSlider = findViewById<RangeSlider>(R.id.priceSlider)
        priceSlider.setLabelFormatter { value -> "${value.toInt()} €"; }

//        rangeSlider.setLabelFormatter { value: Float ->
//            val format = NumberFormat.getCurrencyInstance()
//            format.maximumFractionDigits = 0
//            format.currency = Currency.getInstance("EUR")
//            format.format(value.toDouble())
//        }

        priceSlider.addOnChangeListener { slider, value, fromUser ->
            val priceEditText = findViewById<TextView>(R.id.priceRange)
            val updTxt = "Fascia di prezzo: ${priceSlider.values[0]}€ - ${priceSlider.values[1]}€"
            priceEditText.text = updTxt
        }

        val filterButton = findViewById<ImageButton>(R.id.filters)

        filterButton.setOnClickListener {
            val filterLay = findViewById<LinearLayout>(R.id.filterElements)
            if(!filterLay.isVisible){
                filterLay.visibility = View.VISIBLE
            }else{
                filterLay.visibility = View.GONE
            }
        }


    }

    private fun definisciQuery(): Query{

        //Quando ad un annuncio non è assegnato un acquirente, non vogliamo mostrare nella home degli annunci che sono già stati venduti.
        this.queryRisultato = myCollection.whereEqualTo("userIdAcquirente",null)

        if(titoloAnnuncio != null)
            this.queryRisultato = this.queryRisultato.whereEqualTo("titolo", titoloAnnuncio)
        //siamo nel caso in cui deve essere compreso
        if(prezzoSuperiore != null && prezzoMinore != null)
            this.queryRisultato = this.queryRisultato.orderBy("prezzo").whereGreaterThan("prezzo", prezzoMinore!!).whereLessThan("prezzo", prezzoSuperiore!!)
        else{
            if(prezzoSuperiore != null)
                this.queryRisultato = this.queryRisultato.orderBy("prezzo").whereGreaterThan("prezzo", prezzoSuperiore!!)
            if(prezzoMinore != null)
                this.queryRisultato = this.queryRisultato.orderBy("prezzo").whereLessThan("prezzo", prezzoMinore!!)
        }
        if(disponibilitaSpedire != null)
            this.queryRisultato = this.queryRisultato.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)

        return this.queryRisultato
    }

    //Ogni pagina, mostra 10 annunci alla volta, questo metodo mi ritorna 10 annunci alla volta, in base ai parametri specificati dal utente
    public suspend fun recuperaAnnunciPerMostrarliNellaHome(numeroPagina: Int): HashMap<String, Annuncio>? {

        if(numeroPagina==1){

            definisciQuery()

            val myDocumenti = queryRisultato.orderBy(FieldPath.documentId()).limit(10).get().await()

            myAnnunciHome = recuperaAnnunci(myDocumenti)

            myListenerAnnunciHome?.remove()

            return myAnnunciHome
        }
        else if(numeroPagina>1 && myAnnunciHome.isNotEmpty()){

            val myDocumenti = queryRisultato.orderBy(FieldPath.documentId()).startAfter(ultimoAnnuncioId).limit(10).get().await()

            myAnnunciHome = recuperaAnnunci(myDocumenti)

            return myAnnunciHome
        }
        else
            return null
    }

    //Sospendo il metodo, per aspettare che la lista dei documenti sia stata recuperata e insirita nel arrayList
    suspend fun recuperaTuttiAnnunci() {

        this.titoloAnnuncio = null
        this.disponibilitaSpedire = null
        this.prezzoSuperiore = null
        this.prezzoMinore = null
    }

    //Recupera gli annunci che contengono una sequernza/sottosequenza nel titolo del annuncio.
    suspend fun recuperaAnnunciTitolo(nomeAnnuncio: String){

        this.titoloAnnuncio = nomeAnnuncio
    }

    //Fissano un limite inferiore
    suspend fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int){

        this.prezzoMinore = Integer(prezzoMinore)
        this.prezzoSuperiore = null
    }

    //Fissano un limite superiore
    suspend fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int) {

        this.prezzoMinore = null
        this.prezzoSuperiore = Integer(prezzoSuperiore)
    }

    // Fissano un range in cui l'annuncio deve essere compreso tra il prezzo minore e quello maggiore.
    suspend fun recuperaAnnunciPrezzoRange(prezzoMinore: Int, prezzoSuperiore: Int){

        this.prezzoMinore = Integer(prezzoMinore)
        this.prezzoSuperiore = Integer(prezzoSuperiore)
    }

    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    suspend fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean) {
        this.disponibilitaSpedire = disponibilitaSpedire
    }

    //--- Da guardare!!! ---
    /*
    public suspend fun recuperaAnnunciLocalizzazione(
        posizioneUtente: Location,
        distanzaMax: Int
    ): HashMap<String, Annuncio> {

        var myHashMap = recuperaTuttiAnnunci()

        var myAnnunci = HashMap<String, Annuncio>()

        for ((key, value) in myHashMap) {
            if (value.distanzaMinore(posizioneUtente, distanzaMax))
                myAnnunci[key] = value
        }

        return myAnnunci
    }
    */
    // --- Da fare ---

    public suspend fun subscribeRealTimeDatabase(query: Query, myAnnunci: HashMap<String,Annuncio>): ListenerRegistration {

        var  listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                var a = documentoAnnuncioToObject(myDocumentoAnnuncio.document)

                //Log.d("CAMBIO DOCUMENTO", "Il documento ${a.toString()} è cambiato!")
                Toast.makeText(this, "Il documento ${a.annuncioId} è cambiato!", Toast.LENGTH_LONG).show()

                myAnnunci[a.annuncioId] = a

                //Log.d("CONTENUTO ARRAYLIST",myAnnunciPreferiti.toString())
            }
        }

        //Log.d("CONTENUTO ARRAYLIST",myAnnunci.toString())

        return listenerRegistration
    }

    //In base alla query che viene passata, questa funzione mi filtra gli annunci e mi ritorna un arrayList di annunci.
    private fun recuperaAnnunci(myDocumenti: QuerySnapshot): HashMap<String, Annuncio> {

        //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
        var myAnnunci = HashMap<String, Annuncio>()

        for (myDocumentoAnnuncio in myDocumenti.documents) {
            myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)

            ultimoAnnuncioId = myDocumentoAnnuncio.id
        }

        return myAnnunci
    }

    //Questo metodo, avrá un voto nella recensione valido, per una maggiore usabilitá si aggiunge comunque il controllo del voto, compreso tra 1 e 5/
    public suspend fun inserisciRecensioneSuFirebaseFirestore(
        titoloRecensione: String,
        descrizioneRecensione: String,
        votoAlUtente: Int,
        idUtenteRecensito: String
    ): String? {

        //se il voto del utente si trova tra 1 e 5 allora inserisci la recensione...
        if(votoAlUtente in 1..5) {

            val myCollectionUtente = this.database.collection("utente");

            val myDocumento = myCollectionUtente.document(idUtenteRecensito)

            val myCollectionRecensione = myDocumento.collection("recensione")

            val myRecensione = hashMapOf(
                "titoloRecensione" to titoloRecensione,
                "descrizioneRecensione" to descrizioneRecensione,
                "votoAlUtente" to votoAlUtente,
                "idUtenteEspresso" to this.userId
            )

            return myCollectionRecensione.add(myRecensione).await().id.toString()
        }
        //se il voto, assegnato dal utente, non é valido...
        else
            return null
    }

    public suspend fun salvaTransazioneSuFirestoreFirebase(idUtente: String, importo: Double, tipoTransazione: Boolean): String{

        val myCollection = this.database.collection("utente")

        val myDocumentUtente = myCollection.document(idUtente)

        val myCollectionTransazioneUtente = myDocumentUtente.collection("transazione")

        //Genero un timestamp
        val dataOraAttuale = Date().time

        val myTransazione = hashMapOf(
            "importo" to importo,
            "dataOraAttuale" to dataOraAttuale,
            //tipoTransazione = true -> ricarica, tipoTransazione = false -> acquisto
            "tipo" to tipoTransazione
        )

        return myCollectionTransazioneUtente.add(myTransazione).await().id.toString()
    }

    public suspend fun acquistaAnnuncio(idUtente: String,myAnnuncio: Annuncio){

        if(isAcquistabile(idUtente,myAnnuncio.getPrezzo())){
            salvaTransazioneSuFirestoreFirebase(idUtente,myAnnuncio.getPrezzo(),false)
            myAnnuncio.setVenduto(idUtente)
        }
    }

    public suspend fun isAcquistabile(idUtente: String, prezzoAcquisto: Double) : Boolean{

        val myCollection = this.database.collection("utente")

        val myCollectionTransazioni = myCollection.document(idUtente).collection("transazione")

        return saldoAccount(myCollectionTransazioni) >= prezzoAcquisto
    }

    public suspend fun saldoAccount(myCollectionTransazioni: CollectionReference): Double {

        var query = myCollectionTransazioni.get().await()

        var saldoAccount = 0.0
        for(myTransazione in query.documents){

            val tipo = myTransazione.get("tipo") as Boolean

            Log.d("SALDO ACCOUNT", myTransazione.id + "tipo: "+ tipo.toString())

            //true -> ricarica
            if(tipo!!)
                saldoAccount += myTransazione.getDouble("importo")!!
            else
                saldoAccount -= myTransazione.getDouble("importo")!!
        }

        return saldoAccount
    }

    public suspend fun inserisciAnnuncioPreferitoFirebaseFirestore(userId : String, annuncioId: String): String {

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionPreferito = myDocumento.collection("preferito")

        val dataOraAttuale = Date().time

        val myElementoPreferito = hashMapOf(
            "annuncioId" to annuncioId,
            "dataOraAttuale" to dataOraAttuale
        )

        val idPreferito = myCollectionPreferito.add(myElementoPreferito).await().id.toString()

        val myDocumentiPreferiti = myCollectionPreferito.get().await()

        if(myDocumentiPreferiti.documents.size>0)
            aggiornaListenerPreferiti(myDocumentiPreferiti)

        return idPreferito
    }

    public suspend fun eliminaAnnuncioPreferitoFirebaseFirestore(userId : String, elementoCarrelloId: String){

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionCarrello = myDocumento.collection("preferito")

        val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

        myDocumentCarrello.delete().await()
    }

    public suspend fun recuperaAnnunciPreferitiFirebaseFirestore(userId : String): HashMap<String, Annuncio>? {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumentUtente = myCollectionUtente.document(userId)

        val myDocumentiPreferiti = myDocumentUtente.collection("preferito").get().await()

        if(myDocumentiPreferiti.documents.size>0) {

            aggiornaListenerPreferiti(myDocumentiPreferiti)

            return myAnnunciPreferiti
        }
        return null
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

        myListenerAnnunciPreferiti = subscribeRealTimeDatabase(query, myAnnunciPreferiti)
    }

    public suspend fun inserisciAnnuncioCarrelloFirebaseFirestore(userId : String, annuncioId: String): String {

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionCarrello = myDocumento.collection("carrello")

        val dataOraAttuale = Date().time

        val myElementoCarrello = hashMapOf(
            "annuncioId" to annuncioId,
            "dataOraAttuale" to dataOraAttuale
        )

        return myCollectionCarrello.add(myElementoCarrello).await().id.toString()
    }

    public suspend fun eliminaAnnuncioCarrelloFirebaseFirestore(userId : String, elementoCarrelloId: String){

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionCarrello = myDocumento.collection("carrello")

        val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

        myDocumentCarrello.delete().await()
    }

    public suspend fun recuperaAnnunciCarrelloFirebaseFirestore(userId : String): HashMap<String, Annuncio>{

        val myCollection = this.database.collection("utente")

        val myDocument = myCollection.document(userId)

        val myElementiCarrello = myDocument.collection("carrello").get().await()

        val myCollectionAnnuncio = this.database.collection(Annuncio.nomeCollection)
        val myHashMap = HashMap<String, Annuncio>()
        for(myElemento in myElementiCarrello.documents){

            val myDocumentAnnuncio = myCollectionAnnuncio.document((myElemento.get("annuncioId") as String)).get().await()

            val myAnnuncio = documentoAnnuncioToObject(myDocumentAnnuncio)

            myHashMap[myAnnuncio.annuncioId] = myAnnuncio
        }
        return myHashMap
    }

    public fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

        val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

        val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita") as Long?

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
            myDocumentoAnnuncio.id as String)
    }
}

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            //imageView.setImageURI(imageUri)

            Log.d("Immagine",imageUri.toString())

            val userId = "aaaaa"

            val newAnnuncio = Annuncio(userId, "Mr Robot: Season 1 Blu-Ray + Digital HD", "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.", 16.99, 2, true, "filmETv/serieTv",  imageUri!!)

            //DA CAMBIARE!!!
            newAnnuncio.salvaAnnuncioSuFirebase(database)

        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
         */
