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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import it.uniupo.oggettiusati.adapter.UserAdapter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ChatFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_chat, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView
        return fragmentRootView //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        runBlocking {

            val nomeCognomeUtenti = UserLoginActivity.recuperaUtenti()

            val recyclerViewUtenti = view?.findViewById<RecyclerView>(R.id.recyclerviewUtenti)
            recyclerViewUtenti!!.layoutManager = LinearLayoutManager(activity)

            val adapterUtenti = UserAdapter(nomeCognomeUtenti)
            recyclerViewUtenti.adapter = adapterUtenti
        }

        Toast.makeText(activity, "Sei nella sezione chat", Toast.LENGTH_SHORT).show()
    }

}
