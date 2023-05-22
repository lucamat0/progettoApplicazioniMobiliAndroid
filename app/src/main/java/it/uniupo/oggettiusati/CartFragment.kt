package it.uniupo.oggettiusati

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.Date

class CartFragment : Fragment() {
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //...
//    }

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
            val adapter = CustomAdapter(recuperaAnnunciCarrelloFirebaseFirestore(auth.uid!!), R.layout.card_view_remove_design)
            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }

        Toast.makeText(activity, "Sei nella sezione carrello", Toast.LENGTH_SHORT).show()

    }

    suspend fun recuperaAnnunciCarrelloFirebaseFirestore(userId: String): HashMap<String, Annuncio> {

        val myCollection = this.database.collection("utente")

        val myDocument = myCollection.document(userId)

        val myElementiCarrello = myDocument.collection("carrello").get().await()

        if(myElementiCarrello.size() > 0) {

            val myCollectionAnnuncio = this.database.collection(Annuncio.nomeCollection)
            val myHashMap = HashMap<String, Annuncio>()

            for (myElemento in myElementiCarrello.documents) {

                val myDocumentAnnuncio =
                    myCollectionAnnuncio.document((myElemento.get("annuncioId") as String)).get()
                        .await()

                val myAnnuncio = documentoAnnuncioToObject(myDocumentAnnuncio)

                myHashMap[myAnnuncio.getAnnuncioId()] = myAnnuncio
            }
            return myHashMap
        }
        return HashMap()
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

}
