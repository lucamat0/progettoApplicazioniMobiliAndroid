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
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserLoginActivity : AppCompatActivity()  {

    private lateinit var auth: FirebaseAuth
    private  lateinit var database: FirebaseFirestore

    //Indica id dell'utente loggato
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

        auth = FirebaseAuth.getInstance()
        database = Firebase.firestore

        //--- Tolto per prova il funzionamento dei test ---

        //utilizzato per recuperare i parametri
        val extras = intent.extras

        //Da cambiare, bisogna ripescarlo!
        userId = extras?.getString("userId").toString()

        lateinit var username : String
        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if(document != null){
                username = document.get("nome").toString()
            } else {
                Log.w("document error","Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }

        // -- Test funzionamento metodi nella classe annuncio --
/*
        val newAnnuncio = Annuncio(userId, "Mr Robot: Season 1 Blu-Ray + Digital HD", "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.", 16.99, 2, true, "filmETv/serieTv")
        newAnnuncio.toString()
        newAnnuncio.salvaAnnuncioSuFirebase(database)


        val buttonModifica = findViewById<Button>(R.id.modifica)

        val buttonElimina = findViewById<Button>(R.id.elimina)

        buttonModifica.setOnClickListener{

            newAnnuncio.titolo = "Mr Robot: Season 2 Blu-Ray + Digital HD"

            newAnnuncio.modificaAnnuncioSuFirebase(database)
        }

        buttonElimina.setOnClickListener{
            newAnnuncio.eliminaAnnuncioDaFirebase(database)
        }
*/
        // -- Fine Test funzionamento metodi nella classe annuncio --

        /*
        var userId = "asasas"

        val buttonSeleziona = findViewById<Button>(R.id.pickUpImg)

        buttonSeleziona.setOnClickListener{

            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
        }
        */
    }

    //Sospendo il metodo, per aspettare che la lista dei documenti sia stata recuperata e insirita nel arrayList
    public suspend fun recuperaTuttiAnnunci(): ArrayList<Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Recupera gli annunci che contengono una sequernza/sottosequenza nel titolo del annuncio.
    suspend fun recuperaAnnunciTitolo(nomeAnnuncio: String): ArrayList<Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.whereEqualTo("titolo",nomeAnnuncio).get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite inferiore
    public suspend fun recuperaAnnunciPrezzoInferiore(prezzoMinore: Int): ArrayList<Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.whereLessThan("prezzo",prezzoMinore).get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Fissano un limite superiore
    public suspend fun recuperaAnnunciPrezzoSuperiore(prezzoSuperiore: Int): ArrayList<Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.whereGreaterThan("prezzo",prezzoSuperiore).get().await()

        return recuperaAnnunci(myDocumenti);
    }

    // Fissano un range in cui l'annuncio deve essere maggiore del prezzo minore e minore del prezzo superiore.
    public suspend fun recuperaAnnunciPrezzoRange(prezzoMinore: Int, prezzoSuperiore: Int): ArrayList<Annuncio> {

        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.whereGreaterThan("prezzo",prezzoMinore).whereLessThan("prezzo",prezzoSuperiore).get().await()

        return recuperaAnnunci(myDocumenti);
    }

    //Ritorna gli annunci che rispettano la disponibilitá di spedire.
    public suspend fun recuperaAnnunciDisponibilitaSpedire(disponibilitaSpedire: Boolean): ArrayList<Annuncio> {
        //Ritorno una referenza alla collezzione contenente i miei documenti.
        val myCollection = this.database.collection("annunci");

        //Recupero la collezione contenente tutti gli elementi, ossia gli annunci.
        val myDocumenti = myCollection.whereEqualTo("disponibilitaSpedire",disponibilitaSpedire).get().await()

        return recuperaAnnunci(myDocumenti);
    }


    private suspend fun recuperaAnnunciLocalizzazione(){

    }

    //In base alla query che viene passata, questa funzione mi filtra gli annunci e mi ritorna un arrayList di annunci.
    private fun recuperaAnnunci(myDocumenti: QuerySnapshot): ArrayList<Annuncio> {

        //Inizializzo l'array vuoto, che sucessivamente dovró restituire.
        val myAnnunci = ArrayList<Annuncio>()

        for (myDocumentoAnnuncio in myDocumenti.documents) {

            //Creazione del oggetto Annuncio, con gli elementi che si trovano sul DB
            var a = Annuncio(
                myDocumentoAnnuncio.get("titolo") as String,
                myDocumentoAnnuncio.get("categoria") as String,
                myDocumentoAnnuncio.get("descrizione") as String,
                (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
                myDocumentoAnnuncio.get("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.get("prezzo") as Double,
                myDocumentoAnnuncio.get("userId") as String,
                myDocumentoAnnuncio.id as String
            );

            myAnnunci.add(a)
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
