package it.uniupo.oggettiusati

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class FavoritesFragment : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    //--- HashMap che mi memorizza gli annunci preferiti, del utente loggato e invia una notifica quando varia uno di questi. ---
    var myAnnunciPreferiti = HashMap<String, Annuncio>()
    var myListenerAnnunciPreferiti: ListenerRegistration? = null

    //HashMap che mi memorizza gli annunci che devo mostrare, a seconda della pagina in cui mi trovo mi vengono mostrati i 10 elementi
    var myAnnunciHome = HashMap<String, Annuncio>()

    private var ultimoAnnuncioId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_favorites, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView

        return fragmentRootView //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        runBlocking {
            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview_favorites)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(recuperaAnnunciPreferitiFirebaseFirestore(auth.uid!!), R.layout.card_view_remove_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

        Toast.makeText(activity, "Sei nella sezione preferiti", Toast.LENGTH_SHORT).show()

    }

    suspend fun recuperaAnnunciPreferitiFirebaseFirestore(userId: String): HashMap<String, Annuncio> {

        val myCollectionUtente = this.database.collection("utente")

        val myDocumentUtente = myCollectionUtente.document(userId)

        val myDocumentiPreferiti = myDocumentUtente.collection("preferito").get().await()

        if(myDocumentiPreferiti.documents.size > 0) {

            aggiornaListenerPreferiti(myDocumentiPreferiti)

            return myAnnunciPreferiti
        }
        return HashMap()
    }

    //Listener dei preferiti si aggiorna quando, inseriamo un nuovo elemento nei preferiti, oppure quando andiamo a recuperare i preferiti.
    //Da qui, ogni modifica effettuata sugli annunci ci viene notificata, provvisoriamente con un Toast.
    private suspend fun aggiornaListenerPreferiti(myDocumentiPreferiti: QuerySnapshot) {

        val myListaId = mutableListOf<String>()

        for (myPreferito in myDocumentiPreferiti.documents)
            myListaId.add(myPreferito.get("annuncioId") as String)

        val myCollectionAnnuncio = this.database.collection(Annuncio.nomeCollection)

        //--- Inizio informazioni per il mantenimento delle informazioni, filtrate, aggiornate ---
        val query = myCollectionAnnuncio.whereIn(FieldPath.documentId(), myListaId)

        myAnnunciPreferiti = recuperaAnnunci(query.get().await())

        myListenerAnnunciPreferiti?.remove()

        myListenerAnnunciPreferiti = subscribeRealTimeDatabase(query, myAnnunciPreferiti, true)
    }

    //In base alla query che viene passata, questa funzione mi filtra gli annunci e mi ritorna un arrayList di annunci.
    private fun recuperaAnnunci(myDocumenti: QuerySnapshot): HashMap<String, Annuncio> {

        //Inizializzo HashMap vuota, la chiave sarà il suo Id, l'elemento associato alla chiave sarà oggetto Annuncio.
        val myAnnunci = HashMap<String, Annuncio>()

        for (myDocumentoAnnuncio in myDocumenti.documents) {
            myAnnunci[myDocumentoAnnuncio.id] = documentoAnnuncioToObject(myDocumentoAnnuncio)

            ultimoAnnuncioId = myDocumentoAnnuncio.id
        }

        return myAnnunci
    }

    fun documentoAnnuncioToObject(myDocumentoAnnuncio: DocumentSnapshot): Annuncio {

        val userIdAcquirente: String? = myDocumentoAnnuncio.get("userIdAcquirente") as String?

        val timeStampFineVendita: Long? = myDocumentoAnnuncio.getLong("timeStampFineVendita")

        return Annuncio(
            myDocumentoAnnuncio.get("userId") as String,
            myDocumentoAnnuncio.get("titolo") as String,
            myDocumentoAnnuncio.get("descrizione") as String,
            myDocumentoAnnuncio.get("prezzo") as Double,
            (myDocumentoAnnuncio.getLong("stato") as Long).toInt(),
            myDocumentoAnnuncio.getBoolean("disponibilitaSpedire") as Boolean,
            myDocumentoAnnuncio.get("categoria") as String,
            myDocumentoAnnuncio.getGeoPoint("posizione") as GeoPoint,
            myDocumentoAnnuncio.getLong("timeStampInizioVendita") as Long,
            timeStampFineVendita,
            userIdAcquirente,
            myDocumentoAnnuncio.id)
    }

    fun subscribeRealTimeDatabase(query: Query, myAnnunci: HashMap<String, Annuncio>, preferiti: Boolean): ListenerRegistration {

        val  listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Query", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                val a = documentoAnnuncioToObject(myDocumentoAnnuncio.document)

                //Log.d("CAMBIO DOCUMENTO", "Il documento ${a.toString()} è cambiato!")

                myAnnunci[a.getAnnuncioId()] = a

                if(preferiti)
                    //Toast.makeText(activity, "Il documento ${a.getAnnuncioId()} è cambiato!", Toast.LENGTH_LONG).show()
                else {
//                    val adapter = CustomAdapter(myAnnunciHome, R.layout.card_view_remove_design)
//
//                    val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview)
//
//                    //setting the Adapter with the recyclerView
//                    recyclerVu?.adapter = adapter
                }

                //Log.d("CONTENUTO ARRAYLIST", myAnnunciPreferiti.toString())
            }
        }

        //Log.d("CONTENUTO ARRAYLIST", myAnnunci.toString())

        return listenerRegistration
    }

    suspend fun eliminaAnnuncioPreferitoFirebaseFirestore(userId : String, elementoCarrelloId: String){

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionCarrello = myDocumento.collection("preferito")

        val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

        myDocumentCarrello.delete().await()
    }

    suspend fun inserisciAnnuncioPreferitoFirebaseFirestore(userId : String, annuncioId: String): String {

        val myCollection = this.database.collection("utente")

        val myDocumento = myCollection.document(userId)

        val myCollectionPreferito = myDocumento.collection("preferito")

        val dataOraAttuale = Date().time

        val myElementoPreferito = hashMapOf(
            "annuncioId" to annuncioId,
            "dataOraAttuale" to dataOraAttuale
        )

        val idPreferito = myCollectionPreferito.add(myElementoPreferito).await().id

        val myDocumentiPreferiti = myCollectionPreferito.get().await()

        if(myDocumentiPreferiti.documents.size>0)
            aggiornaListenerPreferiti(myDocumentiPreferiti)

        return idPreferito
    }

}
