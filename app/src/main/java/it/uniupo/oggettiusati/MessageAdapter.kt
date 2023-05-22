package it.uniupo.oggettiusati

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MessageAdapter(val context: Context, val myMessaggi: ArrayList<Messaggio>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECIVE = 1
    val ITEM_SENT = 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            if(viewType == 1){
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.ricevuto, parent, false)

                return RicevutiVieHolder(view)
            }
            else{
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inviato, parent, false)

                return InviatiVieHolder(view)
            }
        }

    override fun getItemViewType(position: Int): Int {
       val messaggioCorrente = myMessaggi[position]

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(messaggioCorrente.userId)){
            return ITEM_SENT
        }
        else{
            return ITEM_RECIVE
        }

    }

        override fun getItemCount(): Int {
            return myMessaggi.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val messaggioCorrente = myMessaggi[position]

            if(holder.javaClass == InviatiVieHolder::class.java){

                val viewHolder = holder as InviatiVieHolder

                holder.messaggioInviato.text = messaggioCorrente.messaggio

            }
            else{

                val viewHolder = holder as RicevutiVieHolder

                holder.messaggioRicevuto.text = messaggioCorrente.messaggio
            }
        }

        class InviatiVieHolder(itemView: View): RecyclerView.ViewHolder(itemView){

            val messaggioInviato = itemView.findViewById<TextView>(R.id.messaggioInviato)

        }

        class RicevutiVieHolder(itemView: View): RecyclerView.ViewHolder(itemView){

            val messaggioRicevuto = itemView.findViewById<TextView>(R.id.messaggioRicevuto)

        }
}