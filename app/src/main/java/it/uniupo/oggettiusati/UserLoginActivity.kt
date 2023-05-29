package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.ViewPagerAdapter
import it.uniupo.oggettiusati.fragment.FavoritesFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

val pageTitlesArray = arrayOf(
    "Home",
    "Carrello",
    "Chat",
    "Preferiti",
)
open class UserLoginActivity : AppCompatActivity() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()

    //--- Fine informazioni per il collegamento con firebase firestore ---

    val userId = auth.currentUser!!.uid

    companion object {

        private var ultimoAnnuncio: String? = null

        private val database = Firebase.firestore

        fun recuperaAnnunci(myDocumenti: QuerySnapshot, home: Boolean): HashMap<String, Annuncio> {

            //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
            val myAnnunci = HashMap<String, Annuncio>()

            for (myDocumentoAnnuncio in myDocumenti.documents) {
                myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)

                if(home)
                    ultimoAnnuncio = myDocumentoAnnuncio.id
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
                myDocumentoAnnuncio.get("prezzo") as Double,
                (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
                myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
                myDocumentoAnnuncio.get("categoria") as String,
                myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
                myDocumentoAnnuncio.getLong("timeStampInizioVendita") as Long,
                timeStampFineVendita,
                userIdAcquirente,
                myDocumentoAnnuncio.id)
        }

        fun definisciQuery(titoloAnnuncio: String?, disponibilitaSpedire: Boolean?, prezzoSuperiore: Int?, prezzoMinore: Int?): Query {

            //Quando ad un annuncio non è assegnato un acquirente, non vogliamo mostrare nella home degli annunci che sono già stati venduti.
            var myQuery = database.collection(Annuncio.nomeCollection).whereEqualTo("userIdAcquirente", null)

            if(titoloAnnuncio != null)
                myQuery = myQuery.whereEqualTo("titolo", titoloAnnuncio)
            //siamo nel caso in cui deve essere compreso
            if(prezzoSuperiore != null && prezzoMinore != null)
                myQuery = myQuery.orderBy("prezzo").whereGreaterThan("prezzo", prezzoMinore).whereLessThan("prezzo", prezzoSuperiore)
            else {
                if(prezzoSuperiore != null)
                    myQuery = myQuery.orderBy("prezzo").whereGreaterThan("prezzo", prezzoSuperiore)
                else if(prezzoMinore != null)
                    myQuery = myQuery.orderBy("prezzo").whereLessThan("prezzo", prezzoMinore)
            }
            if(disponibilitaSpedire != null)
                myQuery = myQuery.whereEqualTo("disponibilitaSpedire", disponibilitaSpedire)

            return myQuery
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

        //fragments
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, pageTitlesArray.size)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pageTitlesArray[position]
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
        val userRef = database.collection("utente").document(userId)
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
            recuperaRicercheSalvateFirebaseFirestore(auth.uid!!)
        }
    }

    suspend fun recuperaRicercheSalvateFirebaseFirestore(userId: String): ArrayList<Ricerca> {

        val myCollection = database.collection("utente")

        val myDocumentUtente = myCollection.document(userId)

        val myDocumentsRicerca = myDocumentUtente.collection("ricerca").get().await()

        val myArrayList = ArrayList<Ricerca>()
        for(myRicerca in myDocumentsRicerca.documents){
            myArrayList.add(Ricerca(userId,myRicerca.get("idRicerca") as String, myRicerca.get("titoloAnnuncio") as String?, myRicerca.getBoolean("disponibilitaSpedire"), myRicerca.get("prezzoSuperiore") as Int?, myRicerca.get("prezzoMinore") as Int?, myRicerca.get("numeroAnnunci") as Int))
        }

        return myArrayList
    }

    suspend fun controllaStatoRicercheAnnunci(userId: String): Boolean {

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

            val query = definisciQuery(titoloAnnuncio, disponibilitaSpedire, prezzoSuperiore, prezzoMinore)

            val numeroAnnunci = query.get().await().size()

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


    data class Ricerca(val userId: String, val idRicerca: String, val titoloAnnuncio: String?, val disponibilitaSpedire: Boolean?, val prezzoSuperiore: Int?, val prezzoMinore: Int?, val numeroAnnunci: Int)
}