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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


open class UserLoginActivity : AppCompatActivity() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore
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

//        runBlocking{
//            Annuncio(
//                userId,
//                "2 gomme 205 55 16 estive al 70% falken",
//                "CONTROLLA LE DISPONIBILITA' AGGIORNATE; Solo sul NOSTRO sito WWW.DANIGOMEUSATE.COM con tutte le foto delle gomme, sempre aggiornato con tutti i pneumatici usati disponibili al momento, con foto,prezzo,marca ed altre info DOT 17 SPEDIZIONE GRATUITA GOMME USATE ESTIVE -Pneumatici Usati Controllati e Garantiti -TOP qualità fino al 99% -ACQUISTA sul nostro sito DANIGOMMEUSATE.COM subito per TE SPEDIZIONE GRATIS Chiamaci e ordina le tue gomme -Cell e WhatsApp 339-49.11.259 TANTE ALTRE DISPONBIILITA' DI GOMME ESTIVE, GOMME INVERNALI, GOMME PER FURGONE TRASPORTO LEGGERE, delle migliori marche",
//                850.99,
//                0,
//                true,
//                "ACCESSORI AUTO"
//            ).salvaAnnuncioSuFirebase()
//        }

        //RecyclerView

        //getting the recyclerView by its id
        val recyclerVu = findViewById<RecyclerView>(R.id.recyclerview)

        //this creates a vertical layout Manager
        recyclerVu.layoutManager = LinearLayoutManager(this)

        //ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        //This loop will create as many Views as documents containing
        //the image with title and price of object
        for (key in myAnnunci.keys){
            data.add(ItemsViewModel(myAnnunci[key]?.annuncioId, R.drawable.ic_launcher_background, "${myAnnunci[key]?.getTitolo()}", myAnnunci[key]?.getPrezzo(), auth.currentUser?.email/*, myAnnunci[key]?.getNTel()*/) )
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

        priceSlider.addOnChangeListener { /*slider, value, fromUser*/ _, _, _ ->
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
    suspend fun recuperaTuttiAnnunci(): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Recupera gli annunci che contengono una sequernza/sottosequenza nel titolo del annuncio.
    suspend fun recuperaAnnunciTitolo(nomeAnnuncio: String): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereEqualTo("titolo", nomeAnnuncio)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite inferiore
    suspend fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereLessThan("prezzo", prezzoMinore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite superiore
    suspend fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereGreaterThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    // Fissano un range in cui l'annuncio deve essere maggiore del prezzo minore e minore del prezzo superiore.
    suspend fun recuperaAnnunciPrezzoRange(prezzoMinore: Int, prezzoSuperiore: Int): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereGreaterThan("prezzo", prezzoMinore)
            .whereLessThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }


    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    suspend fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        var myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //--- Da testare ---
    suspend fun recuperaAnnunciLocalizzazione(posizioneUtente: Location, distanzaMax: Int): HashMap<String, Annuncio> {

        //val myCollection = this.database.collection("annunci");

        var myHashMap = recuperaTuttiAnnunci()

        var myAnnunci = HashMap<String,Annuncio>()

        for((key, value) in myHashMap){
            if(value.distanzaMinore(posizioneUtente,distanzaMax))
                myAnnunci[key] = value
        }

        return myAnnunci
    }
    //--- ----

    suspend fun subscribeRealTimeDatabase() {

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                val userIdAcquirente: String? = myDocumentoAnnuncio.document.get("userIdAcquirente") as String?

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
                    myDocumentoAnnuncio.document.id //as String
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
                myDocumentoAnnuncio.get("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.get("categoria") as String,
                userIdAcquirente,
                myDocumentoAnnuncio.id //as String
            );

            myAnnunci[myDocumentoAnnuncio.id] = a
        }
        return myAnnunci
    }

}

// TODO Handle the back button event
//    override fun onBackPressed() {
//        Toast.makeText(this, "Arrivederci!", Toast.LENGTH_LONG).show()
//    }

//    val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//        //...
//    }

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
