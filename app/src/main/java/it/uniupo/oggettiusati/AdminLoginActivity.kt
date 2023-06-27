package it.uniupo.oggettiusati

import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.AdminViewPagerAdapter
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.ArrayList
import kotlin.streams.toList

val pageTitlesArray = arrayOf("Home", "Chat", "Personal")

/**
 * Array che memorizza le icone che verranno mostrate nel menu principale
 *
 * @author Busto Matteo
 */
private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_person_50
)

/**
 * Activity che viene utilizzata nel momento in cui il login va a buon fine e l'utente è un Amministratore
 *
 * @author Amato Luca
 * @author Busto Matteo
 */
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

    /**
     * Elimina l'utente specificato, nel caso in cui ci fosse stata una richiesta di acquisto ai suoi annunci i soldi vengono riaccreditati
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     */
    private suspend fun eliminaUtente(userId: String) {

        database.collection(Utente.nomeCollection).document(userId).update("eliminato", true).await()

        database.collection(Annuncio.nomeCollection).get().await().documents.stream().forEach {
            doc -> if(doc.getString("userId") as String == userId){
                if(doc.getString("userIdAcquirente") != null && !(doc.getBoolean("venduto") as Boolean))
                    runBlocking {  CartFragment.salvaTransazioneSuFirestoreFirebase(doc.getString("userIdAcquirente") as String, doc.getDouble("prezzo") as Double, true) }
            }
        }
    }

    /**
     * Sospende l'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     */
    suspend fun sospendiUtente(userId: String) {
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", true).await()
    }

    /**
     * Riammette l'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     */
    suspend fun attivaUtente(userId: String){
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", false).await()
    }

    //--- Fine eliminazione e sospensione utente ---

    //--- Accesso a dati statistici ---

    private val myAnnunciVenduti = database.collection(Annuncio.nomeCollection).whereEqualTo("venduto", false)

    /**
     * Restituisce il numero di oggetti attualmente in vendita
     *
     * @author Amato Luca
     * @return Numero di oggetti
     */
    suspend fun numeroOggettiInVendita(): Int {
        return myAnnunciVenduti.get().await().size()
    }

    /**
     * Restituisce il numero di oggetti attualmente in vendita per un particolare utente
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @return Numero di oggetti
     */
    suspend fun numeroOggettiInVenditaPerSpecificoUtente(userId: String): Int {
           return myAnnunciVenduti.whereEqualTo("userId", userId).get().await().size()
    }

    /**
     * Restituisce il numero di oggetti attualmente in vendita entro una certa distanza dalla posizione dell'utente
     *
     * @author Amato Luca
     * @param posizioneUtente Posizione dell'utente
     * @param distanzaKmMax Distanza massima in km entro cui cercare gli oggetti in vendita
     * @return Numero di oggetti
     */
    private suspend fun numeroOggettiInVenditaPerRaggioDistanza(posizioneUtente: Location, distanzaKmMax: Int): Int {
            return recuperaAnnunciFiltrati(null, null, null, null, posizioneUtente, distanzaKmMax).size
    }

    /**
     * Restituisce una lista di utenti ordinati in base al punteggio delle recensioni, in maniera decrescente.
     *
     * @author Amato Luca
     * @return Lista di oggetti Utente
     */
    suspend fun classificaUtentiRecensitiConVotoPiuAlto(): List<Utente> {

        var myUtenti = recuperaUtenti(auth.uid!!)

        myUtenti = myUtenti.sortedByDescending { utente -> runBlocking{utente.recuperaPunteggioRecensioniFirebase()}} as ArrayList<Utente>

        return myUtenti
    }

    /**
     * Calcola il tempo medio degli Annunci che sono stati venduti
     *
     * @author Amato Luca
     * @return tempo medio annunci venduti
     */
    suspend fun calcolaTempoMedioAnnunciVenduti(): Double{

        val myUtenti = recuperaUtenti(auth.uid!!).stream().filter{ utente-> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() != 0.0 } }.toList()

        return myUtenti.sumOf { utente -> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() } } / myUtenti.size
    }

    /**
     * Crea una nuova categoria
     *
     * @author Amato Luca
     * @param nomeNuovaCategoria Nome della nuova categoria
     */
    suspend fun creaNuovaCategoriaFirebaseFirestore(nomeNuovaCategoria: String){
        database.collection("categoria").add("nome" to  nomeNuovaCategoria).await()
    }

    /**
     * Modifica una categoria esistente
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria
     * @param nomeAggiornatoCategoria Nome aggiornato della categoria
     */
    suspend fun modificaCategoriaFirebaseFirestore(idCategoria: String,nomeAggiornatoCategoria: String){
        database.collection("categoria").document(idCategoria).update("nome", nomeAggiornatoCategoria).await()
    }

    /**
     * Modifica una sottocategoria esistente
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria a cui appartiene la sottocategoria
     * @param idSottocategoria Identificativo della sottocategoria
     * @param nomeAggiornatoSottocategoria Nome aggiornato della sottocategoria
     */
    suspend fun modificaSottocategoriaFirebaseFirestore(idCategoria: String, idSottocategoria: String, nomeAggiornatoSottocategoria: String){
        database.collection("categoria").document(idCategoria).collection("sottocategoria").document(idSottocategoria).update("nome",nomeAggiornatoSottocategoria).await()
    }

    /**
     * Crea una nuova sottocategoria
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria a cui appartiene la sottocategoria
     * @param nomeNuovaSottocategoria Nome della nuova sottocategoria
     */
    suspend fun creaNuovaSottocategoriaFirebaseFirestore(idCategoria: String, nomeNuovaSottocategoria: String){
        database.collection("categoria").document(idCategoria).collection("sottocategoria").add("nome" to nomeNuovaSottocategoria).await()
    }
}

