package it.uniupo.oggettiusati

import android.location.Location
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.lang.Math.*
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
        var database = Firebase.firestore
    }

    lateinit var annuncioId: String

    private var userIdAcquirente: String? = null

    constructor(userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean, posizione: GeoPoint, categoria: String, userIdAcquirente: String?, annuncioId: String) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.annuncioId = annuncioId
        this.userIdAcquirente = userIdAcquirente

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
    }

    //Funzione che mi permette di scrivere sul cloud, FireBase, i dati del singolo annuncio, passo anche la posizione dell'immagine che voglio caricare sul cloud.
    //fun salvaAnnuncioSuFirebase(immagineUri: Uri){
    suspend fun salvaAnnuncioSuFirebase(){

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
                "userIdAcquirente" to userIdAcquirente
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
    }

    suspend fun eliminaAnnuncioDaFirebase(){

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

    fun getTitolo() :String {
        return titolo
    }
    fun getPrezzo() :Double {
        return prezzo
    }

    suspend fun setVenduto(userIdAcquirente: String){
        if(this.userIdAcquirente == null){
            this.userIdAcquirente = userIdAcquirente

            salvaAnnuncioSuFirebase()
        }

    }

    suspend fun setTitolo(newTitolo:String){
        this.titolo = newTitolo

        modificaAnnuncioSuFirebase()
    }

    suspend fun setDescrizione(newDescrizione:String){
        this.descrizione = newDescrizione

        modificaAnnuncioSuFirebase()
    }

    suspend fun setCategoria(newCategoria:String){
        this.categoria = newCategoria

        modificaAnnuncioSuFirebase()
    }

    suspend fun setPrezzo(newPrezzo: Double){
        this.prezzo = newPrezzo

        modificaAnnuncioSuFirebase()
    }

    //Metodo che mi permette di tradurre la distanza in Km, date due coordinate composte da longitudine e latitudine
    private fun distanzaInKm(posizioneUtente: Location): Double {

        val lat1 = this.posizione.latitude
        val lon1 = this.posizione.longitude

        val lat2 = posizioneUtente.latitude
        val lon2 = posizioneUtente.longitude

        val theta = lon1 - lon2

        var dist = sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(Math.toRadians(theta))
        dist = acos(dist)
        dist = Math.toDegrees(dist)
        dist *= 60 * 1.1515
        dist *= 1.609344

        return dist
    }

    //Mi ritorna true se l'annuncio, ha una distanza inferiore a quella massima.
    fun distanzaMinore(posizioneUtente: Location, distanzaMax: Int): Boolean{
        return  distanzaInKm(posizioneUtente) <= distanzaMax
    }

    override fun toString(): String {
        return "Annuncio(userId='$userId', titolo='$titolo', descrizione='$descrizione', prezzo=$prezzo, stato=$stato, disponibilitaSpedire=$disponibilitaSpedire, categoria='$categoria', posizione=$posizione, annuncioId='$annuncioId')"
    }

}