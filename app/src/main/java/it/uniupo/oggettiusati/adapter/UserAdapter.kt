package it.uniupo.oggettiusati.adapter

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.graphics.Color
import android.os.BadParcelableException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import it.uniupo.oggettiusati.chat.ChatActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class UserAdapter(private val userList: ArrayList<UserLoginActivity.Utente>, private val isAdmin: Boolean): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

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

        if(((!utenteCorrente.eliminato) && (!utenteCorrente.sospeso)) || isAdmin) {
            holder.nomeCognome.text = "${utenteCorrente.cognome} ${nomeUtenteCorrente}"
        } else {
            holder.nomeCognome.text = "Utente disabilitato"
        }

        if(utenteCorrente.eliminato || utenteCorrente.sospeso) {
            rimuoviColoreLayoutUtente(holder)
        }

        holder.itemView.setOnClickListener { viewClicked ->
            try {
                val utenteSospeso: Boolean?
                val utenteEliminato: Boolean?
                runBlocking {
                    utenteSospeso = database.collection(UserLoginActivity.Utente.nomeCollection).document(utenteCorrente.userId).get().await().getBoolean("sospeso")
                    utenteEliminato = database.collection(UserLoginActivity.Utente.nomeCollection).document(utenteCorrente.userId).get().await().getBoolean("eliminato")
                }
                if(utenteSospeso == false && utenteEliminato == false) {
                    mostraUtenteNormale(holder)
                    holder.nomeCognome.text = "${utenteCorrente.cognome} ${nomeUtenteCorrente}"
                    val intent =
                        Intent(holder.itemView.context, ChatActivity::class.java)

                    intent.putExtra("nome", nomeUtenteCorrente)
                    intent.putExtra("uid", utenteCorrente.userId)

                    viewClicked.context.startActivity(intent)
                } else {
                    if(!isAdmin) {
                        holder.nomeCognome.text = "Utente disabilitato"
                    }
                    rimuoviColoreLayoutUtente(holder)
                    if(utenteEliminato == true) {
                        mostraEliminato(holder)
                    } else {
                        if(utenteSospeso == true){
                            mostraSospeso(holder)
                        }
                    }
                }
            } catch (e: BadParcelableException) {
                Log.e(
                    "AnnuncioSerialization",
                    "Errore nella serializzazione dell'oggetto Annuncio: ${e.message}"
                )
            }
        }

        if (isAdmin) {
            holder.stats.visibility = View.VISIBLE
            holder.sospendiElimina.visibility = View.VISIBLE

            setUpEliminatoSospeso(utenteCorrente, holder)

            holder.btn_sospendi.setOnClickListener {
                //sospendi utente
                Toast.makeText(holder.itemView.context, "Sospendo", Toast.LENGTH_SHORT).show()
                mostraSospeso(holder)
                rimuoviColoreLayoutUtente(holder)
            }

            holder.btn_attiva.setOnClickListener {
                //attiva
                mostraUtenteNormale(holder)
                Toast.makeText(holder.itemView.context, "Attivo", Toast.LENGTH_SHORT).show()
            }

            holder.btn_elimina.setOnClickListener {
                //dialog per conferma
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Attenzione")
                    .setMessage("Sei sicuro di voler eliminare l'utente?")
                    .setPositiveButton("Si") { dialog : DialogInterface, _:Int ->
                        dialog.dismiss()
                        runBlocking {
//                            eliminaUtente()
                            //elimina utente
                            mostraEliminato(holder)
                            rimuoviColoreLayoutUtente(holder)
                        }
                    }
                    .setNegativeButton("No") { dialog : DialogInterface, _:Int ->
                        dialog.dismiss()
                    }
                    .show()
            }

            val punteggio = 0.0
            val numOgg = 0

            holder.punteggioUtente.text = "Punteggio: ${punteggio}"
            holder.numOggUtente.text = "Oggetti in vendita: ${numOgg}"
        }
    }

    private fun mostraUtenteNormale(holder: UserViewHolder) {
//        holder.btn_sospendi.isEnabled = true
//        holder.btn_attiva.isEnabled = true

//        holder.btn_elimina.backgroundTintList = ColorStateList.valueOf(holder.itemView.context.resources.getColor(R.color.red, holder.itemView.context.theme))

        holder.btn_sospendi.visibility = View.VISIBLE
        holder.btn_attiva.visibility = View.GONE
        aggiungiColoreLayoutUtente(holder)

    }

    private fun setUpEliminatoSospeso(utenteCorrente: UserLoginActivity.Utente, holder: UserViewHolder) {
        if(utenteCorrente.eliminato || utenteCorrente.sospeso)
            rimuoviColoreLayoutUtente(holder)
        if(utenteCorrente.eliminato) {
            mostraEliminato(holder)
        } else {
            if(utenteCorrente.sospeso){
                mostraSospeso(holder)
            }
        }
    }

    private fun mostraEliminato(holder: UserViewHolder) {
        holder.btn_sospendi.visibility = View.GONE
        holder.btn_attiva.visibility = View.GONE

        holder.btn_sospendi.isEnabled = false
        holder.btn_attiva.isEnabled = false

        //holder.btn_elimina.visibility = View.VISIBLE // da controllare se serve
        holder.btn_elimina.isEnabled = false
        holder.btn_elimina.text = "rimosso"
        holder.btn_elimina.backgroundTintList = ColorStateList.valueOf(holder.itemView.context.resources.getColor(androidx.appcompat.R.color.button_material_light, holder.itemView.context.theme))

//        holder.btn_attiva.setBackgroundColor(Color.TRANSPARENT)
//        holder.btn_sospendi.setBackgroundColor(Color.TRANSPARENT)

//        holder.btn_elimina.backgroundTintList = ColorStateList.valueOf(holder.itemView.context.resources.getColor(R.color.button, holder.itemView.context.theme))//ContextCompat.getDrawable(holder.itemView.context, )
//        holder.btn_elimina.backgroundTintList = ColorStateList.valueOf(holder.itemView.context.resources.getColor(
//            com.google.android.material.R.color.button_material_light, holder.itemView.context.theme))
//        holder.btn_elimina.setBackgroundColor(Color.TRANSPARENT)
//        holder.btn_elimina.backgroundTintList = Button(holder.itemView.context).backgroundTintList
//        holder.btn_elimina.setBackgroundResource(android.R.drawable.btn_default)
//        holder.btn_elimina.alpha = 0.5f
//        holder.btn_elimina.backgroundTintList = ColorStateList.valueOf(holder.itemView.context.resources.getColor(Color.parseColor(Color.TRANSPARENT.toString().replace("#")), holder.itemView.context.theme))
//        holder.btn_elimina.background = Button(holder.itemView.context).background
    }

    private fun mostraSospeso(holder: UserViewHolder) {

        holder.btn_sospendi.visibility = View.GONE
        holder.btn_attiva.visibility = View.VISIBLE
        //holder.btn_elimina.visibility = View.VISIBLE // da controllare
    }

    private fun rimuoviColoreLayoutUtente(holder: UserViewHolder) {
        impostaColore(Color.TRANSPARENT, holder)
    }

    private fun aggiungiColoreLayoutUtente(holder: UserViewHolder) {
        impostaColore(holder.itemView.context.resources.getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95, holder.itemView.context.theme), holder)
    }

    private fun impostaColore(colore: Int, holder: UserViewHolder) {
        holder.layoutUtente.setBackgroundColor(colore)
    }

    class UserViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val nomeCognome = itemView.findViewById<TextView>(R.id.nomeCognome)
        val btn_sospendi = itemView.findViewById<Button>(R.id.btn_sospendi)!!
        val btn_attiva = itemView.findViewById<Button>(R.id.btn_attiva)
        val btn_elimina = itemView.findViewById<Button>(R.id.btn_elimina)
        val numOggUtente = itemView.findViewById<TextView>(R.id.num_ogg_in_vendita_utente)
        val punteggioUtente = itemView.findViewById<TextView>(R.id.punteggio_utente)
        val stats = itemView.findViewById<LinearLayout>(R.id.layout_statistiche)
        val sospendiElimina = itemView.findViewById<LinearLayout>(R.id.layout_sospendi_elimina)
        val layoutUtente = itemView.findViewById<LinearLayout>(R.id.user_layout_chat)
    }
}

