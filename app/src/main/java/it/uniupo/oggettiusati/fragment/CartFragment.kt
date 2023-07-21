package it.uniupo.oggettiusati.fragment

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.RicaricaActivity
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class CartFragment() : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentRootView = inflater.inflate(R.layout.fragment_cart, container, false)

        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView
        fragmentRootView.findViewById<Button>(R.id.ricarica).setOnClickListener {
            startActivity(Intent(activity, RicaricaActivity::class.java))
        }

        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()

        runBlocking {

            val importo = "Budget: ${String.format("%.2f", saldoAccount(auth.uid!!)) + "€"}"
            view?.findViewById<TextView>(R.id.saldo_account)?.text = importo

            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview_cart)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)

            //this will pass the ArrayList to our Adapter
            val myAnnunciCarrello = recuperaAnnunciCarrelloFirebaseFirestore(auth.uid!!)

            requireView().findViewById<TextView>(R.id.info_carrello).text = if(myAnnunciCarrello.size > 0) "${myAnnunciCarrello.size} oggetti nel carrello" else "Non sono presenti oggetti nel carrello"
            val adapter = CustomAdapter(myAnnunciCarrello, R.layout.card_view_remove_buy_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }
    }

    //1) Inserisce articolo nel carrello, ci possono essere più articoli.
    //2) Nel momento in cui invia la richiesta di acquisto gli vengono scalati i money
    //3) Il venditore accetta o no, nel caso in cui non venisse accettato i soldi vengono riaccreditati.

    companion object {

        val database = Firebase.firestore

        /**
         * Verifica se utente specificato ha un saldo sufficente sul proprio account
         *
         * @author Amato Luca
         * @param idUtente Identificativo dell'utente da verificare
         * @param prezzoAcquisto Prezzo dell'acquisto da confrontare con il saldo dell'account
         * @return true se il saldo del account e' sufficente altrimenti false
         */
        private suspend fun isAcquistabile(idUtente: String, prezzoAcquisto: Double): Boolean {
            return saldoAccount(idUtente) >= prezzoAcquisto
        }

        /**
         * Recupera il saldo dell'account per l'utente specificato.
         *
         * @author Amato Luca
         * @param idUtente Identificativo dell'utente
         * @return saldo dell'account
         */
        private suspend fun saldoAccount(idUtente: String): Double {

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myCollectionTransazioni = myCollection.document(idUtente).collection("transazione")

            val query = myCollectionTransazioni.get().await()

            var saldoAccount = 0.0
            for (myTransazione in query.documents) {

                val tipo = myTransazione.get("tipo") as Boolean

                //Log.d("SALDO ACCOUNT", myTransazione.id + "tipo: "+ tipo.toString())

                //true -> ricarica
                if (tipo)
                    saldoAccount += myTransazione.getDouble("importo")!!
                else
                    saldoAccount -= myTransazione.getDouble("importo")!!
            }

            return saldoAccount
        }

        /**
         * Inserisce un annuncio nel carrello all'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @param annuncioId Identificativo dell'annuncio
         */
        suspend fun inserisciAnnuncioCarrelloFirebaseFirestore(
            userId: String,
            annuncioId: String
        ) {

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumento = myCollection.document(userId)

            val myCollectionCarrello = myDocumento.collection("carrello")

            val dataOraAttuale = Date().time

            val myElementoCarrello = hashMapOf(
                "dataOraAttuale" to dataOraAttuale
            )

            //aggiungo un documento che ha id uguale a quello del annuncio
            myCollectionCarrello.document(annuncioId).set(myElementoCarrello).await()
        }

        /**
         * Elimina un annuncio dal carrello dell'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @param elementoCarrelloId Identificativo dell'elemento nel carrello
         */
        suspend fun eliminaAnnuncioCarrelloFirebaseFirestore(
            userId: String,
            elementoCarrelloId: String
        ) {

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumento = myCollection.document(userId)

            val myCollectionCarrello = myDocumento.collection("carrello")

            val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

            myDocumentCarrello.delete().await()
        }

        /**
         * Salva una transazione nell'archivio per l'utente specificato
         *
         * @author Amato Luca
         * @param idUtente Identificativo dell'utente
         * @param importo Importo della transazione da salvare
         * @param tipoTransazione true per ricarica e false per acquisto
         */
        suspend fun salvaTransazioneSuFirestoreFirebase(
            idUtente: String,
            importo: Double,
            tipoTransazione: Boolean
        ){

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumentUtente = myCollection.document(idUtente)

            val myCollectionTransazioneUtente = myDocumentUtente.collection("transazione")

            //Genero un timestamp
            val dataOraAttuale = Date().time

            val myTransazione = hashMapOf(
                "importo" to importo,
                "dataOraAttuale" to dataOraAttuale,
                "tipo" to tipoTransazione
            )

            myCollectionTransazioneUtente.add(myTransazione).await()
        }

        /**
         * Verifica se è stata inviata una richiesta per un determinato annuncio
         *
         * @author Amato Luca
         * @param annuncioId Identificativo dell'annuncio
         * @return true se non è stata inviata alcuna richiesta per l'annuncio altrimenti false
         */
        suspend fun isInviataRichiesta(annuncioId: String): Boolean{
            return database.collection(Annuncio.nomeCollection).document(annuncioId).get().await().getString("userIdAcquirente") == null
        }

        /**
         * Recupera gli annunci presenti nel carrello per l'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @return HashMap contenente gli annunci nel carrello con l'identificativo dell'annuncio come chiave e l'oggetto Annuncio come valore
         */
        suspend fun recuperaAnnunciCarrelloFirebaseFirestore(userId: String): HashMap<String, Annuncio> {

            val myHashMap = HashMap<String, Annuncio>()

            recuperaAnnunciRefCarrelloFirebaseFirestore(userId).stream().forEach { doc ->
                runBlocking {
                    val myAnnuncio = UserLoginActivity.documentoAnnuncioToObject(doc)
                    myHashMap[myAnnuncio.getAnnuncioId()] = myAnnuncio
                }
            }

            return myHashMap
        }

        /**
         * Recupera i riferimenti agli annunci presenti nel carrello dell'utente
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @return Un ArrayList contenente i documenti agli annunci
         */
        suspend fun recuperaAnnunciRefCarrelloFirebaseFirestore(userId: String): MutableList<DocumentSnapshot> {

            val myDocumentRef =
                database.collection(UserLoginActivity.Utente.nomeCollection).document(userId)
            val myElementiCarrello = myDocumentRef.collection("carrello").get().await()


            if (myElementiCarrello.size() > 0) {

                val myListaId = mutableListOf<String>()

                for (myCarrello in myElementiCarrello.documents)
                    myListaId.add(myCarrello.id)

                val myCollectionAnnuncio = database.collection(Annuncio.nomeCollection)

                return myCollectionAnnuncio.whereIn(FieldPath.documentId(), myListaId).get().await().documents
            }

            return ArrayList<DocumentSnapshot>()
        }

        /**
         * Invia una richiesta di acquisto per un annuncio da parte di un utente soltanto se ha saldo a sufficenza altrimenti non avviene.la richiesta di acquisto
         *
         * @author Amato Luca
         * @param idUtente Identificativo dell'utente
         * @param myAnnuncio Annuncio per il quale si vuole inviare la richiesta di acquisto
         * @param contesto Contesto dell'applicazione
         * @return True se l'annuncio è acquistabile e la richiesta viene inviata correttamente altrimenti False
         */
        suspend fun inviaRichiestaAcqiustoAnnuncio(idUtente: String, myAnnuncio: Annuncio, contesto: Context) : Boolean {

            Toast.makeText(contesto, "Richiesta inoltrata", Toast.LENGTH_SHORT).show()

            if (isAcquistabile(idUtente, myAnnuncio.getPrezzo())) {
                return myAnnuncio.setRichiesta(idUtente, contesto)
            }
            else {
                Toast.makeText(
                    contesto,
                    "Non hai abbastanza soldi per inoltrare la richiesta di acquisto",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        //26.62
        //26.70
    }
}
