package it.uniupo.oggettiusati.fragment

import android.content.Context
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class FavoritesFragment(private val isAdmin: Boolean) : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()

    //--- HashMap che mi memorizza gli annunci che devo mostrare ---
    private var myAnnunciPreferiti = HashMap<String, Annuncio>()

    companion object {

        private val database = Firebase.firestore

        private var isFirstTime = true

        //--- Listener che mi avvisa quando uno degli annunci, che ho messo nei preferiti cambia ---
        private var listenerPreferiti: ListenerRegistration? = null

        suspend fun recuperaAnnunciPreferitiFirebaseFirestore(userId: String, context: Context): java.util.HashMap<String, Annuncio> {

            val myCollectionUtente = Firebase.firestore.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumentUtente = myCollectionUtente.document(userId)

            val query = myDocumentUtente.collection("preferito")

            val myDocumentiPreferiti = query.get().await()

            //--- HashMap che mi memorizza gli annunci preferiti, del utente loggato e invia una notifica quando varia uno di questi. ---
            var myAnnunciPreferiti = java.util.HashMap<String, Annuncio>()

            if(myDocumentiPreferiti.documents.size > 0) {

                val myListaId = mutableListOf<String>()

                for (myPreferito in myDocumentiPreferiti.documents)
                    myListaId.add(myPreferito.get("annuncioId") as String)

                val myCollectionAnnuncio = database.collection(Annuncio.nomeCollection)

                val queryPreferiti = myCollectionAnnuncio.whereIn(FieldPath.documentId(), myListaId)

                myAnnunciPreferiti = UserLoginActivity.recuperaAnnunci(queryPreferiti.get().await().documents.toSet())

                //Rimuovo, il possibile listener che avevo inserito precedentemnte, per definirne uno nuovo,
                //sulla base dei possibili nuovi elementi
                listenerPreferiti?.remove()

                isFirstTime = true

                listenerPreferiti = queryPreferiti.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Query", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    else if(!isFirstTime) {
                        for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                            //Log.d("CAMBIO DOCUMENTO", "Il documento ${myDocumentoAnnuncio.document.id} è cambiato!")

                            Toast.makeText(
                                context,
                                "Il documento ${myDocumentoAnnuncio.document.id} è cambiato!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    else
                        isFirstTime = false
                }
            }

            return myAnnunciPreferiti
        }

        suspend fun inserisciAnnuncioPreferitoFirebaseFirestore(userId: String, annuncioId: String, context: Context){

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumento = myCollection.document(userId)

            val myCollectionPreferito = myDocumento.collection("preferito")

            val dataOraAttuale = Date().time

            val myElementoPreferito = hashMapOf(
                "annuncioId" to annuncioId,
                "dataOraAttuale" to dataOraAttuale
            )

            myCollectionPreferito.document(annuncioId).set(myElementoPreferito).await()

            recuperaAnnunciPreferitiFirebaseFirestore(userId, context)
        }

        suspend fun eliminaAnnuncioPreferitoFirebaseFirestore(userId : String, elementoCarrelloId: String, context: Context){

            val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

            val myDocumento = myCollection.document(userId)

            val myCollectionCarrello = myDocumento.collection("preferito")

            val myDocumentCarrello = myCollectionCarrello.document(elementoCarrelloId)

            myDocumentCarrello.delete().await()

            recuperaAnnunciPreferitiFirebaseFirestore(userId, context)
        }
    }

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

            //Ogni volta che il mio fragment viene messo in primo piano recupero i miei annunci preferiti
            myAnnunciPreferiti = recuperaAnnunciPreferitiFirebaseFirestore(auth.uid!!, requireActivity())

            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(myAnnunciPreferiti, R.layout.card_view_remove_design, isAdmin)

            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }
        Toast.makeText(activity, "Sei nella sezione preferiti", Toast.LENGTH_SHORT).show()
    }
}
