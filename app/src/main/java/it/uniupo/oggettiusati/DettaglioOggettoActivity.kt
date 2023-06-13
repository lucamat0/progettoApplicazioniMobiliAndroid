package it.uniupo.oggettiusati

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.chat.ChatActivity
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.FavoritesFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class DettaglioOggettoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oggetto)

//        supportActionBar?.setDisplayShowTitleEnabled(false)
        runBlocking{
            supportActionBar?.setTitle(UserLoginActivity.recuperaUtente(auth.uid!!).nome)
        }

        val myAnnuncio: Annuncio = intent.getParcelableExtra<Annuncio>("annuncio")!!

        findViewById<TextView>(R.id.nome).text = myAnnuncio.getTitolo()
        findViewById<TextView>(R.id.categoria).text = myAnnuncio.getCategoria()
        findViewById<TextView>(R.id.descrizione).text = myAnnuncio.getDescrizione()
        findViewById<TextView>(R.id.prezzo).text = myAnnuncio.getPrezzoToString()
        var nomeVenditore :String
        val proprietarioAnnuncio = myAnnuncio.getProprietario()
        runBlocking {
            nomeVenditore = "Proprietario: ${UserLoginActivity.recuperaUtente(proprietarioAnnuncio).getNomeCognome()}"
        }
        findViewById<TextView>(R.id.nomeVenditore).text = nomeVenditore

        // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
        findViewById<TextView>(R.id.stato).text = if(myAnnuncio.getStato() == 0) "Stato: difettoso" else if(myAnnuncio.getStato() == 1) "Stato: qualche lieve difetto" else if(myAnnuncio.getStato() == 2) "Stato: usato ma in perfette condizioni" else "Stato: nuovo"
        val spediz = "Spedizione: ${if(myAnnuncio.getDisponibilitaSpedire()) "Si" else "No"}"
        findViewById<TextView>(R.id.spedizione).text = spediz

        val btnRecensioniVenditore = findViewById<Button>(R.id.visualizza_recensioni_venditore)
        btnRecensioniVenditore.setOnClickListener {
            val i = Intent(this, RecensioniActivity::class.java)
            i.putExtra("idVenditore", proprietarioAnnuncio)
            startActivity(i)
        }

        findViewById<Button>(R.id.chatta_con_proprietario).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("nome", nomeVenditore)
            intent.putExtra("uid", proprietarioAnnuncio)
            startActivity(intent)
        }

        if(myAnnuncio.isProprietario(auth.uid.toString())) { //l'annuncio appartiene all'utente autenticato:
            btnRecensioniVenditore.visibility = View.GONE //Must be one of: View.VISIBLE, View.INVISIBLE, View.GONE
            // non e' possibile inserirlo nei preferiti ne metterlo nel carrello per acquistarlo
            findViewById<LinearLayout>(R.id.layout_aggiungi_contatta).visibility = View.GONE

            if(myAnnuncio.getRichiesta()) {
                if(!myAnnuncio.isVenduto()) {
                    Log.d("venduto", "${myAnnuncio.isVenduto()}")
                    findViewById<LinearLayout>(R.id.layout_richiesta).visibility = View.VISIBLE
                    findViewById<Button>(R.id.visualizza_recensioni_acquirente).setOnClickListener {
                        //startActivity(Recensioni.kt)
                    }

                    findViewById<Button>(R.id.accetta).setOnClickListener {
                        val idAcquirente = myAnnuncio.getAcquirente()
                        if(idAcquirente != null) {
                            runBlocking {
                                myAnnuncio.setVenduto(/*idAcquirente*/)
                            }
                        }
                        findViewById<LinearLayout>(R.id.layout_richiesta).visibility = View.GONE
                    }

                    findViewById<Button>(R.id.rifiuta).setOnClickListener {
                        runBlocking { myAnnuncio.setEliminaRichiesta(auth.uid.toString()) }
                        findViewById<Button>(R.id.layout_richiesta).visibility = View.GONE
                    }
                }

            } else { //se non c'è una richiesta e sono il proprietario/admin posso modificare ed eliminare l'oggetto
                findViewById<Button>(R.id.modifica_oggetto).visibility = View.VISIBLE
                // creare funzione e aggiungerlo anche per admin
                findViewById<Button>(R.id.modifica_oggetto).setOnClickListener {
                    runBlocking {
                        //startActivity(Modifica.kt)
                    }
                }

                findViewById<Button>(R.id.elimina_oggetto).visibility = View.VISIBLE
                // creare funzione e aggiungerlo anche per admin
                findViewById<Button>(R.id.elimina_oggetto).setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle("Attenzione")
                        .setMessage("Sei sicuro di voler eliminare l'oggetto?")
                        .setPositiveButton("Si") { dialog :DialogInterface, _:Int ->
                            dialog.dismiss()
                            runBlocking {
//                                eliminaAnnuncioFirebase()
                            }
                        }
                        .setNegativeButton("No") { dialog :DialogInterface, _:Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        } else {
            runBlocking {
                if(isPreferito(auth.uid!!, myAnnuncio.getAnnuncioId())){
                    findViewById<Button>(R.id.aggiungi_preferiti).visibility = View.GONE
                } else {
                    findViewById<Button>(R.id.aggiungi_preferiti).setOnClickListener {
                        runBlocking {
                            //inserisci oggetto nei preferiti
                            FavoritesFragment.inserisciAnnuncioPreferitoFirebaseFirestore(auth.uid!!, myAnnuncio.getAnnuncioId(), this@DettaglioOggettoActivity)
                        }
                        findViewById<Button>(R.id.aggiungi_preferiti).visibility = View.GONE
                    }
                }

                if(isCarrello(auth.uid!!, myAnnuncio.getAnnuncioId())){
                    findViewById<Button>(R.id.aggiungi_carrello).visibility = View.GONE
                } else {
                    findViewById<Button>(R.id.aggiungi_carrello).setOnClickListener {
                        runBlocking {
                            CartFragment.inserisciAnnuncioCarrelloFirebaseFirestore(auth.uid!!, myAnnuncio.getAnnuncioId())
                        }
                        findViewById<Button>(R.id.aggiungi_carrello).visibility = View.GONE
                    }
                }
            }
        }
    }

    suspend fun isPreferito(userId: String, annuncioId: String): Boolean{

        val myCollectionPreferito = this.database.collection("utente").document(userId).collection("preferito")

        for(myDocument in myCollectionPreferito.get().await().documents)
            if (myDocument.get("annuncioId") == annuncioId)
                return true
        return false
    }

    suspend fun isCarrello(userId: String, annuncioId: String): Boolean{

        val myCollectionPreferito = this.database.collection("utente").document(userId).collection("carrello")

        for(myDocument in myCollectionPreferito.get().await().documents)
            if (myDocument.id == annuncioId)
                return true
        return false
    }

}