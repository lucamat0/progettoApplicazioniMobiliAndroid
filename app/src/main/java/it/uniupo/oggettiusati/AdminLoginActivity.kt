package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
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


class AdminLoginActivity : UserLoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //--- Deve poter eliminare utenti o sospenderli dalle attività ---
    private suspend fun eliminaUtente(userId: String) {
        database.collection(Utente.nomeCollection).document(userId).update("eliminato", true).await()
    }

    suspend fun sospendiUtente(userId: String) {
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", true).await()
    }

    suspend fun attivaUtente(userId: String){
        database.collection(Utente.nomeCollection).document(userId).update("sospeso", false).await()
    }

    //--- Fine eliminazione e sospensione utente ---

    //--- Accesso a dati statistici ---

    val myAnnunciVenduti = database.collection(Annuncio.nomeCollection).whereEqualTo("venduto", false)

    suspend fun numeroOggettiInVendita(): Int {
        return myAnnunciVenduti.get().await().size()
    }

    suspend fun numeroOggettiInVenditaPerSpecificoUtente(userId: String): Int {
           return myAnnunciVenduti.whereEqualTo("userId", userId).get().await().size()
    }

    private suspend fun numeroOggettiInVenditaPerRaggioDistanza(posizioneUtente: Location, distanzaMax: Int): Int {

            val myOggettiInVenditaRef = UserLoginActivity.definisciQuery(null,null,null)

            return HomeFragment.recuperaAnnunciLocalizzazione(posizioneUtente, distanzaMax, UserLoginActivity.recuperaAnnunci(myOggettiInVenditaRef)).size
    }

    suspend fun classificaUtentiRecensitiConVotoPiuAlto(): List<Utente> {

        var myUtenti = recuperaUtenti(auth.uid!!)

        myUtenti = myUtenti.sortedByDescending { utente -> runBlocking{utente.recuperaPunteggioRecensioniFirebase()}} as ArrayList<Utente>

        return myUtenti
    }

    suspend fun calcolaTempoMedioAnnunciVenduti(): Double{

        var myUtenti = recuperaUtenti(auth.uid!!).stream().filter{ utente-> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() != 0.0 } }.toList()

        return myUtenti.sumOf { utente -> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() } } / myUtenti.size
    }
}

