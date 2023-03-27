package it.uniupo.oggettiusati

import android.location.Location
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class Annuncio(

    //Informazioni del proprietario che vuole creare annuncio.
    private var userId: String,

    //Titolo Annuncio
    private var titolo: String,

    //Descrizione Annuncio
    private var descrizione: String,
    
    //Prezzo della vendita
    private var prezzo: Double,

    // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
    private var stato: Int,

    //false = No, true = Si
    private var disponibilitaSpedire: Boolean,

    //Categoria del annuncio: Es.  libri/libriPerBambini
    private var categoria: String,

    //Viene utilizzata, per rappresentare la posizione geografica, metodi che mi gestiscono la posizione come latitudine e longitudine
    //Parametro NON obbligatorio, per il costruttore secondario.
    private var posizione: Location = Location("provider")
){

    //collegamento con il mio database, variabile statica.
    companion object {
        public var database = Firebase.firestore
    }

    public lateinit var annuncioId: String

    /*
    constructor(userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean, categoria: String, latitude:Double, longitude: Double ) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.posizione.latitude = latitude
        this.posizione.longitude = longitude
    }
*/
    //Costruttore secondario, utile dopo che abbiamo letto un annuncio, andiamo a definire un suo oggetto.
    constructor( titolo: String, categoria: String, descrizione: String, stato: Int, disponibilitaSpedire: Boolean, posizione: GeoPoint,prezzo: Double,  userId: String, annuncioId: String) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.annuncioId = annuncioId

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
    }

    //Funzione che mi permette di scrivere sul cloud, FireBase, i dati del singolo annuncio, passo anche la posizione dell'immagine che voglio caricare sul cloud.
    //public fun salvaAnnuncioSuFirebase(immagineUri: Uri){
    public suspend fun salvaAnnuncioSuFirebase(){

            val geo = GeoPoint(posizione.latitude,posizione.longitude)

            val annuncio = hashMapOf(
                "userId" to userId,
                "titolo" to titolo,
                "descrizione" to descrizione,
                "prezzo" to prezzo,
                "stato" to stato,
                "disponibilitaSpedire" to disponibilitaSpedire,
                "categoria" to categoria,
                "posizione" to geo,
                "userIdAcquirente" to null
            )

            val myCollection = Annuncio.database.collection("annunci")

            //Log.d("DEBUG","Prima")

            val myDocument = myCollection.add(annuncio).await()

            //Log.d("DEBUG", "Dopo")

            this.annuncioId = myDocument.id

            Log.d("SALVA ANNUNCIO SU FIREBASE", annuncioId)

    }

    private suspend fun modificaAnnuncioSuFirebase(){

        val adRif = database.collection("annunci").document(this.annuncioId)

        adRif.update("userId",userId,"titolo",titolo,"descrizione",descrizione,"prezzo",prezzo,"stato",stato,"disponibilitaSpedire",disponibilitaSpedire,"categoria",categoria).await()


        /*

        adRif.update("userId",userId,"titolo",titolo,"descrizione",descrizione,"prezzo",prezzo,"stato",stato,"disponibilitaSpedire",disponibilitaSpedire,"categoria",categoria)
            .addOnSuccessListener {
                Log.d("Modifica annuncio", "Annuncio modificato con successo")
            }
            .addOnFailureListener { e ->
                Log.w("Modifica annuncio", "Errore nella modifica dell'annuncio", e)
            }

         */
    }

    public suspend fun eliminaAnnuncioDaFirebase(){

        val myCollection = database.collection("annunci")

        val myDocument = myCollection.document(this.annuncioId)

        myDocument.delete().await()
    }

    private fun caricaImmagineSuFirebase(immagineUri: Uri){

        val storage = FirebaseStorage.getInstance()

        var storageRef = storage.reference

        val cartella = storageRef.child(annuncioId)

        //Utilizzo il randomizzatore per generare un valore pseudocasuale, dedotto dal tempo, questo valore lo converto in una stringa. Questo valore sará associato al immagine.
        val immagineRef = cartella.child( Random(System.currentTimeMillis()).toString())

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

    //Metodo che in base se l'oggetto é venduto o no, segna o no l'utente che l'ha acquistato.
    public fun setVenduto(userIdAcquirente: String){

        val documentoRif = database.collection("annunci").document(this.annuncioId)

        //se effettivamente, il documento contiene null vorrá dire che non é stato acquistato da nessun altra persona, quindi posso effettuare upgrade.
        if (isVenduto()) {
            documentoRif.update("userIdAcquirente", userIdAcquirente)
                .addOnSuccessListener { Log.d("Recupero documento", "Il documento é stato aggiornato, é stato segnato acquirente.") }
                .addOnFailureListener { e -> Log.e("Recupero documento", "Il documento non é stato aggiornato",e)}
        }
        else{
            Log.d("Recupero documento", "Il documento esiste, é giá stato associato un acquirente.")
        }
    }

    //Metodo che ritorna il valore true o false in base a se l'oggetto associato al annuncio é stato venduto oppure no.
    public fun isVenduto(): Boolean {

        //Definisce un riferimento con il documento
        val documentoRif = database.collection("annunci").document(this.annuncioId)

        var risultato = false

        //recupero le proprietá del documento, dal cloud.
        documentoRif.get()
            .addOnSuccessListener { document ->
                //controllo se effettivamente esiste il documento, sul db.
                if (document != null) {

                    Log.d("Recupero documento", "Il documento esiste")

                    //se effettivamente, il documento contiene null vorrá dire che non é stato acquistato da nessun altra persona, quindi posso effettuare upgrade.
                    risultato = document["userIdAcquirente"] == null
                }else{
                    Log.d("Recupero documento", "Il documento non esiste")

                    risultato = false
                }
            }

        return risultato
    }

    public suspend fun setTitolo(newTitolo:String){
        this.titolo = newTitolo

        modificaAnnuncioSuFirebase()
    }

    public suspend fun setDescrizione(newDescrizione:String){
        this.descrizione = newDescrizione

        modificaAnnuncioSuFirebase()
    }

    public suspend fun setCategoria(newCategoria:String){
        this.categoria = newCategoria

        modificaAnnuncioSuFirebase()
    }

    public suspend fun setPrezzo(newPrezzo: Double){
        this.prezzo = newPrezzo

        modificaAnnuncioSuFirebase()
    }

    override fun toString(): String {
        return "Annuncio(userId='$userId', titolo='$titolo', descrizione='$descrizione', prezzo=$prezzo, stato=$stato, disponibilitaSpedire=$disponibilitaSpedire, categoria='$categoria', posizione=$posizione, annuncioId='$annuncioId')"
    }

}