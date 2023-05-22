package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlin.collections.HashMap


class AdminLoginActivity : UserLoginActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        /*
        var username = ""
        val userRef = this.database.collection("utente").document(this.userId)
        userRef.get().addOnSuccessListener { document ->
            if(document != null) {
                username = document.get("nome").toString()
            } else {
                Log.w("document error", "Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }
         */

        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //--- Deve poter eliminare utenti o sospenderli dalle attività ---
    private suspend fun eliminaUtente(userId: String) {

        try {
            val myCollection = this.database.collection("utente")

            val myDocument = myCollection.document(userId)

            myDocument.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ELIMINAZIONE UTENTE", "Documento eliminato con successo")

                    //this.auth.currentUser!!.delete()

                } else {
                    Log.e("ELIMINAZIONE UTENTE", "Errore durante l'eliminazione del documento", task.exception)
                }
            }.await()

            //myDocument.delete().await()
        } catch (e: Exception) {
            Log.e("ERRORE ELIMINA UTENTE", "Durante l'eliminazione del utente c'é stato un errore!", e)
        }

    }

    suspend fun sospendiUtente(userId: String) {

        try {
            val myCollection = this.database.collection("utente")

            myCollection.document(userId).update("sospeso", true).await()

        } catch (e: Exception) {
            Log.e("ERRORE SOSPENDI UTENTE", "Durante la sospensione del utente c'é stato un errore!", e)
        }
    }

    //--- Fine eliminazione e sospensione utente ---

    //--- Accesso a dati statistici ---

    suspend fun numeroOggettiInVendita(): Int {
        return try {

            val myCollection = this.database.collection(Annuncio.nomeCollection)

            val query = myCollection.whereEqualTo("userIdAcquirente", null)

            val myDocuments = query.get().await()

            return myDocuments.size()

        } catch (e: Exception) {
            Log.e(
                "ERRORE NUMERO OGGETTI IN VENDITA",
                "Durante il recupero del numero degli oggetti in vendita c'é stato un errore!",
                e
            )
        }
    }

    suspend fun numeroOggettiInVenditaPerSpecificoUtente(userId: String): Int {
        return try {

            val myCollection = this.database.collection(Annuncio.nomeCollection)

            val query = myCollection.whereEqualTo("userIdAcquirente", null).whereEqualTo("userId", userId)

            val myDocuments = query.get().await()

            return myDocuments.size()

        } catch (e: Exception) {
            Log.e(
                "ERRORE NUMERO OGGETTI IN VENDITA X SPECIFICO UTENTE",
                "Durante il recupero del numero degli oggetti in vendita c'é stato un errore!",
                e
            )
        }
    }

    private suspend fun numeroOggettiInVenditaPerRaggioDistanza(posizione: Location): Int {
        return try {

            val myCollection = this.database.collection(Annuncio.nomeCollection)

            val query = myCollection.whereLessThanOrEqualTo("posizione", posizione)

            val myDocuments = query.get().await()

            return myDocuments.size()

        } catch (e: Exception) {
            Log.e(
                "ERRORE NUMERO OGGETTI IN VENDITA X SPECIFICO UTENTE",
                "Durante il recupero del numero degli oggetti in vendita c'é stato un errore!",
                e
            )
        }
    }

    //Nel caso in cui non ci fosse nessuna recensione non rientra nella lista, una maniera efficente per farlo???
    suspend fun classificaUtentiRecensitiConVotoPiuAlto(): Map<String, Double> {

        val myCollection = this.database.collection("utente")

        //Contiene la qye di tutti gli utenti
        val queryUtente = myCollection.get().await()

        val myHashRecensioni = HashMap<String, Double>()

        for (myDocumento in queryUtente.documents) {

            val queryRecensioni = myCollection.document(myDocumento.id).collection("recensione").get().await()

            //Log.d("RECENSIONI CON VOTO PIÚ ALTO", myDocumento.id)

            val numeroRecensioni = queryRecensioni.documents.size

            //Log.d("RECENSIONI CON VOTO PIÚ ALTO", numeroRecensioni.toString())

            if(numeroRecensioni > 0) {
                var totalePunteggioRecensioni: Double = 0.0
                for (myRecensioni in queryRecensioni.documents) {
                    totalePunteggioRecensioni += (myRecensioni.getLong("votoAlUtente") as Long).toDouble()
                }

                myHashRecensioni[myDocumento.id] = totalePunteggioRecensioni / numeroRecensioni
            } else {
                myHashRecensioni[myDocumento.id] = 0.0
            }
        }

        if(myHashRecensioni.size > 1) {
            //Converto la mia Hash map in lista, utilizzando il toList, ordiniamo gli elementi considerando i valori, in ordine decrescente, poi riconvertiamo la lista in mappa.
            //Alla mia funzione sortedByDescending gli passo una funzione lambda, coppia chiave valore nella lista sono rappresentati come dei valori,
            //visto che non vogliamo considerare la chiave, utilizziamo _ per indicare il precedente valore, restituiamo solo il valore,
            //che viene dato in input alla funzione sortedByDescending, che lo considera per per l'ordinamento.
            return  myHashRecensioni.toList().sortedByDescending { (_, value) -> value }.toMap()
        } else
            return myHashRecensioni
    }

    suspend fun calcolaTempoMedioAnnunciUtenteVenduto(userId: String): Double? {

        val myCollection = this.database.collection(Annuncio.nomeCollection)

        val query = myCollection.whereEqualTo("userId", userId).whereNotEqualTo("userIdAcquirente",null)

        val myDocuments = query.get().await()

        val numeroDocumenti = myDocuments.size()

        if(numeroDocumenti > 0) {

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
        } else
            return null
    }
}
