package it.uniupo.oggettiusati

import android.location.Location
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.lang.Math.*
import kotlin.random.Random

data class Annuncio(

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
): Parcelable {

    //collegamento con il mio database, variabile statica.
    companion object {
        val nomeCollection = "annuncio"

        @JvmField val CREATOR = object : Parcelable.Creator<Annuncio> {
            override fun createFromParcel(parcel: Parcel): Annuncio {
                return Annuncio(parcel)
            }

            override fun newArray(size: Int): Array<Annuncio?> {
                return arrayOfNulls(size)
            }
        }
    }

    //--- Inizio variabili utili all'inserimento delle immagini sul cloud ---
    val storage = FirebaseStorage.getInstance()

    private lateinit var storageRef : StorageReference
    //--- Fine variabili utili all'inserimento delle immagini sul cloud ---

    private val database = Firebase.firestore

    private lateinit var annuncioId: String

    private var userIdAcquirente: String? = null

    private var timeStampInizioVendita: Long? = null

    private var timeStampFineVendita: Long? = null

        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readParcelable(Location::class.java.classLoader) ?: Location("")
        ) {
            annuncioId = parcel.readString() ?: ""
            userIdAcquirente = parcel.readString()
            timeStampInizioVendita = parcel.readLong()
            timeStampFineVendita = parcel.readLong()
        }

    constructor(
        userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean,
        categoria: String, posizione: GeoPoint, timeStampInizioVendita: Long, timeStampFineVendita: Long?, userIdAcquirente: String?, annuncioId: String) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.annuncioId = annuncioId

        this.userIdAcquirente = userIdAcquirente
        this.timeStampInizioVendita = timeStampInizioVendita
        this.timeStampFineVendita = timeStampFineVendita

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
    }

    constructor(
        userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean,
        categoria: String, posizione: Location, timeStampInizioVendita: Long, timeStampFineVendita: Long?, userIdAcquirente: String?) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {

        this.userIdAcquirente = userIdAcquirente
        this.timeStampInizioVendita = timeStampInizioVendita
        this.timeStampFineVendita = timeStampFineVendita

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
    }

    //Funzione che mi permette di scrivere sul cloud, FireBase, i dati del singolo annuncio, passo anche la posizione dell'immagine che voglio caricare sul cloud.
    suspend fun salvaAnnuncioSuFirebase(myImmagini: ArrayList<Uri>?){
    //public suspend fun salvaAnnuncioSuFirebase(){

            val geo = GeoPoint(posizione.latitude,posizione.longitude)

            this.timeStampInizioVendita = System.currentTimeMillis()

            val annuncio = hashMapOf(
                "userId" to userId,
                "titolo" to titolo,
                "descrizione" to descrizione,
                "prezzo" to prezzo,
                "stato" to stato,
                "disponibilitaSpedire" to disponibilitaSpedire,
                "categoria" to categoria,
                "posizione" to geo,
                "timeStampInizioVendita" to timeStampInizioVendita,
                "timeStampFineVendita" to timeStampFineVendita,
                "userIdAcquirente" to userIdAcquirente
            )

            val myCollection = database.collection(nomeCollection)

            //Log.d("DEBUG","Prima")

            val myDocument = myCollection.add(annuncio).await()

            //Log.d("DEBUG", "Dopo")

            this.annuncioId = myDocument.id
            this.storageRef = storage.reference.child(annuncioId)

            Log.d("SALVA ANNUNCIO SU FIREBASE", annuncioId)

            if(myImmagini != null)
                caricaImmaginiSuFirebase(myImmagini)
    }

    private suspend fun modificaAnnuncioSuFirebase(){

        val adRif = database.collection(nomeCollection).document(this.annuncioId)

        adRif.update("userId",userId,"titolo",titolo,"descrizione",descrizione,"prezzo",prezzo,"stato",stato,"disponibilitaSpedire",disponibilitaSpedire,"categoria",categoria).await()
    }

    suspend fun eliminaAnnuncioDaFirebase(){

        val myCollection = database.collection(nomeCollection)

        val myDocument = myCollection.document(this.annuncioId)

        myDocument.delete().await()
    }

    private fun caricaImmaginiSuFirebase(myImmagini: ArrayList<Uri>){

        for(immagineUri in myImmagini) {

            //Utilizzo il randomizzatore per generare un valore pseudocasuale, dedotto dal tempo, questo valore lo converto in una stringa. Questo valore sará associato al immagine.
            val immagineRef = storageRef.child(Random(System.currentTimeMillis()).toString())

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

    public suspend fun recuperaImmaginiSuFirebase(): ArrayList<Uri> {

        var myListImmaginiRef = storageRef.listAll().await()

        val myImmagini = ArrayList<Uri>()

        for(item in myListImmaginiRef.items){
            myImmagini.add(item.downloadUrl.await())
        }

        return myImmagini
    }

    suspend fun setVenduto(userIdAcquirente: String){
        if(this.userIdAcquirente == null){
            this.userIdAcquirente = userIdAcquirente

            modificaAnnuncioSuFirebase()
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

    fun getPrezzo(): Double {
        return prezzo
    }

    fun getTimeStampInizioVendita(): Long? {
        return timeStampInizioVendita
    }

    fun isVenduto(): Boolean{
        return userIdAcquirente != null
    }

    fun getAnnuncioId(): String {
        return annuncioId
    }

    fun getTitolo(): String {
        return titolo
    }

    //Metodo che mi permette di tradurre la distanza in Km, date due coordinate composte da longitudine e latitudine
    private fun distanzaInKm(posizioneUtente: Location): Double {

        val lat1 = this.posizione.latitude
        val lon1 = this.posizione.longitude

        val lat2 = posizioneUtente.latitude
        val lon2 = posizioneUtente.longitude

        val theta = lon1 - lon2

        var dist = sin(toRadians(lat1)) * sin(toRadians(lat2)) + cos(toRadians(lat1)) * cos(toRadians(lat2)) * cos(toRadians(theta))
        dist = acos(dist)
        dist = toDegrees(dist)
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

    fun getDisponibilitaSpedire(): Boolean {
        return disponibilitaSpedire
    }

    fun getStato(): Int {
        return stato
    }

    fun getDescrizione(): String {
        return descrizione
    }

    fun getCategoria(): String {
        return categoria
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(titolo)
        parcel.writeString(descrizione)
        parcel.writeDouble(prezzo)
        parcel.writeInt(stato)
        parcel.writeByte(if (disponibilitaSpedire) 1 else 0)
        parcel.writeString(categoria)
        parcel.writeParcelable(posizione, flags)
        parcel.writeString(annuncioId)
        parcel.writeString(userIdAcquirente)
        parcel.writeValue(timeStampInizioVendita)
        parcel.writeValue(timeStampFineVendita)
    }

    override fun describeContents(): Int {
        return 0
    }
}
