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
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Rappresenta un Annuncio
 *
 * @author Amato Luca
 * @property userId Identificativo dell'utente che ha creato l'annuncio
 * @property titolo Titolo dell'annuncio
 * @property descrizione Descrizione dell'annuncio
 * @property prezzo Prezzo dell'annuncio
 * @property stato Stato dell'annuncio (0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo)
 * @property disponibilitaSpedire Indica se l'oggetto dell'annuncio è disponibile per la spedizione
 * @property categoria Categoria dell'annuncio
 * @property sottocategoria Sottocategoria dell'annuncio, parametro opzionale
 * @property posizione Posizione geografica dell'annuncio, parametro opzionale
 */
data class Annuncio(

    private var userId: String,

    private var titolo: String,

    private var descrizione: String,

    private var prezzo: Double,

    private var stato: Int,

    private var disponibilitaSpedire: Boolean,

    private var categoria: String,

    private var sottocategoria: String? = null,

    private var posizione: Location = Location("provider")
) : Parcelable {

    /**
     * @suppress
     */
    companion object {
        private val database = Firebase.firestore

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

    lateinit var storageRef: StorageReference

    private lateinit var annuncioId: String

    private var userIdAcquirente: String? = null

    private var timeStampInizioVendita: Long? = null

    private var timeStampFineVendita: Long? = null

    private var venduto: Boolean = false

    private var acquirenteRecensito: Boolean = false

    private var proprietarioRecensito: Boolean = false

    /**
     * Costruttore aggiuntivo che crea un oggetto Annuncio da un oggetto Parcel
     *
     * @author Amato Luca
     * @param parcel Oggetto Parcel contenente i dati dell'annuncio.
     */
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readBoolean(),
            parcel.readString() ?: "",
            parcel.readString() ?: null,
            parcel.readValue(Location::class.java.classLoader) as Location
    ) {
        this.annuncioId = parcel.readString() ?: ""
        this.userIdAcquirente = parcel.readString()
        this.timeStampInizioVendita = parcel.readLong()
        this.timeStampFineVendita = parcel.readValue(Long::class.java.classLoader) as Long?
        this.venduto = parcel.readBoolean()
        this.acquirenteRecensito = parcel.readBoolean()
        this.proprietarioRecensito = parcel.readBoolean()
    }

    /**
     * Costruttore secondario utilizzato per inizializzare gli annunci una volta che sono stati scaricati da Firebase
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente che ha creato l'annuncio
     * @param titolo Titolo dell'annuncio
     * @param descrizione Descrizione dell'annuncio
     * @param prezzo Prezzo dell'annuncio
     * @param stato Stato dell'annuncio (0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo)
     * @param disponibilitaSpedire Indica se l'annuncio è disponibile per la spedizione
     * @param categoria Categoria dell'annuncio
     * @param sottocategoria Sottocategoria dell'annuncio, parametro opzionale
     * @param posizione Posizione geografica dell'annuncio
     * @param timeStampInizioVendita Timestamp di inizio della vendita
     * @param timeStampFineVendita Timestamp di fine della vendita, parametro opzionale
     * @param userIdAcquirente Identificativo dell'utente acquirente, parametro opzionale
     * @param annuncioId Identificativo dell'annuncio
     * @param venduto Indica se l'annuncio è stato venduto oppure no
     * @param acquirenteRecensito Indica se l'acquirente ha lasciato una recensione al venditore
     * @param proprietarioRecensito Indica se il proprietario ha lasciato una recensione al acquirente
     */
    constructor(
        userId: String, titolo: String, descrizione: String, prezzo: Double, stato: Int, disponibilitaSpedire: Boolean,
        categoria: String, sottocategoria: String?, posizione: GeoPoint, timeStampInizioVendita: Long, timeStampFineVendita: Long?, userIdAcquirente: String?, annuncioId: String, venduto: Boolean, acquirenteRecensito: Boolean, proprietarioRecensito: Boolean) : this(userId, titolo, descrizione, prezzo, stato, disponibilitaSpedire, categoria, sottocategoria) {
        this.annuncioId = annuncioId

        this.userIdAcquirente = userIdAcquirente
        this.timeStampInizioVendita = timeStampInizioVendita
        this.timeStampFineVendita = timeStampFineVendita
        this.venduto = venduto
        this.acquirenteRecensito = acquirenteRecensito
        this.proprietarioRecensito = proprietarioRecensito

        this.posizione.latitude = posizione.latitude
        this.posizione.longitude = posizione.longitude
        this.storageRef = FirebaseStorage.getInstance().reference.child(annuncioId)
    }

    /**
     * Salva annuncio corrente su Firebase
     *
     * @author Amato Luca
     * @param myImmagini Lista di immagini associate all'annuncio, parametro opzionale.
     */
    suspend fun salvaAnnuncioSuFirebase(myImmagini: ArrayList<Uri>?) {

            val geo = GeoPoint(posizione.latitude, posizione.longitude)

            this.timeStampInizioVendita = System.currentTimeMillis()

            val annuncio = hashMapOf(
                "userId" to this.userId,
                "titolo" to this.titolo,
                "descrizione" to this.descrizione,
                "prezzo" to this.prezzo,
                "stato" to this.stato,
                "disponibilitaSpedire" to this.disponibilitaSpedire,
                "categoria" to this.categoria,
                "sottocategoria" to this.sottocategoria,
                "posizione" to geo,
                "timeStampInizioVendita" to this.timeStampInizioVendita,
                "timeStampFineVendita" to this.timeStampFineVendita,
                "userIdAcquirente" to this.userIdAcquirente,
                "venduto" to false,
                "acquirenteRecensito" to false,
                "proprietarioRecensito" to false
            )

            val myCollection = database.collection(nomeCollection)

            //Log.d("DEBUG", "Prima")

            val myDocument = myCollection.add(annuncio).await()

            //Log.d("DEBUG", "Dopo")

            this.annuncioId = myDocument.id
            this.storageRef = FirebaseStorage.getInstance().reference.child(this.annuncioId)

            Log.d("SALVA ANNUNCIO SU FIREBASE", this.annuncioId)

            if(myImmagini != null)
                caricaImmaginiSuFirebase(myImmagini)
    }

    /**
     * Aggiorna annuncio corrente su Firebase
     *
     * @author Amato Luca
     */
    private suspend fun modificaAnnuncioSuFirebase() {

        val adRif = database.collection(nomeCollection).document(this.annuncioId)

        adRif.update("userId", userId, "titolo", titolo, "descrizione", descrizione, "prezzo", prezzo, "stato", stato, "disponibilitaSpedire", disponibilitaSpedire, "categoria", categoria, "venduto", venduto, "userIdAcquirente", userIdAcquirente, "timeStampFineVendita", timeStampFineVendita, "acquirenteRecensito", this.acquirenteRecensito, "proprietarioRecensito", this.proprietarioRecensito, "sottocategoria", this.sottocategoria).await()
    }

    /**
     * Elimina annuncio corrente da Firebase
     *
     * @author Amato Luca
     */
    suspend fun eliminaAnnuncioDaFirebase() {

        val myCollection = database.collection(nomeCollection)

        val myDocument = myCollection.document(this.annuncioId)

        myDocument.delete().await()
    }

    /**
     * Carica le immagini specificate su Firebase Storage
     *
     * @author Amato Luca
     * @param myImmagini La lista delle URI delle immagini da caricare.
     */
    private fun caricaImmaginiSuFirebase(myImmagini: ArrayList<Uri>) {

        for(immagineUri in myImmagini) {

            val immagineRef = storageRef.child(File(immagineUri.path!!).name)

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

    /**
     * Recupera le immagini dell'annuncio corrente da Firebase Storage
     *
     * @author Amato Luca
     * @return Lista di URI delle immagini recuperate
     */
    suspend fun recuperaImmaginiSuFirebase(): ArrayList<Uri> {
        storageRef = FirebaseStorage.getInstance().reference.child(annuncioId)
        val myListImmaginiRef = storageRef.listAll().await()

        //Log.d("Mostra immagini: ${titolo} ",storageRef.name)

        val myImmagini = ArrayList<Uri>()

        for(item in myListImmaginiRef.items) {
            myImmagini.add(item.downloadUrl.await())
        }

        //Log.d("Mostra immagini: ${titolo} ",myImmagini.size.toString())

        return myImmagini
    }

    /**
     * Imposta lo stato dell'annuncio come "venduto" e aggiorna il timestamp di fine vendita.
     *
     * @author Amato Luca
     */
    suspend fun setVenduto() {
        //aggiunta di un nuovo campo booleano che viene settato a true quando acquirente ha dato ok
        if(this.userIdAcquirente != null /*&& this.venduto == false*/){
            this.venduto = true
            this.timeStampFineVendita = System.currentTimeMillis()

            modificaAnnuncioSuFirebase()
        }
        //Errore, dove si informa che non è stata avanzata nessuna richiesta
    }

    /**
     * Imposta identificativo dell'acquirente che ha fatto la richiesta di acquisto, elimina dal carrello Annuncio nel caso in cui qualcunaltro ha provato a inoltrare la richiesta di acquisto
     *
     * @author Amato Luca
     * @param userIdAcquirente Identificativo dell'acquirente
     */
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

    /**
     * Restituisce un valore booleano che indica se c'è stata una richiesta di acquisto
     *
     * @author Amato Luca
     * @return true se identificativo utente dell'acquirente non è nullo altrimenti false
     */
    fun getRichiesta(): Boolean{
        return this.userIdAcquirente != null
    }

    /**
     * Restituisce identificativo del proprietario dell'Annuncio
     *
     * @author Amato Luca
     * @return Identificativo del proprietario
     */
    fun getProprietario() :String {
        return this.userId
    }

    /**
     * Verifica se identificativo dell'utente corrisponde al identificativo del proprietario
     *
     * @author Amato Luca
     * @param userId identificativo utente da verificare
     * @return true se i due identificativi corrispondono altrimenti false
     */
    fun isProprietario(userId: String): Boolean{
        return userId == this.userId
    }

    /**
     * Verifica se identificativo dell'utente specificato corrisponde al identificato del acquirente
     *
     * @author Amato Luca
     * @param userId identificativo utente
     * @return true se i due identificativi corrispondono altrimenti false
     */
    fun isAcquirente(userId: String): Boolean{
        return this.userIdAcquirente == userId
    }

    /**
     * Verifica se l'annuncio corrente è stato venduto
     *
     * @author Amato Luca
     * @return true se l'annuncio è stato venduto altrimenti false
     */
    fun isVenduto(): Boolean {
        return this.userIdAcquirente != null && this.venduto
    }


    //ti comunica se e' stata inserita una recensione dopo acquisto del prodotto
    /**
     * Verifica se il proprietario ha recensito l'acquirente
     *
     * @author Amato Luca
     * @return true se acquirente è stato recensito altrimenti false
     */
    fun getAcquirenteRecensito(): Boolean {
        return this.acquirenteRecensito
    }

    /**
     * Verifica se l'acquirente ha recensito il proprietario
     *
     * @author Amato Luca
     * @return true se il proprietario è stato recensito altrimenti false
     */
    fun getProprietarioRecensito(): Boolean {
        return this.proprietarioRecensito
    }

    //setRichiesta prende in input userId, si controlla che sia quella del acquirente, per un maggiore livello di sicurezza
    suspend fun setAcquirenteRecensito(userId: String) {
        if(this.userId == userId){
            acquirenteRecensito = true
            modificaAnnuncioSuFirebase()
        }
    }

    suspend fun setProprietarioRecensito(userId: String) {
        if(this.userIdAcquirente == userId){
            proprietarioRecensito = true
            modificaAnnuncioSuFirebase()
        }
    }

    /**
     * Elimina la richiesta di acquisto se l'utente specificato è il proprietario
     *
     * @author Amato Luca
     * @param userId identificativo della persona che vuole eliminare la richiesta
     */
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

    /**
     * Imposta il nuovo titolo dell'annuncio se l'utente specificato è il proprietario o amministratore
     *
     * @author Amato Luca
     * @param newTitolo Nuovo titolo dell'annuncio
     * @param userId Identificativo della persona che vuole modificare il titolo dell'annuncio
     */
    suspend fun setTitolo(newTitolo: String, userId: String) {
        if(isProprietario(userId)) {
            this.titolo = newTitolo

            modificaAnnuncioSuFirebase()
        }
    }

    /**
     * Imposta la nuova descrizione dell'annuncio se l'utente specificato è il proprietario o amministratore
     *
     * @param newDescrizione Nuova descrizione dell'annuncio
     * @param userId Identificativo della persona che vuole modificare il titolo dell'annuncio
     */
    suspend fun setDescrizione(newDescrizione: String, userId: String) {
        if(isProprietario(userId)) {
            this.descrizione = newDescrizione

            modificaAnnuncioSuFirebase()
        }
    }

    /**
     * Imposta la nuova categoria dell'annuncio se l'utente specificato è il proprietario o amministratore
     *
     * @author Amato Luca
     * @param newCategoria Nuova categoria dell'annuncio
     * @param userId Identificativo della persona che vuole modificare la categoria dell'annuncio
     */
    suspend fun setCategoria(newCategoria: String, userId: String) {
        if(isProprietario(userId)) {
            this.categoria = newCategoria

            modificaAnnuncioSuFirebase()
        }
    }

    /**
     * Imposta il nuovo prezzo dell'annuncio se l'utente specificato è il proprietario o amministratore e se non c'è stata una richiesta di acquisto
     *
     * @author Amato Luca
     * @param newPrezzo Nuovo prezzo dell'annuncio
     * @param userId Identificativo della persona che vuole modificare il prezzo dell'annuncio
     */
    suspend fun setPrezzo(newPrezzo: Double, userId: String) {
        if(isProprietario(userId) && userIdAcquirente == null) {
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

    fun getSottocategoria(): String? {
        return this.sottocategoria
    }

    fun getPosizione(): Location {
        return this.posizione
    }

    /**
     * Scrive i dati dell'Annuncio in un Parcel
     *
     * @author Amato Luca
     * @param parcel Oggetto in cui verranno scritti i dati
     * @param flags Opzioni aggiuntive
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.userId)
        parcel.writeString(this.titolo)
        parcel.writeString(this.descrizione)
        parcel.writeDouble(this.prezzo)
        parcel.writeInt(this.stato)
        parcel.writeBoolean(this.disponibilitaSpedire)
        parcel.writeString(this.categoria)
        parcel.writeString(this.sottocategoria)
        parcel.writeValue(this.posizione)
        parcel.writeString(this.annuncioId)
        parcel.writeString(this.userIdAcquirente)
        parcel.writeLong(this.timeStampInizioVendita!!)
        parcel.writeValue(this.timeStampFineVendita)
        parcel.writeBoolean(this.venduto)
        parcel.writeBoolean(this.acquirenteRecensito)
        parcel.writeBoolean(this.proprietarioRecensito)
    }

    /**
     * @suppress
     */
    override fun describeContents(): Int {
        return 0
    }
}
