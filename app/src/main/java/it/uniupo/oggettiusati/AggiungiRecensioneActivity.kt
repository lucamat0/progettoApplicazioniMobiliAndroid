package it.uniupo.oggettiusati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AggiungiRecensioneActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aggiungi_recensione)

        findViewById<Button>(R.id.aggiungi_ricerca).setOnClickListener {
            val titolo = findViewById<EditText>(R.id.titolo_nuova_recensione).text.toString()
            val descrizione = findViewById<EditText>(R.id.descrizione_nuova_recensione).text.toString()
            val voto = findViewById<RatingBar>(R.id.valutazione_utente_recensito).rating

            if(validString(titolo) && validString(descrizione) ){

                val idUtenteRecensito = intent.extras?.getString("idUtenteRecensito")
                if(idUtenteRecensito != null) {
                    runBlocking {
                        val idResult = inserisciRecensioneSuFirebaseFirestore(titolo, descrizione, voto, idUtenteRecensito)
                        if(idResult != null) {
                            val annuncioRif: Annuncio = intent.getParcelableExtra("annuncio", Annuncio::class.java)!!
                            if(annuncioRif.isProprietario(auth.uid!!)) {
                                annuncioRif.setAcquirenteRecensito(auth.uid!!)
                            } else if(annuncioRif.isAcquirente(auth.uid!!)) {
                                annuncioRif.setProprietarioRecensito(auth.uid!!)
                            } else {
                                Log.d("erroreRecensione", " errore Inserimento Recensione: id utente ${auth.uid} , id recensione $idResult")
                            }
                        }
                    }
                }

                startActivity(Intent(this, UserLoginActivity::class.java))
            } else {
                Toast.makeText(this, "Riempi tutti i campi, alcuni sono vuoti.", Toast.LENGTH_LONG).show()
            }
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

    companion object {
        internal fun validString(titolo: String): Boolean {
            return titolo.isNotBlank() //&& titolo.isNotEmpty()
        }
    }

    //    -- DA SPOSTARE IN INSERISCI RECENSIONE

    //Questo metodo, avrá un voto nella recensione valido, per una maggiore usabilitá si aggiunge comunque il controllo del voto, compreso tra 1 e 5/
    /**
     * Inserisce una recensione su Firebase
     *
     * @author Amato Luca
     * @param titoloRecensione Titolo della recensione
     * @param descrizioneRecensione Descrizione della recensione
     * @param votoAlUtente Voto assegnato all'utente nella recensione
     * @param idUtenteRecensito Identificativo dell'utente recensito
     * @return Identificativo della recensione se il voto e' valido altrimenti null
     */
    private suspend fun inserisciRecensioneSuFirebaseFirestore(
        titoloRecensione: String,
        descrizioneRecensione: String,
        votoAlUtente: Float,
        idUtenteRecensito: String
    ): String? {

        //se il voto del utente si trova tra 1 e 5 allora inserisci la recensione...
        if(votoAlUtente in 1.0..5.0) {

            val myCollectionUtente = CartFragment.database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumento = myCollectionUtente.document(idUtenteRecensito)

            val myCollectionRecensione = myDocumento.collection("recensione")

            val myRecensione = hashMapOf(
                "titoloRecensione" to titoloRecensione,
                "descrizioneRecensione" to descrizioneRecensione,
                "votoAlUtente" to votoAlUtente,
                "idUtenteEspresso" to this.auth.uid
            )

            return myCollectionRecensione.add(myRecensione).await().id
        }
        //se il voto, assegnato dal utente, non é valido...
        return null
    }

}