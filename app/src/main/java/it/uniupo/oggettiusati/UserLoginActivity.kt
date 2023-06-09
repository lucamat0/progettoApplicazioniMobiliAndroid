package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
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
import it.uniupo.oggettiusati.fragment.ChatFragment
import it.uniupo.oggettiusati.fragment.HomeFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

val pageTitlesArray = arrayOf(
    "Home",
    "Carrello",
    "Chat",
    "Preferiti",
)

private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_shopping_cart_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_star_50,
    R.drawable.baseline_person_50
)

open class UserLoginActivity : AppCompatActivity() {

    companion object {

        private var ultimoAnnuncio: String? = null

        private val database = Firebase.firestore

        private val auth = FirebaseAuth.getInstance()

        fun recuperaAnnunci(myDocumenti: Set<DocumentSnapshot>): HashMap<String, Annuncio> {

            //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
            val myAnnunci = HashMap<String, Annuncio>()

            for (myDocumentoAnnuncio in myDocumenti) {
                myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)
            }

            return myAnnunci
        }
        fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

            val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

            val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita")

            return Annuncio(
                myDocumentoAnnuncio.get("userId") as String,
                myDocumentoAnnuncio.get("titolo") as String,
                myDocumentoAnnuncio.get("descrizione") as String,
                myDocumentoAnnuncio.getDouble("prezzo") as Double,
                (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
                myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.get("categoria") as String,
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.getLong("timeStampInizioVendita") as Long,
                timeStampFineVendita,
                userIdAcquirente,
                myDocumentoAnnuncio.id)
        }

        suspend fun definisciQuery(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoInferiore: Int?): Set<DocumentSnapshot> {

            var myDocumentiFiltrati = database.collection(Annuncio.nomeCollection).whereNotEqualTo("userId", auth.currentUser?.uid).get().await().documents.toSet()
/*
            myDocumentiFiltrati.forEach { myDocumento ->
                Log.d("Uguale", (myDocumento.get("userId") as String).equals(auth.currentUser!!.uid).toString()+ "[ "+ myDocumento.get("userId")+"] [${auth.currentUser!!.uid}] "+myDocumento.get("titolo") + "${myDocumentiFiltrati.size}")
            }

            Log.d("UserId", auth.currentUser!!.uid)
*/

            if(titoloAnnuncio != null)
                myDocumentiFiltrati = database.collection(Annuncio.nomeCollection).whereEqualTo("titolo", titoloAnnuncio).get().await().documents.intersect(myDocumentiFiltrati)
            //siamo nel caso in cui deve essere compreso
            if(prezzoSuperiore != null && prezzoInferiore != null)
                myDocumentiFiltrati =  database.collection(Annuncio.nomeCollection).orderBy("prezzo").whereGreaterThan("prezzo", prezzoInferiore).whereLessThan("prezzo", prezzoSuperiore).get().await().documents.intersect(myDocumentiFiltrati)
            else {
                if(prezzoInferiore != null)
                    myDocumentiFiltrati =  database.collection(Annuncio.nomeCollection).orderBy("prezzo").whereGreaterThan("prezzo", prezzoInferiore).get().await().documents.intersect(myDocumentiFiltrati)
                else if(prezzoSuperiore != null)
                    myDocumentiFiltrati =  database.collection(Annuncio.nomeCollection).orderBy("prezzo").whereLessThan("prezzo", prezzoSuperiore).get().await().documents.intersect(myDocumentiFiltrati)
            }
            if(disponibilitaSpedire != null)
                myDocumentiFiltrati = database.collection(Annuncio.nomeCollection).whereEqualTo("disponibilitaSpedire", disponibilitaSpedire).get().await().documents.intersect(myDocumentiFiltrati)

            return myDocumentiFiltrati
        }

        suspend fun recuperaUtenti(): ArrayList<Utente>{

            val myUtenti = ArrayList<Utente>()

            val myDocumenti = database.collection("utente")/*.whereNotEqualTo("userId", auth.uid)*/.get().await()
            for(myDocumento in myDocumenti.documents){
                myUtenti.add(
                    documentoUtenteToObject(myDocumento)
                )
            }
            return myUtenti
        }

        fun documentoUtenteToObject(myDocumento :DocumentSnapshot): Utente {
            return Utente(
                myDocumento.id,
                myDocumento.getString("nome") as String,
                myDocumento.getString("cognome") as String,
                (myDocumento.getLong("amministratore") as Long).toInt(),
                myDocumento.getString("numeroDiTelefono") as String,
                myDocumento.getBoolean("sospeso") as Boolean,
                myDocumento.getString("dataNascita") as String
            )
        }

        suspend fun recuperaUtente(userId :String) : Utente {
            return documentoUtenteToObject(database.collection("utente").document(userId).get().await())
        }


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

//        supportActionBar?.setDisplayShowTitleEnabled(false)
        runBlocking{
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
        val logoutButton = findViewById<Button>(R.id.logout_activity)
        logoutButton?.setOnClickListener {
            Toast.makeText(this, "Uscita...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }

        val fabAggiungiOggetto = findViewById<FloatingActionButton>(R.id.aggiungi_oggetto)
        fabAggiungiOggetto.setOnClickListener {
            startActivity(Intent(this, AggiungiOggettoActivity::class.java))
        }
        
        lateinit var username: String
        val userRef = database.collection("utente").document(auth.uid!!)
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

        //-- Recupero gli annunci preferiti dell'utente --
        runBlocking {
            //recuperaRicercheSalvateFirebaseFirestore(auth.uid!!)
            //posizione temporanea per test
            var posizioneUtente: Location = Location("provider")
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

        val myCollection = database.collection("utente")

        val myDocumentUtente = myCollection.document(userId)

        val myDocumentsRicerca = myDocumentUtente.collection("ricerca").get().await()

        val myArrayList = ArrayList<Ricerca>()
        for(myRicerca in myDocumentsRicerca.documents){
            myArrayList.add(Ricerca(userId,myRicerca.get("idRicerca") as String, myRicerca.get("titoloAnnuncio") as String?, myRicerca.getBoolean("disponibilitaSpedire"), myRicerca.get("prezzoSuperiore") as Int?, myRicerca.get("prezzoMinore") as Int?, myRicerca.get("numeroAnnunci") as Int, myRicerca.get("distanzaMax") as Int?))
        }

        return myArrayList
    }

    suspend fun controllaStatoRicercheAnnunci(userId: String, posizioneUtente :Location): Boolean {

        val myCollection = database.collection("utente")

        val myDocumentoUtente = myCollection.document(userId)

        val myCollectionRicerca = myDocumentoUtente.collection("ricerca")

        val myDocumentiRicerca = myCollectionRicerca.get().await()

        for(myDocumento in myDocumentiRicerca.documents) {

            val titoloAnnuncio = myDocumento.get("titoloAnnuncio") as String?
            val disponibilitaSpedire = myDocumento.getBoolean("disponibilitaSpedire")

            val prezzoSuperiore = (myDocumento.get("prezzoSuperiore") as Long?)?.toInt()

            val prezzoMinore = (myDocumento.get("prezzoMinore") as Long?)?.toInt()

            val numeroAnnunciRicerca = (myDocumento.get("numeroAnnunci") as Long).toInt()

            val distanzaMax = myDocumento.getLong("distanzaMax")!!.toInt()

            val myAnnunciRef = definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoMinore, prezzoSuperiore)
            var myAnnunci = recuperaAnnunci(myAnnunciRef)
            if(distanzaMax != null)
                myAnnunci = HomeFragment.recuperaAnnunciLocalizzazione(posizioneUtente, distanzaMax, myAnnunci)

            val numeroAnnunci = myAnnunci.size

            if( numeroAnnunci > numeroAnnunciRicerca) {

                Toast.makeText(
                    this@UserLoginActivity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono aumentati!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(userId, myDocumento.id, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore, numeroAnnunci)

                return true
            } else if(numeroAnnunci < numeroAnnunciRicerca) {
                Toast.makeText(
                    this@UserLoginActivity,
                    "Il numero di annunci della ricerca ${myDocumento.id} sono diminuiti!",
                    Toast.LENGTH_LONG
                ).show()

                aggiornaRicerca(userId, myDocumento.id, titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore, numeroAnnunci)

                return true
            }
        }
        return false
    }

    private suspend fun aggiornaRicerca(userId: String, idRicerca: String, titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?, numeroAnnunci: Int) {

        val myCollection = database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionRicerca = myDocumento.collection("ricerca")

        val myRicerca = myCollectionRicerca.document(idRicerca)

        myRicerca.update("titoloAnnuncio", titoloAnnuncio,"disponibilitaSpedire", disponibilitaSpedire, "prezzoSuperiore", prezzoSuperiore, "prezzoMinore", prezzoMinore, "numeroAnnunci", numeroAnnunci).await()
    }


    data class Ricerca(val userId: String, val idRicerca: String, val titoloAnnuncio: String?, val disponibilitaSpedire: Boolean?, val prezzoSuperiore: Int?, val prezzoMinore: Int?, val numeroAnnunci: Int, val distanzaMax :Int?)
    data class Utente(val uid: String, val nome: String, val cognome: String, val amministratore: Int, val numeroDiTelefono: String, val sospeso: Boolean, val dataNascita: String) {
        fun getNomeCognome() :String{
            return "$nome $cognome"
        }
    }
}