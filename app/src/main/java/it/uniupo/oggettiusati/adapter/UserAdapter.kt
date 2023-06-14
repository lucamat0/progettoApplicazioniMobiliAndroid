package it.uniupo.oggettiusati.adapter

import android.content.Intent
import android.os.BadParcelableException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import it.uniupo.oggettiusati.chat.ChatActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class UserAdapter(val userList: ArrayList<UserLoginActivity.Utente>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    val auth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val utenteCorrente = userList[position]
        val nomeUtenteCorrente = utenteCorrente.nome
        holder.nomeCognome.text = "${utenteCorrente.cognome} ${nomeUtenteCorrente}"

        if((!utenteCorrente.eliminato) && (!utenteCorrente.sospeso)) {
            holder.itemView.setOnClickListener { viewClicked ->
                try {
                    val intent =
                        Intent(holder.itemView.context, ChatActivity::class.java)

                    intent.putExtra("nome", nomeUtenteCorrente)
                    intent.putExtra("uid", utenteCorrente.userId)

                    viewClicked.context.startActivity(intent)
                } catch (e: BadParcelableException) {
                    Log.e(
                        "AnnuncioSerialization",
                        "Errore nella serializzazione dell'oggetto Annuncio: ${e.message}"
                    )
                }
            }
        }

        val utenteAdmin :Int
        runBlocking {
            utenteAdmin = database.collection(UserLoginActivity.Utente.nomeCollection).document(auth.uid!!).get().await().get("amministratore").toString().toInt()
        }

        if (utenteAdmin == 1) {
            holder.btn_stats.visibility = View.VISIBLE
            if(utenteCorrente.eliminato) {
                mostraEliminato(holder)
            } else {
                if(utenteCorrente.sospeso){
                    mostraSospeso(holder)
                } else {
                    holder.btn_sospendi.visibility = View.VISIBLE
                    holder.btn_elimina.visibility = View.VISIBLE
                }
            }
        }
        holder.btn_sospendi.setOnClickListener {
            //sospendi utente
            Toast.makeText(holder.itemView.context, "Sospendo", Toast.LENGTH_SHORT).show()
            mostraSospeso(holder)
        }

        holder.btn_attiva.setOnClickListener {
            //attiva
            holder.btn_sospendi.visibility = View.VISIBLE
            holder.btn_attiva.visibility = View.GONE
        }

        holder.btn_elimina.setOnClickListener {
            //dialog per conferma
            //elimina utente
            mostraEliminato(holder)
        }
    }

    class UserViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val nomeCognome = itemView.findViewById<TextView>(R.id.nomeCognome)
        val btn_sospendi = itemView.findViewById<Button>(R.id.btn_sospendi)
        val btn_attiva = itemView.findViewById<Button>(R.id.btn_attiva)
        val btn_elimina = itemView.findViewById<Button>(R.id.btn_elimina)
        val btn_stats = itemView.findViewById<Button>(R.id.btn_statistiche)
    }

    private fun mostraEliminato(holder: UserViewHolder) {
        holder.btn_sospendi.visibility = View.GONE
        holder.btn_attiva.visibility = View.GONE

        holder.btn_sospendi.isEnabled = false
        holder.btn_attiva.isEnabled = false

        holder.btn_elimina.visibility = View.VISIBLE
        holder.btn_elimina.isEnabled = false
        holder.btn_elimina.text = "rimosso"
    }
    private fun mostraSospeso(holder: UserViewHolder) {
        holder.btn_sospendi.visibility = View.GONE
        holder.btn_attiva.visibility = View.VISIBLE
        holder.btn_elimina.visibility = View.VISIBLE
    }

}

