package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


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

        //Da cambiare, bisogna ripescarlo!
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

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    //Sospendo il metodo, per aspettare che la lista dei documenti sia stata recuperata e insirita nel arrayList
    public suspend fun recuperaTuttiAnnunci(): HashMap<String, Annuncio> {

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
    public suspend fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereLessThan("prezzo", prezzoMinore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite superiore
    public suspend fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereGreaterThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    // Fissano un range in cui l'annuncio deve essere maggiore del prezzo minore e minore del prezzo superiore.
    public suspend fun recuperaAnnunciPrezzoRange(prezzoMinore: Int, prezzoSuperiore: Int): HashMap<String, Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereGreaterThan("prezzo", prezzoMinore)
            .whereLessThan("prezzo", prezzoSuperiore)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }


    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    public suspend fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean): HashMap<String, Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        query = myCollection.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        var myDocumenti = query.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //--- Da testare ---
    public suspend fun recuperaAnnunciLocalizzazione(posizioneUtente: Location, distanzaMax: Int): HashMap<String, Annuncio> {

        val myCollection = this.database.collection("annunci");

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
                    myDocumentoAnnuncio.get("disponibilitaSpedire") as Boolean,
                    myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                    myDocumentoAnnuncio.get("categoria") as String,
                    userIdAcquirente,
                    myDocumentoAnnuncio.id as String
                );

                myAnnunci[myDocumentoAnnuncio.id] = a
            }
            return myAnnunci
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
