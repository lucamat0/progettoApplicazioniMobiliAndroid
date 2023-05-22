package it.uniupo.oggettiusati

import android.content.Context
import android.content.Intent
import android.os.BadParcelableException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class UserAdapter(val userList: ArrayList<ChatFragment.Utente>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.nomeCognome.text = userList[position].cognome + " " + userList[position].nome

        holder.itemView.setOnClickListener { viewClicked ->
            try {
                val intent =
                    Intent(holder.itemView.context, ChatActivity::class.java)

                intent.putExtra("nome", userList[position].nome)
                intent.putExtra("uid", userList[position].uid)

                viewClicked.context.startActivity(intent)
            } catch (e: BadParcelableException) {
                Log.e(
                    "AnnuncioSerialization",
                    "Errore nella serializzazione dell'oggetto Annuncio: ${e.message}"
                )
            }
        }
    }

    class UserViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val nomeCognome = itemView.findViewById<TextView>(R.id.nomeCognome)
    }

}

