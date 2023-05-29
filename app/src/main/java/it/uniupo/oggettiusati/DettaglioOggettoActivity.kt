package it.uniupo.oggettiusati

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class DettaglioOggettoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oggetto)

        val myAnnuncio: Annuncio = intent.getParcelableExtra<Annuncio>("annuncio")!!

        findViewById<TextView>(R.id.nome).text = myAnnuncio.getTitolo()
        findViewById<TextView>(R.id.categoria).text = myAnnuncio.getCategoria()
        findViewById<TextView>(R.id.descrizione).text = myAnnuncio.getDescrizione()
        findViewById<TextView>(R.id.prezzo).text = myAnnuncio.getPrezzoToString()

        // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
        findViewById<TextView>(R.id.stato).text = if(myAnnuncio.getStato() == 0) "Stato: difettoso" else if(myAnnuncio.getStato() == 1) "Stato: qualche lieve difetto" else if(myAnnuncio.getStato() == 2) "Stato: usato ma in perfette condizioni" else "Stato: nuovo"
        val spediz = "Spedizione: ${if(myAnnuncio.getDisponibilitaSpedire()) "Si" else "No"}"
        findViewById<TextView>(R.id.spedizione).text = spediz

        findViewById<Button>(R.id.aggiungi_carrello).setOnClickListener {
            runBlocking {
                CartFragment.inserisciAnnuncioCarrelloFirebaseFirestore(auth.uid!!, myAnnuncio.getAnnuncioId())
            }
        }
    }

    suspend fun isPreferito(userId: String, annuncioId: String): Boolean{

        val myCollectionPreferito = this.database.collection("utente").document(userId).collection("preferito")

        for(myDocument in myCollectionPreferito.get().await().documents)
            if (myDocument.id == annuncioId)
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