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

//val pageTitlesArray = arrayOf("Home", "Carrello", "Chat", "Preferiti")

private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_shopping_cart_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_star_50,
    R.drawable.baseline_person_50
)

open class UserLoginActivity : AppCompatActivity() {

    companion object {

        private val database = Firebase.firestore

        val auth = FirebaseAuth.getInstance()

        suspend fun recuperaCategorieFirebase(): MutableSet<Categoria>? {
            return database.collection("categorie").get().await().documents.stream().map {
                myDocument ->
                Categoria(
                    myDocument.id,
                    myDocument.getString("nome") as String,

                    runBlocking {
                        myDocument.reference.collection("sottocategoria").get().await().documents.stream().map {
                            documentoSottocategoria -> documentoSottocategoria.getString("nome") as String
                        }
                    }.collect(Collectors.toSet()))
            }.collect(Collectors.toSet())
        }

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

        suspend fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

            val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

            val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita")

            val myDocumentoCategoria = database.collection("categoria").document(myDocumentoAnnuncio.getString("categoria") as String)

            val myIdSottocategoria = myDocumentoAnnuncio.getString("sottocategoria") as String?

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
                myNomeCategoria,
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

        suspend fun definisciQuery(
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

                        if (((myDocRef.getString("titolo") as String).lowercase().contains(titoloAnnuncio.lowercase())) && (posizioneUtente.distanceTo(posizioneDocRef) <= distanzaKmMax!!*1000))
                            myAnnunciRef.add(myDocRef)
                    }
                    else if(titoloAnnuncio != null){
                        if ((myDocRef.getString("titolo") as String).lowercase().contains(titoloAnnuncio.lowercase()))
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


        suspend fun recuperaAnnunciFiltrati(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoInferiore: Int?, posizioneUtente: Location?, distanzaKmMax: Int?): Set<DocumentSnapshot> {
            return recuperaAnnunciFiltratiPossibileRichiesta(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoInferiore, posizioneUtente, distanzaKmMax).intersect(database.collection(Annuncio.nomeCollection).whereEqualTo("userIdAcquirente",null)
                .get().await().documents.toSet())
        }

        //Recupero tutti gli utenti, eccetto quello che e' loggato
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

        private fun documentoUtenteToObject(myDocumento: DocumentSnapshot): Utente {
            return Utente(
                myDocumento.id,
                myDocumento.getString("nome") as String,
                myDocumento.getString("cognome") as String,
                myDocumento.getBoolean("amministratore") as Boolean,
                myDocumento.getString("numeroDiTelefono") as String,
                myDocumento.getBoolean("sospeso") as Boolean,
                myDocumento.getString("dataNascita") as String,
                myDocumento.getBoolean("eliminato") as Boolean
            )
        }

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
            supportActionBar?.setTitle(recuperaUtente(auth.uid!!).nome)
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
            startActivity(Intent(this, AggiungiOggettoActivity::class.java))
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

    suspend fun recuperaRicercheSalvateFirebaseFirestore(userId: String): ArrayList<Ricerca> {

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

    private suspend fun aggiornaRicerca(
        userId: String,
        idRicerca: String,
        titoloAnnuncio: String?,
        disponibilitaSpedire: Boolean?,
        prezzoSuperiore: Int?,
        prezzoMinore: Int?,
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
            prezzoMinore,
            "numeroAnnunci",
            numeroAnnunci
        ).await()
    }


    data class Ricerca(
        val userId: String,
        val idRicerca: String,
        val titoloAnnuncio: String?,
        val disponibilitaSpedire: Boolean?,
        val prezzoSuperiore: Int?,
        val prezzoMinore: Int?,
        val numeroAnnunci: Int,
        val distanzaMax: Int?
    )

    data class Categoria(val id: String,
                         val nome: String,
                         val sottocategorie: MutableSet<String>? = null)

    data class Utente(
        val userId: String,
        val nome: String,
        val cognome: String,
        val amministratore: Boolean,
        val numeroDiTelefono: String,
        val sospeso: Boolean,
        val dataNascita: String,
        val eliminato: Boolean
    ) {
        companion object {  const val nomeCollection = "utente" }

        fun getNomeCognome(): String {
            return "$nome $cognome"
        }



        suspend fun recuperaPunteggioRecensioniFirebase(): Double {

            val queryRecensioni =
                database.collection(nomeCollection).document(this.userId).collection("recensione").get()
                    .await()

            val numeroRecensioni = queryRecensioni.documents.size

            //Log.d("RECENSIONI CON VOTO PIÚ ALTO", numeroRecensioni.toString())

            if (numeroRecensioni > 0) {

                var totalePunteggioRecensioni = 0.0
                for (myRecensioni in queryRecensioni.documents)
                    totalePunteggioRecensioni += (myRecensioni.getLong("votoAlUtente") as Long).toDouble()

                return totalePunteggioRecensioni / numeroRecensioni
            }
            return 0.0
        }

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

                    tempoTotale += (timeStampFineVendita!!.toLong() - timeStampInizioVendita!!.toLong()) * -1

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