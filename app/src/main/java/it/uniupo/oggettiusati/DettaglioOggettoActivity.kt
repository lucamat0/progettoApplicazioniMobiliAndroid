package it.uniupo.oggettiusati

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class DettaglioOggettoActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oggetto)

        val myAnnuncio: Annuncio = intent.getParcelableExtra<Annuncio>("annuncio")!!

        findViewById<TextView>(R.id.nome).text = myAnnuncio.getTitolo()
        findViewById<TextView>(R.id.categoria).text = myAnnuncio.getCategoria()
        findViewById<TextView>(R.id.descrizione).text = myAnnuncio.getDescrizione()
        findViewById<TextView>(R.id.prezzo).text = myAnnuncio.getPrezzoToString()

        //--- Inizio ci sevono con amministratore o proprietario ---
        //val statoOgg = myAnnuncio!!.getStato()
        //findViewById<Spinner>(R.id.stato).setSelection(if (statoOgg == 0) 3 else if (statoOgg == 1) 2 else if (statoOgg == 2) 1 else 0)
        //findViewById<Switch>(R.id.spedizione).isChecked = myAnnuncio!!.getDisponibilitaSpedire()
        //--- Fine ci servono con amministratore o proprietario ---

        // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
        findViewById<TextView>(R.id.stato).text = if(myAnnuncio.getStato()==0) "Stato: difettoso" else if(myAnnuncio.getStato()==1) "Stato: qualche lieve difetto" else if(myAnnuncio.getStato()==2) "Stato: usato ma in perfette condizioni" else "Stato: nuovo"
        findViewById<TextView>(R.id.spedizione).text = if(myAnnuncio.getDisponibilitaSpedire()) "Spedizione: Si" else "Spedizione: No"

        findViewById<Button>(R.id.aggiungi_carrello).setOnClickListener {
            runBlocking { inserisciAnnuncioCarrelloFirebaseFirestore(auth.uid!!,myAnnuncio.getAnnuncioId()) }
        }
    }

    suspend fun inserisciAnnuncioCarrelloFirebaseFirestore(userId : String, annuncioId: String): String {

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionCarrello = myDocumento.collection("carrello")

        val dataOraAttuale = Date().time

        val myElementoCarrello = hashMapOf(
            "annuncioId" to annuncioId,
            "dataOraAttuale" to dataOraAttuale
        )

        return myCollectionCarrello.add(myElementoCarrello).await().id
    }
}