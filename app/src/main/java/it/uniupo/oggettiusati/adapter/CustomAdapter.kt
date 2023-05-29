package it.uniupo.oggettiusati.adapter

import android.content.Intent
import android.os.BadParcelableException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.DettaglioOggettoActivity
import it.uniupo.oggettiusati.R
import kotlinx.coroutines.runBlocking

class CustomAdapter(private val myArrayList: HashMap<String, Annuncio>, val layout: Int) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    val auth = FirebaseAuth.getInstance()

    //create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //inflates the card_view_design view
        //that is used to hold list item
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return ViewHolder(view)
    }

    //binds the list items to a view

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        runBlocking {
            val myAnnuncio = myArrayList.toList()

            //sets the text to the textview from our itemHolder class
            holder.textView.text = myAnnuncio[position].second.getTitolo()

            holder.priceTextView.text = myAnnuncio[position].second.getPrezzoToString()

            holder.card.setOnClickListener { viewClicked ->
                try {
                    val intent =
                        Intent(holder.itemView.context, DettaglioOggettoActivity::class.java)

                    intent.putExtra("annuncio", myAnnuncio[position].second)

                    viewClicked.context.startActivity(intent)
                } catch (e: BadParcelableException) {
                    Log.e(
                        "AnnuncioSerialization",
                        "Errore nella serializzazione dell'oggetto Annuncio: ${e.message}"
                    )
                }
            }

            if(layout == R.layout.card_view_remove_buy_design) {
                // il layout della card caricato contiene i bottoni rimuovi e richiedi oggetto

                if(myAnnuncio[position].second.getAcquirente().equals(auth.uid) && myAnnuncio[position].second.getRichiesta()){
                    uiRequestFromCurrentUser(holder)
                } else {
                    if(holder.btnRemove != null) {
                        holder.btnRemove.setOnClickListener { //viewClicked ->
                            //rimuovo oggetto dal carrello
                            Toast.makeText(holder.itemView.context, "Rimuovo l'oggetto ${null} dal carrello", Toast.LENGTH_SHORT).show()
                            holder.card.visibility = View.GONE
                        }
                    }

                    if(holder.btnRequest != null) {
                        holder.btnRequest.setOnClickListener {
                            runBlocking {
                                myAnnuncio[position].second.setRichiesta(auth.uid.toString())
                            }
                            uiRequestFromCurrentUser(holder)
                        }
                    }
                }
            } else if(layout == R.layout.card_view_remove_design) {
                if(holder.btnRemove != null) {
                    holder.btnRemove.setOnClickListener { //viewClicked ->
                        //rimuovo oggetto dai preferiti
                        Toast.makeText(holder.itemView.context, "Rimuovo l'oggetto ${null} dai preferiti", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if(layout == R.layout.card_view_design){
                if(myAnnuncio[position].second.getRichiesta()) { //equivalente a and
                    if(myAnnuncio[position].second.isProprietario(auth.uid.toString())){
                        holder.imgNotification?.visibility = View.VISIBLE
                    }
                }
            }

            val myArrayListImmagini = myAnnuncio[position].second.recuperaImmaginiSuFirebase()
            if (myArrayListImmagini.size > 0) {
                Glide.with(holder.itemView.context)
                    .load(myArrayListImmagini.get(0))
                    .into(holder.imageView)
            }
        }
    }

    private fun uiRequestFromCurrentUser(holder: CustomAdapter.ViewHolder) {
        holder.btnRequest?.visibility = View.GONE
        holder.btnRemove?.visibility = View.GONE
        holder.imgReqSent?.visibility = View.VISIBLE
    }

    //return the number of the items in the HashMap
    override fun getItemCount(): Int {
        return myArrayList.size
    }

    //holds the views for adding it to image and text
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.titolo)
        val priceTextView: TextView = itemView.findViewById(R.id.prezzo)
        val card: CardView = itemView.findViewById(R.id.cardVu)
        val btnRemove: ImageButton? = itemView.findViewById(R.id.rimuovi)
        val btnRequest: ImageButton? = itemView.findViewById(R.id.richiedi_oggetto)
        val imgReqSent :ImageView? = itemView.findViewById(R.id.richiesta_inviata)
        val imgNotification :ImageView? = itemView.findViewById(R.id.avviso_richiesta)
    }
}
