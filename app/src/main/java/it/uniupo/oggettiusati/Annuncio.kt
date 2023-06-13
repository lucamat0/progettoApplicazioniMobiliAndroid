package it.uniupo.oggettiusati


import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

val database = Firebase.firestore

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
) : Parcelable {

    //collegamento con il mio database, variabile statica.
    companion object {
        val nomeCollection = "annuncio"

        @JvmField val CREATOR = object : Parcelable.Creator<Annuncio> {
            @RequiresApi(Build.VERSION_CODES.Q)
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

    lateinit var storageRef: StorageReference
    //--- Fine variabili utili all'inserimento delle immagini sul cloud ---

    private lateinit var annuncioId: String

    private var userIdAcquirente: String? = null

    private var timeStampInizioVendita: Long? = null

    private var timeStampFineVendita: Long? = null

    private var venduto: Boolean = false

    private var recensito: Boolean = false

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readParcelable(Location::class.java.classLoader) ?: Location(""),
    ) {
        this.annuncioId = parcel.readString() ?: ""
        this.userIdAcquirente = parcel.readString()
        this.timeStampInizioVendita = parcel.readLong()
        this.venduto = parcel.readBoolean()
        this.recensito = parcel.readBoolean()
    }

    //--- Costruttore secondario utilizzato per inizializzare gli annunci, una volta che sono stati scaricati da FirebaseFirestore ---
    constructor(
        userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean,
        categoria: String, posizione: GeoPoint, timeStampInizioVendita: Long, timeStampFineVendita: Long?, userIdAcquirente: String?, annuncioId: String, venduto: Boolean, recensito: Boolean) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {
        this.annuncioId = annuncioId

        this.userIdAcquirente = userIdAcquirente
        this.timeStampInizioVendita = timeStampInizioVendita
        this.timeStampFineVendita = timeStampFineVendita
        this.venduto = venduto
        this.recensito = recensito

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
        this.storageRef = storage.reference.child(annuncioId)
    }

    //--- Costruttore secondario utilizzato per inizializzare gli annunci nei test ---
    constructor(
        userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean,
        categoria: String, posizione: Location, timeStampInizioVendita: Long, timeStampFineVendita: Long?, userIdAcquirente: String?,venduto: Boolean, recensito: Boolean) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria) {

        this.userIdAcquirente = userIdAcquirente
        this.timeStampInizioVendita = timeStampInizioVendita
        this.timeStampFineVendita = timeStampFineVendita

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
        this.venduto = venduto
        this.recensito = recensito
    }

    //Funzione che mi permette di scrivere sul cloud, FireBase, i dati del singolo annuncio, passo anche la posizione dell'immagine che voglio caricare sul cloud.
    suspend fun salvaAnnuncioSuFirebase(myImmagini: ArrayList<Uri>?) {
    //public suspend fun salvaAnnuncioSuFirebase() {

            val geo = GeoPoint(posizione.latitude, posizione.longitude)

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
                "userIdAcquirente" to userIdAcquirente,
                "venduto" to false,
                "recensito" to false
            )

            val myCollection = database.collection(nomeCollection)

            //Log.d("DEBUG", "Prima")

            val myDocument = myCollection.add(annuncio).await()

            //Log.d("DEBUG", "Dopo")

            this.annuncioId = myDocument.id
            this.storageRef = storage.reference.child(annuncioId + "\\")

            Log.d("SALVA ANNUNCIO SU FIREBASE", annuncioId)

            if(myImmagini != null)
                caricaImmaginiSuFirebase(myImmagini)
    }

    private suspend fun modificaAnnuncioSuFirebase() {

        val adRif = database.collection(nomeCollection).document(this.annuncioId)

        adRif.update("userId", userId, "titolo", titolo, "descrizione", descrizione, "prezzo", prezzo, "stato", stato, "disponibilitaSpedire", disponibilitaSpedire, "categoria", categoria, "venduto", venduto, "userIdAcquirente", userIdAcquirente, "timeStampFineVendita", timeStampFineVendita, "recensito", recensito).await()
    }

    suspend fun eliminaAnnuncioDaFirebase() {

        val myCollection = database.collection(nomeCollection)

        val myDocument = myCollection.document(this.annuncioId)

        myDocument.delete().await()
    }

    private fun caricaImmaginiSuFirebase(myImmagini: ArrayList<Uri>) {

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


    suspend fun recuperaImmaginiSuFirebase(): ArrayList<Uri> {

        val myListImmaginiRef = storageRef.listAll().await()

        val myImmagini = ArrayList<Uri>()

        for(item in myListImmaginiRef.items) {
            myImmagini.add(item.downloadUrl.await())
        }

        return myImmagini
    }

    suspend fun setVenduto() {
        //aggiunta di un nuovo campo booleano che viene settato a true quando acquirente ha dato ok
        if(this.userIdAcquirente != null /*&& this.venduto == false*/){
            this.venduto = true
            this.timeStampFineVendita = System.currentTimeMillis()

            modificaAnnuncioSuFirebase()
        }
        //Errore, dove si informa che non è stata avanzata nessuna richiesta
    }

    suspend fun setRichiesta(userIdAcquirente: String){
        if(this.userIdAcquirente == null) {
            this.userIdAcquirente = userIdAcquirente

            modificaAnnuncioSuFirebase()
        }
        else{
            //richiesta già inoltrata, da qualcunaltro, comunicalo e rimuovilo dal carrello
            CartFragment.eliminaAnnuncioCarrelloFirebaseFirestore(userIdAcquirente, this.annuncioId)
        }
    }

    //ti controlla se è stata effettuata la richiesta da qualcuno
    fun getRichiesta(): Boolean{
        return this.userIdAcquirente != null
    }

    fun getProprietario() :String {
        return this.userId
    }

    //ti controlla se userId è del proprietario del annuncio
    fun isProprietario(userId: String): Boolean{
        return userId == this.userId
    }

    fun isAcquirente(userId: String): Boolean{
        return this.userIdAcquirente == userId
    }

    fun isVenduto(): Boolean {
        return this.userIdAcquirente != null && this.venduto
    }

    //ti comunica se e' stata inserita una recensione dopo acquisto del prodotto
    fun getRecensito(): Boolean{
        return this.recensito
    }

    //setRichiesta prende in input userId, si controlla che sia quella del acquirente, per un maggiore livello di sicurezza
    suspend fun setRecensito(userId: String){
        if(userIdAcquirente == userId){ //no, anche l'acquirente puo' recensire il venditore
            recensito = true
            modificaAnnuncioSuFirebase()
        }
    }

    suspend fun setEliminaRichiesta(userId: String){
        if(this.userIdAcquirente != null && isProprietario(userId)){

            //Nel momento in cui il proprietario non accetta la vendita, il credito viene riaccreditato al utente.
            CartFragment.salvaTransazioneSuFirestoreFirebase(this.userIdAcquirente!!, this.prezzo, true)

            CartFragment.eliminaAnnuncioCarrelloFirebaseFirestore(this.userIdAcquirente!!,this.annuncioId)

            this.userIdAcquirente = null

            modificaAnnuncioSuFirebase()
        }
        //non sono il proprietario o/e non c'è stata nessuna richiesta
    }

    suspend fun setTitolo(newTitolo: String, userId: String) {
        if(isProprietario(userId)) {
            this.titolo = newTitolo

            modificaAnnuncioSuFirebase()
        }
    }

    suspend fun setDescrizione(newDescrizione: String, userId: String) {
        if(isProprietario(userId)) {
            this.descrizione = newDescrizione

            modificaAnnuncioSuFirebase()
        }
    }

    suspend fun setCategoria(newCategoria: String, userId: String) {
        if(isProprietario(userId)) {
            this.categoria = newCategoria

            modificaAnnuncioSuFirebase()
        }
    }

    suspend fun setPrezzo(newPrezzo: Double, userId: String) {
        if(isProprietario(userId)) {
            this.prezzo = newPrezzo

            modificaAnnuncioSuFirebase()
        }
    }

    fun getPrezzo(): Double{
        return prezzo
    }
    fun getPrezzoToString(): String {
        return String.format("%.2f", prezzo) + "€"
    }

    fun getTimeStampInizioVendita(): Long? {
        return timeStampInizioVendita
    }

    fun getAnnuncioId(): String {
        return annuncioId
    }

    fun getTitolo(): String {
        return titolo
    }

    fun getAcquirente(): String?{
        return this.userIdAcquirente
    }

    //Mi ritorna true se l'annuncio, ha una distanza inferiore a quella massima.
    fun distanzaMinore(posizioneUtente: Location, distanzaMaxKm: Int): Boolean {

        //Log.d("Posizione", posizioneUtente.distanceTo(this.posizione).toString())
        return posizioneUtente.distanceTo(this.posizione) <= distanzaMaxKm*1000
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
        return this.categoria
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
        parcel.writeValue(venduto)
        parcel.writeValue(recensito)
    }

    override fun describeContents(): Int {
        return 0
    }
}
