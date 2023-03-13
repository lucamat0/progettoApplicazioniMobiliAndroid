package it.uniupo.oggettiusati

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class Annuncio(

    //Informazioni del proprietario che vuole creare annuncio.
    private var userId: String,

    //Titolo Annuncio
    public var titolo: String,

    //Descrizione Annuncio
    public var descrizione : String,
    
    //Prezzo della vendita
    public var prezzo: Double,

    // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
    private var stato: Int,

    //false = No, true = Si
    private var disponibilitaSpedire: Boolean,

    //Categoria del annuncio: Es.  libri/libriPerBambini
    private var categoria: String


    //Localizzazione geografica ??? Immagini ???
){

    private lateinit var annuncioId: String

    //Costruttore secondario, utile dopo che abbiamo letto un annuncio, andiamo a definire un suo oggetto.
    constructor(userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean, categoria: String, annuncioId: String) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.annuncioId = annuncioId
    }

    //Funzione che mi permette di scrivere sul cloud, FireBase, i dati del singolo annuncio.
    public fun salvaAnnuncioSuFirebase(database: FirebaseFirestore){

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

                    documentReference ->  annuncioId = documentReference.id

                    Log.d("Creazione annuncio", "Annuncio creato con successo")
                }
                .addOnFailureListener { e ->
                    Log.w("Creazione annuncio", "Errore durante la creazione dell'annuncio", e)
                }
        }

    public fun modificaAnnuncioSuFirebase(database: FirebaseFirestore){

        val adRif = database.collection("annunci").document(this.annuncioId)

        adRif.update("userId",userId,"titolo",titolo,"descrizione",descrizione,"prezzo",prezzo,"stato",stato,"disponibilitaSpedire",disponibilitaSpedire,"categoria",categoria)
            .addOnSuccessListener {
                Log.d("Modifica annuncio", "Annuncio modificato con successo")
            }
            .addOnFailureListener { e ->
                Log.w("Modifica annuncio", "Errore nella modifica dell'annuncio", e)
            }
    }

    public fun eliminaAnnuncioDaFirebase(database: FirebaseFirestore){

        val adRif = database.collection("annunci").document(this.annuncioId)

        adRif.delete()
            .addOnSuccessListener {
                Log.d("Elimina annuncio", "Annuncio eliminato con successo")
            }
            .addOnFailureListener { e ->
                Log.w("Elimina annuncio", "Errore durante l'eliminazione dell'annuncio", e)
            }
    }

}