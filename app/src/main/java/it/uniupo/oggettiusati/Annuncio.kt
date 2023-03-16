package it.uniupo.oggettiusati

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

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
    private var categoria: String,

    //Posizione immagine
    private var immagineUri: Uri

    //Localizzazione geografica ??? Immagini ???
){

    private lateinit var annuncioId: String

    //Costruttore secondario, utile dopo che abbiamo letto un annuncio, andiamo a definire un suo oggetto.
    constructor(userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean, categoria: String, annuncioId: String, immagineUri: Uri) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria, immagineUri) {
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

                    caricaImmagineSuFirebase();

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

    private fun caricaImmagineSuFirebase(){

        val storage = FirebaseStorage.getInstance()

        var storageRef = storage.reference

        val cartella = storageRef.child(annuncioId)

        //Data e ora di caricamento, da implementare per assegnare un id

        val immagineRef = cartella.child("prova")

        // Carica l'immagine sul bucket di archiviazione Firebase
        val uploadTask = immagineRef.putFile(immagineUri)

        // Aggiungi un listener per controllare il progresso del caricamento
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            Log.d("Caricamento immagine", "Caricamento in corso: $progress% completato")
        }.addOnPausedListener {
            Log.d("Caricamento immagine", "Caricamento in pausa")
        }.addOnSuccessListener {
            Log.d("Caricamento immagine", "Caricamento completato: ${it.metadata?.path}")

            Log.d("Creazione annuncio", "Annuncio creato con successo")
        }.addOnFailureListener {
            Log.e("Caricamento immagine", "Errore durante il caricamento dell'immagine", it)
        }

    }

}