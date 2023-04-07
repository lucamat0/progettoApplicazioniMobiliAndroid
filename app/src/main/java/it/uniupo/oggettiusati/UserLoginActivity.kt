package it.uniupo.oggettiusati

import android.app.SearchManager
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.HashMap
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


open class UserLoginActivity : AppCompatActivity() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    public val auth = FirebaseAuth.getInstance()
    public val database = Firebase.firestore
    //--- Fine informazioni per il collegamento con firebase firestore ---

    //Indica id dell'utente loggato
    lateinit var userId: String

    //--- Inizio informazioni per il mantenimento delle informazioni, filtrate, aggiornate ---

    private lateinit var query: Query

    private var myAnnunci = HashMap<String, Annuncio>()

    //--- Fine informazioni per il mantenimento delle informazioni, filtrate, aggiornate ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

        runBlocking {
            myAnnunci = recuperaTuttiAnnunci()

            //--- Metodo utilizzato per il mantenimento delle informazioni aggiornate ---
            subscribeRealTimeDatabase()
        }

        //utilizzato per recuperare i parametri
        val extras = intent.extras

        //recuperato
        userId = extras?.getString("userId").toString()

        lateinit var username: String
        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                username = document.get("nome").toString()
            } else {
                Log.w("document error", "Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        //RecyclerView

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

    //Sospendo il metodo, per aspettare che la lista dei documenti sia stata recuperata e insirita nel arrayList
    public suspend fun recuperaTuttiAnnunci(): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Recupera gli annunci che contengono una sequernza/sottosequenza nel titolo del annuncio.
    suspend fun recuperaAnnunciTitolo(nomeAnnuncio: String): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection.whereEqualTo("titolo", nomeAnnuncio)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite inferiore
    public suspend fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection.whereLessThan("prezzo", prezzoMinore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite superiore
    public suspend fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection.whereGreaterThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    // Fissano un range in cui l'annuncio deve essere maggiore del prezzo minore e minore del prezzo superiore.
    public suspend fun recuperaAnnunciPrezzoRange(
        prezzoMinore: Int,
        prezzoSuperiore: Int
    ): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection.whereGreaterThan("prezzo", prezzoMinore)
            .whereLessThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }


    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    public suspend fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection(Annuncio.nomeCollection);

        query = myCollection.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        var myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //--- Da testare ---
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
    //--- ----

    private suspend fun subscribeRealTimeDatabase() {

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                val userIdAcquirente: String? =
                    myDocumentoAnnuncio.document.get("userIdAcquirente") as String?

                var a = Annuncio(
                    myDocumentoAnnuncio.document.get("userId") as String,
                    myDocumentoAnnuncio.document.get("titolo") as String,
                    myDocumentoAnnuncio.document.get("descrizione") as String,
                    myDocumentoAnnuncio.document.get("prezzo") as Double,
                    (myDocumentoAnnuncio.document.getLong("stato") as Long).toInt(),
                    myDocumentoAnnuncio.document.get("disponibilitaSpedire") as Boolean,
                    myDocumentoAnnuncio.document.getGeoPoint("posizione") as GeoPoint,
                    myDocumentoAnnuncio.document.get("categoria") as String,
                    userIdAcquirente,
                    myDocumentoAnnuncio.document.id as String
                );

                //Log.d("CAMBIO DOCUMENTO", "Il documento ${a.toString()} è cambiato!")

                myAnnunci[myDocumentoAnnuncio.document.id] = a

                //Log.d("CONTENUTO ARRAYLIST",myAnnunci.toString())
            }
        }

        //Log.d("CONTENUTO ARRAYLIST",myAnnunci.toString())
    }

    //In base alla query che viene passata, questa funzione mi filtra gli annunci e mi ritorna un arrayList di annunci.
    private fun recuperaAnnunci(myDocumenti: QuerySnapshot): HashMap<String, Annuncio> {

        //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
        var myAnnunci = HashMap<String, Annuncio>()

        for (myDocumentoAnnuncio in myDocumenti.documents) {

            val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

            //Creazione del oggetto Annuncio, con gli elementi che si trovano sul DB
            var a = Annuncio(
                myDocumentoAnnuncio.get("userId") as String,
                myDocumentoAnnuncio.get("titolo") as String,
                myDocumentoAnnuncio.get("descrizione") as String,
                myDocumentoAnnuncio.get("prezzo") as Double,
                (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
                myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.get("categoria") as String,
                userIdAcquirente,
                myDocumentoAnnuncio.id as String
            );

            myAnnunci[myDocumentoAnnuncio.id] = a
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
