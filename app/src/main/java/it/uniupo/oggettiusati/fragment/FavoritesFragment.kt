package it.uniupo.oggettiusati.fragment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.DettaglioOggettoActivity
import it.uniupo.oggettiusati.adapter.CustomAdapter
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date

class FavoritesFragment() : Fragment() {

    //--- Inizio informazioni per il collegamento con firebase firestore ---
    val auth = FirebaseAuth.getInstance()

    //--- HashMap che mi memorizza gli annunci che devo mostrare ---
    private var myAnnunciPreferiti = HashMap<String, Annuncio>()

    companion object {

        lateinit var notificaManager: NotificationManager
        var idChannelPreferiti = "0"

        fun showNotificationFromCompanion(fragment: Context) {
            notificaManager = fragment.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        private fun createNotificationChannel(id: String, name: String, description: String) {

            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(id, name, importance)

            channel.description = description

            notificaManager?.createNotificationChannel(channel)
        }

        private val database = Firebase.firestore

        private var isFirstTime = true

        //--- Listener che mi avvisa quando uno degli annunci, che ho messo nei preferiti cambia ---
        private var listenerPreferiti: ListenerRegistration? = null

        /**
         * Recupera gli annunci preferiti dall'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @param context Contesto dell'applicazione
         * @return HashMap contenente gli annunci preferiti dell'utente con identificativo dell'annuncio come chiave e l'oggetto Annuncio come valore
         */
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

                showNotificationFromCompanion(context)

                createNotificationChannel(idChannelPreferiti, "Preferiti", "Notifica la modifica di un annuncio preferito" )

                listenerPreferiti = queryPreferiti.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Query", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    else if(!isFirstTime) {
                        for (myDocumentoAnnuncio in snapshot!!.documentChanges) {

                            runBlocking {

                                showNotificationFromCompanion(context)

                                val intent =
                                    Intent(context, DettaglioOggettoActivity::class.java)

                                intent.putExtra(
                                    "annuncio",
                                    UserLoginActivity.documentoAnnuncioToObject(myDocumentoAnnuncio.document)
                                )
                                intent.putExtra("isAdmin", false)

                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    myDocumentoAnnuncio.document.id.hashCode() and Int.MAX_VALUE,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )

                                val notification = Notification.Builder(context, idChannelPreferiti)
                                    .setContentTitle("Preferito modificato")
                                    .setContentText("Il documento ${myDocumentoAnnuncio.document.id} nei preferiti è cambiato!")
                                    .setSmallIcon(android.R.drawable.star_big_on)
                                    .setChannelId(idChannelPreferiti)
                                    .setContentIntent(pendingIntent)
                                    .build()
                                notificaManager?.notify(myDocumentoAnnuncio.document.id.hashCode() and Int.MAX_VALUE, notification)
                            }
                        }
                    }
                    else
                        isFirstTime = false
                }
            }


            return myAnnunciPreferiti
        }

        /**
         * Inserisce un annuncio tra i preferiti dell'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @param annuncioId Identificativo dell'annuncio
         * @param context Contesto dell'applicazione
         */
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

        /**
         * Rimuove un annuncio dai preferiti dell'utente specificato
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @param elementoCarrelloId Identificativo dell'annuncio
         * @param context Contesto dell'applicazione
         */
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

        //showNotificationFromCompanion(requireContext())
        //createNotificationChannel(idChannelPreferiti, "Preferiti", "Notifica la modifica di un annuncio preferito" )

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
            requireView().findViewById<TextView>(R.id.info_preferiti).text = if(myAnnunciPreferiti.size > 0) "${myAnnunciPreferiti.size} preferiti" else "Non sono presenti oggetti tra i preferiti"
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(myAnnunciPreferiti, R.layout.card_view_remove_design)

            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

    }
}
