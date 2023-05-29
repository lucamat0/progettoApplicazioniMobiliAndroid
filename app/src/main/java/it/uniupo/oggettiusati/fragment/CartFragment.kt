package it.uniupo.oggettiusati.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class CartFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentRootView = inflater.inflate(R.layout.fragment_cart, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView

        return fragmentRootView //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        runBlocking {
            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview_cart)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(recuperaAnnunciCarrelloFirebaseFirestore(auth.uid!!), R.layout.card_view_remove_buy_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

        Toast.makeText(activity, "Sei nella sezione carrello", Toast.LENGTH_SHORT).show()

    }

    //1) Inserisce articolo nel carrello, ci possono essere più articoli.
    //2) Nel momento in cui invia la richiesta di acquisto gli vengono scalati i money
    //3) Il venditore accetta o no, nel caso in cui non venisse accettato i soldi vengono riaccreditati.

    companion object {

        val database = Firebase.firestore

        private suspend fun isAcquistabile(idUtente: String, prezzoAcquisto: Double): Boolean {
            return saldoAccount(idUtente) >= prezzoAcquisto
        }

        private suspend fun saldoAccount(idUtente: String): Double{

            val myCollection = database.collection("utente")

            val myCollectionTransazioni = myCollection.document(idUtente).collection("transazione")

            val query = myCollectionTransazioni.get().await()

            var saldoAccount = 0.0
            for(myTransazione in query.documents){

                val tipo = myTransazione.get("tipo") as Boolean

                //Log.d("SALDO ACCOUNT", myTransazione.id + "tipo: "+ tipo.toString())

                //true -> ricarica
                if(tipo)
                    saldoAccount += myTransazione.getDouble("importo")!!
                else
                    saldoAccount -= myTransazione.getDouble("importo")!!
            }

            return saldoAccount
        }

        suspend fun inserisciAnnuncioCarrelloFirebaseFirestore(
            userId: String,
            annuncioId: String
        ){

            val myCollection = database.collection("utente")

            val myDocumento = myCollection.document(userId)

            val myCollectionCarrello = myDocumento.collection("carrello")

            val dataOraAttuale = Date().time

            val myElementoCarrello = hashMapOf(
                "dataOraAttuale" to dataOraAttuale
            )

            //aggiungo un documento che ha id uguale a quello del annuncio
            myCollectionCarrello.document(annuncioId).set(myElementoCarrello).await()
        }

        suspend fun eliminaAnnuncioCarrelloFirebaseFirestore(userId : String, elementoCarrelloId: String){

            val myCollection = database.collection("utente")

            val myDocumento = myCollection.document(userId)

            val myCollectionCarrello = myDocumento.collection("carrello")

            val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

            myDocumentCarrello.delete().await()
        }

        suspend fun salvaTransazioneSuFirestoreFirebase(idUtente: String, importo: Double, tipoTransazione: Boolean): String{

            val myCollection = database.collection("utente")

            val myDocumentUtente = myCollection.document(idUtente)

            val myCollectionTransazioneUtente = myDocumentUtente.collection("transazione")

            //Genero un timestamp
            val dataOraAttuale = Date().time

            val myTransazione = hashMapOf(
                "importo" to importo,
                "dataOraAttuale" to dataOraAttuale,
                //tipoTransazione = true -> ricarica, tipoTransazione = false -> acquisto
                "tipo" to tipoTransazione
            )

            return myCollectionTransazioneUtente.add(myTransazione).await().id
        }
    }

    private suspend fun recuperaAnnunciCarrelloFirebaseFirestore(userId: String): HashMap<String, Annuncio> {

        val myCollection = database.collection("utente")

        val myDocument = myCollection.document(userId)

        val myElementiCarrello = myDocument.collection("carrello").get().await()

        val myHashMap = HashMap<String, Annuncio>()

        if(myElementiCarrello.size() > 0) {

            val myCollectionAnnuncio = database.collection(Annuncio.nomeCollection)

            for (myElemento in myElementiCarrello.documents) {

                val myDocumentAnnuncio =
                    myCollectionAnnuncio.document((myElemento.get("annuncioId") as String)).get()
                        .await()

                val myAnnuncio = UserLoginActivity.documentoAnnuncioToObject(myDocumentAnnuncio)

                myHashMap[myAnnuncio.getAnnuncioId()] = myAnnuncio
            }
        }
        return myHashMap
    }

    private suspend fun inviaRichiestaAcqiustoAnnuncio(idUtente: String, myAnnuncio: Annuncio): Boolean{

        if(isAcquistabile(idUtente,myAnnuncio.getPrezzo())){

            salvaTransazioneSuFirestoreFirebase(idUtente,myAnnuncio.getPrezzo(),false)

            myAnnuncio.setRichiesta(idUtente)
            return true
        }
        return false

    }

    //---INIZIO da spostare nel activity recensione ---

    //Questo metodo, avrá un voto nella recensione valido, per una maggiore usabilitá si aggiunge comunque il controllo del voto, compreso tra 1 e 5/
    suspend fun inserisciRecensioneSuFirebaseFirestore(
        titoloRecensione: String,
        descrizioneRecensione: String,
        votoAlUtente: Int,
        idUtenteRecensito: String
    ): String? {

        //se il voto del utente si trova tra 1 e 5 allora inserisci la recensione...
        if(votoAlUtente in 1..5) {

            val myCollectionUtente = database.collection("utente")

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

    //--- FINE da spostare nel activity recensione ---
}
