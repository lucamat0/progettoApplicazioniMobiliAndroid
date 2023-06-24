package it.uniupo.oggettiusati

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.RecensioniAdapter
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.LinkedList

class RecensioniActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recensioni)

        runBlocking{
            supportActionBar?.setTitle(UserLoginActivity.recuperaUtente(auth.uid!!).nome)
        }

//        recensioniDaMostrare.put("1",Recensione("bel prdotto", "funziona molto bene e mi piace", "marco", 1685238057))
//        recensioniDaMostrare.put("2",Recensione("grande occasione", "qualita' prezzo ok, durevolezza meno", "johnny", 1685228277))
//        recensioniDaMostrare.put("3",Recensione("Ottime cuffie, fatta eccezione per la manopola del volume.",
//            "Batte le mie altre cuffie e il prezzo è buono. Si adatta perfettamente, non ho usato il microfono. Il mio ultimo auricolare di marca simile si è rotto, perché l'ho sollevato da un lato da solo e la plastica ha iniziato a rompersi fino a quando non ha ceduto. Afferra sempre l'auricolare con entrambe le mani.",
//            "Philip Ray Bessa", 1682132400000))
//        recensioniDaMostrare.put("4",Recensione("Adoro il design ma deluso dal colore",
//            "Ho appena ricevuto il set la settimana scorsa e penso di essere un po' deluso dal colore. Ho ordinato la crema come mostrato nel sito web ma quello che è arrivato è un vero pistacchio. Lo dice anche nella confezione. Non avevo bisogno di un pistacchio con l'arredamento della mia casa, ma immagino che il colore stia crescendo su di me. Non ho ancora deciso se lo sostituirò, non ho ancora iniziato a usarlo. Dato un 4 stelle perché altrimenti sembra un set di stoviglie robusto e abbastanza resistente ai graffi. Il design del labbro rialzato è decisamente molto bello, il motivo per cui ho scelto questo prodotto.",
//            "Nik", 1683132403760))
//        recensioniDaMostrare.put("5",Recensione("Immagine e prodotto corrispondono perfettamente", "stavo cercando un fazzoletto di rame e un bidone della spazzatura che sembra fantastico e funziona bene", "Caterina L. Pianta", 1684532403760))

        val idVenditore = intent.extras?.getString("idVenditore")
        runBlocking {//fill Recyclerview
            val recyclerVu = findViewById<RecyclerView>(R.id.recyclerview_recensioni)
            recyclerVu?.layoutManager = LinearLayoutManager(this@RecensioniActivity)
            val adapter = RecensioniAdapter(recuperaRecensioniFirebaseFirestore(idVenditore!!))
            recyclerVu?.adapter = adapter
        }
    }

    private suspend fun recuperaRecensioniFirebaseFirestore(userId: String): LinkedList<Recensione> {
        val queryRecensioni = database.collection(UserLoginActivity.Utente.nomeCollection).document(userId).collection("recensione").get().await()
        val myRecensioni = LinkedList<Recensione>()
        for (myRecensione in queryRecensioni.documents) {
            myRecensioni.add(Recensione(
                myRecensione.get("titoloRecensione") as String,
                myRecensione.get("descrizioneRecensione") as String,
                myRecensione.getLong("votoAlUtente")!!.toInt(),
                UserLoginActivity.recuperaUtente(myRecensione.get("idUtenteEspresso") as String).getNomeCognome()))
        }
        return myRecensioni
    }

//    -- DA SPOSTARE IN INSERISCI RECENSIONE

    //Questo metodo, avrá un voto nella recensione valido, per una maggiore usabilitá si aggiunge comunque il controllo del voto, compreso tra 1 e 5/
    suspend fun inserisciRecensioneSuFirebaseFirestore(
        titoloRecensione: String,
        descrizioneRecensione: String,
        votoAlUtente: Int,
        idUtenteRecensito: String
    ): String? {

        //se il voto del utente si trova tra 1 e 5 allora inserisci la recensione...
        if(votoAlUtente in 1..5) {

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
        else
            return null
    }



    data class Recensione(val titoloRecensione: String, val descrizioniRecensione: String, val votoAlUtente :Int, val idUtenteEspresso: String)
}