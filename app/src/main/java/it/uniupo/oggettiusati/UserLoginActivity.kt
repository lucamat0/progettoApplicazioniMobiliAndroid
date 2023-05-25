package it.uniupo.oggettiusati

import android.content.Context
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.coroutineContext

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
        private fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

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
            FavoritesFragment.recuperaAnnunciPreferitiFirebaseFirestore(auth.uid!!, this@UserLoginActivity)
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

    data class Ricerca(val userId: String, val idRicerca: String, val titoloAnnuncio: String?, val disponibilitaSpedire: Boolean?, val prezzoSuperiore: Int?, val prezzoMinore: Int?, val numeroAnnunci: Int)




    //metodi classe userlogin da inserire nei fragment


//
//    suspend fun acquistaAnnuncio(idUtente: String,myAnnuncio: Annuncio){
//
//        if(isAcquistabile(idUtente,myAnnuncio.getPrezzo())){
//            salvaTransazioneSuFirestoreFirebase(idUtente,myAnnuncio.getPrezzo(),false)
//            myAnnuncio.setVenduto(idUtente)
//        }
//    }
//
//    suspend fun isAcquistabile(idUtente: String, prezzoAcquisto: Double) : Boolean{
//
//        val myCollection = this.database.collection("utente")
//
//        val myCollectionTransazioni = myCollection.document(idUtente).collection("transazione")
//
//        return saldoAccount(myCollectionTransazioni) >= prezzoAcquisto
//    }
//



//    //Listener dei preferiti si aggiorna quando, inseriamo un nuovo elemento nei preferiti, oppure quando andiamo a recuperare i preferiti.
//    //Da qui, ogni modifica effettuata sugli annunci ci viene notificata, provvisoriamente con un Toast.
//



//

//

}