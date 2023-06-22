package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.AdminViewPagerAdapter
import it.uniupo.oggettiusati.adapter.ViewPagerAdapter
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.HomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.ArrayList
import java.util.LinkedList
import kotlin.collections.HashMap
import kotlin.streams.toList

val pageTitlesArray = arrayOf("Home", "Chat", "Personal")

private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_person_50
)

class AdminLoginActivity : UserLoginActivity() {
    private val database = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        supportActionBar?.title = "[${supportActionBar?.title}] - admin"

        val viewPager = findViewById<ViewPager2>(R.id.viewPager2_admin)
        viewPager.adapter = AdminViewPagerAdapter(supportFragmentManager, lifecycle, tabIcons.size)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout_admin)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pageTitlesArray[position]
            tab.icon = ContextCompat.getDrawable(this, tabIcons[position])
        }.attach()
    }

    //--- Deve poter eliminare utenti o sospenderli dalle attività ---
    private suspend fun eliminaUtente(userId: String) {

        database.collection(Utente.nomeCollection).document(userId).update("eliminato", true).await()

        database.collection(Annuncio.nomeCollection).get().await().documents.stream().forEach {
            doc -> if(doc.getString("userId") as String == userId){
                if(doc.getString("userIdAcquirente") != null && !(doc.getBoolean("venduto") as Boolean)!!)
                    runBlocking {  CartFragment.salvaTransazioneSuFirestoreFirebase(doc.getString("userIdAcquirente") as String, doc.getDouble("prezzo") as Double, true) }
            }
        }
    }

    suspend fun sospendiUtente(userId: String) {
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", true).await()
    }

    suspend fun attivaUtente(userId: String){
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", false).await()
    }

    //--- Fine eliminazione e sospensione utente ---

    //--- Accesso a dati statistici ---

    private val myAnnunciVenduti = database.collection(Annuncio.nomeCollection).whereEqualTo("venduto", false)

    suspend fun numeroOggettiInVendita(): Int {
        return myAnnunciVenduti.get().await().size()
    }

    suspend fun numeroOggettiInVenditaPerSpecificoUtente(userId: String): Int {
           return myAnnunciVenduti.whereEqualTo("userId", userId).get().await().size()
    }

    private suspend fun numeroOggettiInVenditaPerRaggioDistanza(posizioneUtente: Location, distanzaMax: Int): Int {

            return recuperaAnnunciFiltrati(null, null, null, null, posizioneUtente, distanzaMax).size
    }

    suspend fun classificaUtentiRecensitiConVotoPiuAlto(): List<Utente> {

        var myUtenti = recuperaUtenti(auth.uid!!)

        myUtenti = myUtenti.sortedByDescending { utente -> runBlocking{utente.recuperaPunteggioRecensioniFirebase()}} as ArrayList<Utente>

        return myUtenti
    }

    suspend fun calcolaTempoMedioAnnunciVenduti(): Double{

        val myUtenti = recuperaUtenti(auth.uid!!).stream().filter{ utente-> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() != 0.0 } }.toList()

        return myUtenti.sumOf { utente -> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() } } / myUtenti.size
    }
}

