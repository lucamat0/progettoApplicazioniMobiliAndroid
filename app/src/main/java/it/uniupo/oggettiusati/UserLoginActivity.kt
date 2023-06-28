package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.ViewPagerAdapter
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

/**
 * Array che memorizza le icone che verranno mostrate nel menu principale
 *
 * @author Busto Matteo
 */
private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_shopping_cart_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_star_50,
    R.drawable.baseline_person_50
)


/**
 * Activity che viene utilizzata nel momento in cui il login va a buon fine e l'utente non è un Amministratore
 *
 * @author Amato Luca
 * @author Busto Matteo
 */
open class UserLoginActivity : AppCompatActivity() {

    companion object {

        private val database = Firebase.firestore

        private val auth = FirebaseAuth.getInstance()

        /**
         * Recupera le categorie e sottocategorie da Firebase.
         *
         * @author Amato Luca
         * @return Un insieme modificabile di oggetti Categoria
         */
        suspend fun recuperaCategorieFirebase(): List<Categoria> {
            return database.collection("categoria").get().await().documents.stream().map {
                    myDocument ->
                Categoria(
                    myDocument.id,
                    myDocument.getString("nome") as String,

                    runBlocking {
                        myDocument.reference.collection("sottocategoria").get().await().documents.stream().map {
                                documentoSottocategoria -> Categoria(documentoSottocategoria.id, documentoSottocategoria.getString("nome") as String)
                        }
                    }.collect(Collectors.toSet()))
            }.collect(Collectors.toList())
        }

        /**
         * Verifica se la categoria specificata ha delle sottocategorie
         *
         * @author Amato Luca
         * @author Busto Matteo
         * @param myCategoria Categoria da verificare
         * @return true se la categoria ha almeno una sottocategoria alrimenti false
         */
        fun hasSottocategorie(myCategoria: Categoria): Boolean {
            return myCategoria.sottocategorie!!.size > 0
        }

        /**
         * Crea un insieme di oggetti Annuncio da un insimeme di documenti e li restituisce come una HashMap
         *
         * @author Amato Luca
         * @param myDocumenti L'insieme di documenti da cui recuperare gli annunci.
         * @return Una HashMap in cui la chiave è l'ID dell'annuncio e il valore è l'oggetto Annuncio corrispondente.
         */
        suspend fun recuperaAnnunci(myDocumenti: Set<DocumentSnapshot>): HashMap<String, Annuncio> {

            //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
            val myAnnunci = HashMap<String, Annuncio>()

            for (myDocumentoAnnuncio in myDocumenti) {
                val myDocumentUtente = database.collection(Utente.nomeCollection).document(myDocumentoAnnuncio.getString("userId") as String).get().await()

                if(!myDocumentUtente.getBoolean("sospeso")!! && !myDocumentUtente.getBoolean("eliminato")!!)
                    myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)
            }

            return myAnnunci
        }

        /**
         * Converte il documento del Annuncio in un oggetto Annuncio
         *
         * @author Amato Luca
         * @param myDocumentoAnnuncio Il documento da convertire
         * @return Un oggetto Annuncio creato dai dati del documento
         */
        suspend fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

            val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

            val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita")

            val myDocumentoCategoria = database.collection("categoria").document(myDocumentoAnnuncio.getString("categoria") as String)

            val myIdSottocategoria = myDocumentoAnnuncio.getString("sottocategoria")

            var myNomeCategoria = myDocumentoCategoria.get().await().getString("nome") as String
            if(myIdSottocategoria != null)
                myNomeCategoria += ": " +
                    myDocumentoCategoria.collection("sottocategoria").document(myIdSottocategoria).get().await().getString("nome")!!

            return Annuncio(
                myDocumentoAnnuncio.getString("userId") as String,
                myDocumentoAnnuncio.getString("titolo") as String,
                myDocumentoAnnuncio.getString("descrizione") as String,
                myDocumentoAnnuncio.getDouble("prezzo") as Double,
                (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
                myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.getString("categoria") as String,
                myDocumentoAnnuncio.getString("sottocategoria"),
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.getLong("timeStampInizioVendita") as Long,
                timeStampFineVendita,
                userIdAcquirente,
                myDocumentoAnnuncio.id,
                myDocumentoAnnuncio.getBoolean("venduto") as Boolean,
                myDocumentoAnnuncio.getBoolean("acquirenteRecensito") as Boolean,
                myDocumentoAnnuncio.getBoolean("proprietarioRecensito") as Boolean,
            )
        }

        /**
         *
         * Crea un insieme di documenti filtrati in base alla disponibilità di spedizione e ai limiti di prezzo
         *
         * @author Amato Luca
         * @param disponibilitaSpedire La disponibilità a spedire utilizzato per il filtro, può essere null
         * @param prezzoSuperiore Il limite superiore utilizzato per il filtro, può essere null
         * @param prezzoInferiore Il limite inferiore utilizzato per il filtro, può essere null
         * @return Un insieme di documenti che corrispondono ai criteri specificati
         */
        private suspend fun definisciQuery(
            disponibilitaSpedire: Boolean?,
            prezzoSuperiore: Int?,
            prezzoInferiore: Int?
        ): Set<DocumentSnapshot> {

            var myDocumentiFiltrati = database.collection(Annuncio.nomeCollection)
                .whereNotEqualTo("userId", auth.currentUser?.uid).get().await().documents.toSet()

            //siamo nel caso in cui deve essere compreso
            if (prezzoSuperiore != null && prezzoInferiore != null)
                myDocumentiFiltrati = database.collection(Annuncio.nomeCollection).orderBy("prezzo")
                    .whereGreaterThan("prezzo", prezzoInferiore)
                    .whereLessThan("prezzo", prezzoSuperiore).get().await().documents.intersect(
                    myDocumentiFiltrati
                )
            else {
                if (prezzoInferiore != null)
                    myDocumentiFiltrati =
                        database.collection(Annuncio.nomeCollection).orderBy("prezzo")
                            .whereGreaterThan("prezzo", prezzoInferiore).get()
                            .await().documents.intersect(myDocumentiFiltrati)
                else if (prezzoSuperiore != null)
                    myDocumentiFiltrati =
                        database.collection(Annuncio.nomeCollection).orderBy("prezzo")
                            .whereLessThan("prezzo", prezzoSuperiore).get()
                            .await().documents.intersect(myDocumentiFiltrati)
            }
            if (disponibilitaSpedire != null)
                myDocumentiFiltrati = database.collection(Annuncio.nomeCollection)
                    .whereEqualTo("disponibilitaSpedire", disponibilitaSpedire).get()
                    .await().documents.intersect(myDocumentiFiltrati)

            return myDocumentiFiltrati
        }

        /**
         * Recupera i documenti da Firebase in base ai criteri specificati, che non sono nel carrello del utente loggato e che possono avere una richiesta di acquisto
         *
         * @author Amato Luca
         * @param titoloAnnuncio Il titolo utilizzato per il filtro, può essere null
         * @param disponibilitaSpedire La disponibilità a spedire utilizzato per il filtro, può essere null
         * @param prezzoSuperiore Il limite superiore utilizzato per il filtro, può essere null
         * @param prezzoInferiore Il limite inferiore utilizzato per il filtro, può essere null
         * @param posizioneUtente La posizione del utente per il filtro, può essere null
         * @param distanzaKmMax La distanza massima in km, può essere null
         * @return Un insieme di documenti che corrispondono ai criteri specificati
         */
        suspend fun recuperaAnnunciFiltratiPossibileRichiesta(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoInferiore: Int?, posizioneUtente: Location?, distanzaKmMax: Int?): Set<DocumentSnapshot> {

            //-- Recupero i riferimenti ai miei documenti --
            var myDocumentiRef =
                definisciQuery(disponibilitaSpedire, prezzoInferiore, prezzoSuperiore) - CartFragment.recuperaAnnunciRefCarrelloFirebaseFirestore(auth.uid!!).toSet()

            if(titoloAnnuncio != null || posizioneUtente != null) {
                val myAnnunciRef = ArrayList<DocumentSnapshot>()
                for (myDocRef in myDocumentiRef) {

                    if (titoloAnnuncio != null && posizioneUtente != null) {

                        val posizioneGeoPoint = myDocRef.getGeoPoint("posizione") as GeoPoint

                        val posizioneDocRef = Location("provider")
                        posizioneDocRef.latitude = posizioneGeoPoint.latitude
                        posizioneDocRef.longitude = posizioneGeoPoint.longitude

                        if (((myDocRef.getString("titolo") as String).lowercase().trim().contains(titoloAnnuncio.lowercase().trim())) && (posizioneUtente.distanceTo(posizioneDocRef) <= distanzaKmMax!!*1000))
                            myAnnunciRef.add(myDocRef)
                    }
                    else if(titoloAnnuncio != null){
                        if ((myDocRef.getString("titolo") as String).lowercase().trim().contains(titoloAnnuncio.lowercase().trim()))
                            myAnnunciRef.add(myDocRef)
                    }
                    else if(posizioneUtente != null){

                        val posizioneGeoPoint = myDocRef.getGeoPoint("posizione") as GeoPoint

                        val posizioneDocRef = Location("provider")
                        posizioneDocRef.latitude = posizioneGeoPoint.latitude
                        posizioneDocRef.longitude = posizioneGeoPoint.longitude

                        if (posizioneUtente.distanceTo(posizioneDocRef) <= distanzaKmMax!!*1000)
                            myAnnunciRef.add(myDocRef)
                    }
                }
                myDocumentiRef = myAnnunciRef.toSet()
            }

            return myDocumentiRef
        }


        /**
         * Recupera i documenti da Firebase in base ai criteri specificati, che non sono nel carrello del utente loggato e che non hanno una richiesta di acquisto
         *
         * @author Amato Luca
         * @param titoloAnnuncio Il titolo utilizzato per il filtro, può essere null
         * @param disponibilitaSpedire La disponibilità a spedire utilizzato per il filtro, può essere null
         * @param prezzoSuperiore Il limite superiore utilizzato per il filtro, può essere null
         * @param prezzoInferiore Il limite inferiore utilizzato per il filtro, può essere null
         * @param posizioneUtente La posizione del utente per il filtro, può essere null
         * @param distanzaKmMax La distanza massima in km, può essere null
         * @return Documenti in base ai criteri specificati
         */
        suspend fun recuperaAnnunciFiltrati(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoInferiore: Int?, posizioneUtente: Location?, distanzaKmMax: Int?): Set<DocumentSnapshot> {
            return recuperaAnnunciFiltratiPossibileRichiesta(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, posizioneUtente, distanzaKmMax).intersect(database.collection(Annuncio.nomeCollection).whereEqualTo("userIdAcquirente",null)
                .get().await().documents.toSet())
        }


        /**
         * Recupero tutti gli utenti da Firebase, eccetto quello loggato
         *
         * @author Amato Luca
         * @param userId identificativo del utente da non recuperare
         * @return Lista di oggetti Utente che sono stati recuperati
         */
        suspend fun recuperaUtenti(userId: String): ArrayList<Utente> {

            val myUtenti = ArrayList<Utente>()

            val myDocumenti =
                database.collection(Utente.nomeCollection).whereNotEqualTo("userId", userId).whereEqualTo("sospeso",false).whereEqualTo("eliminato", false).get().await()

            for (myDocumento in myDocumenti.documents) {
                myUtenti.add(
                    documentoUtenteToObject(myDocumento)
                )
            }
            return myUtenti
        }

        /**
         * Converte un documento in un oggetto Utente
         *
         * @author Amato Luca
         * @param myDocumento Documento da convertire
         * @return Oggetto Utente creato dai dati del documento
         */
        private fun documentoUtenteToObject(myDocumento: DocumentSnapshot): Utente {
            return Utente(
                myDocumento.id,
                myDocumento.getString("nome") as String,
                myDocumento.getString("cognome") as String,
                myDocumento.getBoolean("amministratore") as Boolean,
                myDocumento.getString("numeroDiTelefono") as String,
                myDocumento.getBoolean("sospeso") as Boolean,
                myDocumento.getString("dataNascita") as String,
                myDocumento.getBoolean("eliminato") as Boolean,
                myDocumento.getString("email") as String
            )
        }

        /**
         * Recupera un singolo utente da Firebase
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente da recuperare
         * @return Oggetto Utente che rappresenta l'utente recuperato
         */
        suspend fun recuperaUtente(userId: String): Utente {
            return documentoUtenteToObject(
                database.collection(Utente.nomeCollection).document(userId).get().await()
            )
        }
    }

    //lateinit var myLocationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

//        supportActionBar?.setDisplayShowTitleEnabled(false)
        runBlocking {
            supportActionBar?.setTitle(recuperaUtente(auth.uid!!).getNome())
        }

        //fragments
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, tabIcons.size)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            //tab.text = pageTitlesArray[position]
            tab.icon = ContextCompat.getDrawable(this, tabIcons[position])
        }.attach()
        //end fragments

        //logica bottone logout
/*        val logoutButton = findViewById<Button>(R.id.logout_activity)
        logoutButton?.setOnClickListener {
            Toast.makeText(this, "Uscita...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }*/

        val fabAggiungiOggetto = findViewById<FloatingActionButton>(R.id.aggiungi_oggetto)
        fabAggiungiOggetto.setOnClickListener {
            val i = Intent(this, AggiungiOggettoActivity::class.java)
            val a : Annuncio? = null
            i.putExtra("annuncio", a)
            startActivity(i)
        }

        lateinit var username: String
        val userRef = database.collection(Utente.nomeCollection).document(auth.uid!!)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                username = document.get("nome").toString()
            } else {
                Log.w("document error", "Error: document is null")
            }

            val usernameView = findViewById<TextView>(R.id.username)
            val userText = "Ciao $username"
            usernameView?.text = userText // '?' per AdminLoginActivity

//            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        //myLocationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager;

        //-- Recupero gli annunci preferiti dell'utente --
        runBlocking {
            //recuperaRicercheSalvateFirebaseFirestore(auth.uid!!)
            //posizione temporanea per test
            val posizioneUtente = Location("provider")
            posizioneUtente.latitude = 44.922
            posizioneUtente.longitude = 8.617
            controllaStatoRicercheAnnunci(auth.uid!!, posizioneUtente)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_menu -> {
                Toast.makeText(this, "Uscita...", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * Recupera una lista di ricerche salvate per un determinato utente
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente per cui recuperare le ricerche salvate
     * @return Lista di oggetti Ricerca
     */
    private suspend fun recuperaRicercheSalvateFirebaseFirestore(userId: String): ArrayList<Ricerca> {

        val myCollection = database.collection(Utente.nomeCollection)

        val myDocumentUtente = myCollection.document(userId)

        val myDocumentsRicerca = myDocumentUtente.collection("ricerca").get().await()

        val myArrayList = ArrayList<Ricerca>()
        for (myRicerca in myDocumentsRicerca.documents) {
            myArrayList.add(
                Ricerca(
                    userId,
                    myRicerca.get("idRicerca") as String,
                    myRicerca.get("titoloAnnuncio") as String?,
                    myRicerca.getBoolean("disponibilitaSpedire"),
                    myRicerca.get("prezzoSuperiore") as Int?,
                    myRicerca.get("prezzoMinore") as Int?,
                    myRicerca.get("numeroAnnunci") as Int,
                    myRicerca.get("distanzaMax") as Int?
                )
            )
        }

        return myArrayList
    }

    /**
     * Controlla se il numero degli annunci è aumentato, considerando le ricerche salvate
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @param posizioneUtente Posizione dell'utente
     * @return true se il numero delle ricerche è cambiato altrimenti false
     */
    private suspend fun controllaStatoRicercheAnnunci(userId: String, posizioneUtente: Location): Boolean {

        val myCollection = database.collection(Utente.nomeCollection)

        val myDocumentoUtente = myCollection.document(userId)

        val myCollectionRicerca = myDocumentoUtente.collection("ricerca")

        val myDocumentiRicerca = myCollectionRicerca.get().await()

        for (myDocumento in myDocumentiRicerca.documents) {

            val titoloAnnuncio = myDocumento.getString("titoloAnnuncio")
            val disponibilitaSpedire = myDocumento.getBoolean("disponibilitaSpedire")
            val prezzoSuperiore = (myDocumento.getLong("prezzoSuperiore") )?.toInt()
            val prezzoInferiore = (myDocumento.getLong("prezzoMinore") )?.toInt()
            val distanzaMax = myDocumento.getLong("distanzaMax")?.toInt()

            val numeroAnnunciRicerca = (myDocumento.get("numeroAnnunci") as Long).toInt()

            val numeroAnnunci = recuperaAnnunciFiltrati(titoloAnnuncio,disponibilitaSpedire,prezzoSuperiore,prezzoInferiore,posizioneUtente, distanzaMax).size

            if (numeroAnnunci > numeroAnnunciRicerca) {

                Toast.makeText(
                    this@UserLoginActivity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono aumentati!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(
                    userId,
                    myDocumento.id,
                    titoloAnnuncio,
                    disponibilitaSpedire,
                    prezzoSuperiore,
                    prezzoInferiore,
                    numeroAnnunci
                )

                return true
            } else if (numeroAnnunci < numeroAnnunciRicerca) {
                Toast.makeText(
                    this@UserLoginActivity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono diminuiti!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(
                    userId,
                    myDocumento.id,
                    titoloAnnuncio,
                    disponibilitaSpedire,
                    prezzoSuperiore,
                    prezzoInferiore,
                    numeroAnnunci
                )

                return true
            }
        }
        return false
    }

    /**
     * Aggiorna il numero di annunci per una specifica ricerca, che era stata salvata precedentemente
     *
     * @author Amato Luca
     * @param userId Identificato dell'utente
     * @param idRicerca Identificativo della ricerca salvata
     * @param titoloAnnuncio Il titolo utilizzato per il filtro, può essere null
     * @param disponibilitaSpedire La disponibilità a spedire utilizzato per il filtro, può essere null
     * @param prezzoSuperiore Il limite superiore utilizzato per il filtro, può essere null
     * @param prezzoInferiore Il limite inferiore utilizzato per il filtro, può essere null
     * @param numeroAnnunci  Il nuovo numero di annunci per la ricerca.
     */
    private suspend fun aggiornaRicerca(
        userId: String,
        idRicerca: String,
        titoloAnnuncio: String?,
        disponibilitaSpedire: Boolean?,
        prezzoSuperiore: Int?,
        prezzoInferiore: Int?,
        numeroAnnunci: Int
    ) {

        val myCollection = database.collection(Utente.nomeCollection)

        val myDocumento = myCollection.document(userId)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myRicerca = myCollectionRicerca.document(idRicerca)

        myRicerca.update(
            "titoloAnnuncio",
            titoloAnnuncio,
            "disponibilitaSpedire",
            disponibilitaSpedire,
            "prezzoSuperiore",
            prezzoSuperiore,
            "prezzoMinore",
            prezzoInferiore,
            "numeroAnnunci",
            numeroAnnunci
        ).await()
    }

    /**
     * Rappresenta una ricerca sugli annunci
     *
     * @author Amato Luca
     * @property userId Identificativo dell'utente
     * @property idRicerca Identificativo della Ricerca
     * @property titoloAnnuncio Il titolo della ricerca, può essere null
     * @property disponibilitaSpedire La disponibilità a spedire utilizzato per la ricerca, può essere null
     * @property prezzoSuperiore Il limite superiore utilizzato per la ricerca, può essere null
     * @property prezzoInferiore Il limite inferiore utilizzato per la ricerca, può essere null
     * @property numeroAnnunci Il numero di annunci che abbiamo ottenuto dopo il filtro
     * @property distanzaKmMax La distanza massima in km utilizzata per la ricerca, può essere null
     */
    data class Ricerca(
        val userId: String,
        val idRicerca: String,
        val titoloAnnuncio: String?,
        val disponibilitaSpedire: Boolean?,
        val prezzoSuperiore: Int?,
        val prezzoInferiore: Int?,
        val numeroAnnunci: Int,
        val distanzaKmMax: Int?
    )

    /**
     * Rappresenta una categoria con le sue sottocategorie
     *
     * @author Amato Luca
     * @property id Identificativo della Categoria
     * @property nome Nome della categoria\
     * @property sottocategorie Insieme di elementi contenenti le sottocategorie
     */
    data class Categoria(
        val id: String,
        val nome: String? = null,
        val sottocategorie: MutableSet<Categoria>? = null){
        override fun equals(other: Any?): Boolean {
            return if (this === other) true
            else if(other == null || other !is Categoria) false
            else this.id == other.id
        }
    }


    /**
     * Rappresenta un utente
     *
     * @author Amato Luca
     * @property userId Identificativo dell'utente
     * @property nome Nome dell'utente
     * @property cognome Cognome dell'utente
     * @property amministratore  Indica se l'utente è un amministratore o meno.
     * @property numeroDiTelefono Numero di telefono dell'utente
     * @property sospeso Indica se l'utente è sospeso o attivo
     * @property dataNascita Data di nascita dell'utente
     * @property eliminato Indica se l'utente è eliminato o no
     */
    data class Utente(
        private val userId: String,
        private val nome: String,
        private val cognome: String,
        private val amministratore: Boolean,
        private val numeroDiTelefono: String,
        private val sospeso: Boolean,
        private val dataNascita: String,
        private val eliminato: Boolean,
        private val email: String
    ) {
        companion object {  const val nomeCollection = "utente" }

        /**
         * Restituisce il nome e cognome dell'utente
         *
         * @author Amato Luca
         * @return nome e cognome dell'utente
         */
        fun getNomeCognome(): String {
            return "${this.nome} ${this.cognome}"
        }

        fun getEmail(): String {
            return this.email
        }

        fun getNumeroTelefono(): String{
            return this.numeroDiTelefono
        }

        fun getEliminato(): Boolean {
            return this.eliminato
        }

        fun getSospeso(): Boolean {
            return this.sospeso
        }

        fun getNome(): String{
            return this.nome
        }

        fun getCognome(): String{
            return this.cognome
        }

        fun getId(): String{
            return this.userId
        }

        /**
         * Recupera il punteggio medio delle recensioni su Firebase per un determinato utente
         *
         * @author Amato Luca
         * @return Punteggio medio delle Recensioni
         */
        suspend fun recuperaPunteggioRecensioniFirebase(): Double {

            val queryRecensioni =
                database.collection(nomeCollection).document(this.userId).collection("recensione").get()
                    .await()

            val numeroRecensioni = queryRecensioni.documents.size

            if (numeroRecensioni > 0) {

                var totalePunteggioRecensioni = 0.0
                for (myRecensioni in queryRecensioni.documents)
                    totalePunteggioRecensioni += (myRecensioni.getDouble("votoAlUtente")!!.toFloat()).toDouble()

                return totalePunteggioRecensioni / numeroRecensioni
            }
            return 0.0
        }

        /**
         * Calcola il tempo medio di vendita degli annunci per un determinato utente
         *
         * @author Amato Luca
         * @return Tempo medio di vendita
         */
        suspend fun calcolaTempoMedioAnnunciVenduti(): Double{

            val myCollectionAnnuncioRef = database.collection(Annuncio.nomeCollection).whereEqualTo("userId", this.userId).whereEqualTo("venduto",true)

            val myDocuments = myCollectionAnnuncioRef.get().await()

            val numeroDocumenti = myDocuments.size()

            if(numeroDocumenti>0){

                var tempoTotale: Long = 0
                for (myDocument in myDocuments.documents) {

                    val timeStampInizioVendita = myDocument.getLong("timeStampInizioVendita")
                    val timeStampFineVendita = myDocument.getLong("timeStampFineVendita")

                    //Log.d("TEMPO INIZIO VENDITA", "Il tempo inizio vendita é $timeStampInizioVendita")

                    //Log.d("TEMPO FINE VENDITA", "Il tempo fine vendita é $timeStampFineVendita")

                    tempoTotale += (timeStampFineVendita!!.toLong() - timeStampInizioVendita!!.toLong())

                    //Log.d("TEMPO TOTALE", "Il tempo totale é $tempoTotale")

                }

                val tempoMedio = tempoTotale / numeroDocumenti

                //Log.d("TEMPO MEDIO", "Il tempo medio é $tempoMedio")

                //86400000 = numero di millisecondi per giorno.
                return tempoMedio.toDouble() / 86400000.toDouble()
            }
            return 0.0
        }
    }
}