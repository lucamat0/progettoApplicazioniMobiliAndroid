package it.uniupo.oggettiusati.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import it.uniupo.oggettiusati.adapter.CustomAdapter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class OwnerObjecsFragment(private val isAdmin: Boolean) : Fragment() {
    private val database = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_owner_objects, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView

        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview_owner_objects)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)

            //Ogni volta che il mio fragment viene messo in primo piano recupero i miei annunci preferiti
            val annunciProprietario = recuperaMieiAnnunci(auth.uid!!)
            requireView().findViewById<TextView>(R.id.info_personal).text = if(annunciProprietario.size > 0) "Hai ${annunciProprietario.size} oggetti " else "Non hai ancora inserito tuoi oggetti"
            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(annunciProprietario, R.layout.card_view_design, isAdmin)

            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }
    }


    /**
     * Recupera gli annunci dell'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @return  HashMap contenente gli annunci miei con l'identificativo dell'annuncio come chiave e l'oggetto Annuncio come valore
     */
    private suspend fun recuperaMieiAnnunci(userId: String): HashMap<String, Annuncio> {

        val documentoAnnunci = database.collection(Annuncio.nomeCollection).whereEqualTo("userId", userId).get().await()

        val myAnnunci = HashMap<String, Annuncio>()

        for(myDocumento in documentoAnnunci.documents){
            val myAnnuncio = UserLoginActivity.documentoAnnuncioToObject(myDocumento)

            myAnnunci[myAnnuncio.getAnnuncioId()] = myAnnuncio
        }

        return myAnnunci
    }


}
