package it.uniupo.oggettiusati

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity()  {

    private lateinit var auth: FirebaseAuth
    private  lateinit var database: FirebaseFirestore

    //Indica id dell'utente loggato
    private var userId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged)

        auth = FirebaseAuth.getInstance();
        database = Firebase.firestore

        //Da cambiare, bisogna ripescarlo!
        userId = 0;

/*
        // -- Inserimento di un annuncio --

        val newAnnuncio = Annuncio(userId, "Mr Robot: Season 1 Blu-Ray + Digital HD", "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.", 16.99, 2, true, "filmETv/serieTv");
        newAnnuncio.toString();
        newAnnuncio.salvaAnnuncioSuFirebase(database);

        //-- Fine inserimento di un annuncio --
 */
    }

    //Classe utilizzata specialmente per rappresentare dati. All'interno di questa classe possiamo trovare, in automatico i metodi: toString(), equals(), hashCode(), toString(), copy().
    data class Annuncio(

        //Informazioni del proprietario che vuole creare annuncio.
        var userId: Int,

        //Titolo Annuncio
        var titolo: String,

        //Descrizione Annuncio
        var descrizione : String,

        //Prezzo della vendita
        var prezzo: Double,

        // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
        var stato: Int,

        //false = No, true = Si
        var disponibilitaSpedire: Boolean = false,

        //Categoria del annuncio: Es.  libri/libriPerBambini
        var categoria: String

        //Localizzazione geografica ??? Immagini ???
    ){
        fun salvaAnnuncioSuFirebase(database: FirebaseFirestore){

            val annuncio = hashMapOf(
                "userId" to this.userId,
                "titolo" to this.titolo,
                "descrizione" to this.descrizione,
                "prezzo" to this.prezzo,
                "stato" to this.stato,
                "disponibilitaSpedire" to this.disponibilitaSpedire,
                "categoria" to this.categoria
            )

            database.collection("annunci")
                .add(annuncio)
                .addOnSuccessListener {
                    Log.d("Creazione annuncio", "Annuncio creato con successo")
                }
                .addOnFailureListener { e ->
                    Log.w("Creazione annuncio", "Errore durante la creazione dell'annuncio", e)
                }
        }
    }

}