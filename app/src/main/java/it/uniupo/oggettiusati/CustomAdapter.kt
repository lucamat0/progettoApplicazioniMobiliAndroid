package it.uniupo.oggettiusati

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.BadParcelableException
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class CustomAdapter(private val myArrayList: HashMap<String, Annuncio>, val layout: Int) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

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

            val myArrayListImmagini = myAnnuncio[position].second.recuperaImmaginiSuFirebase()

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

            if(holder.btnRemove != null) {
                holder.btnRemove.setOnClickListener { //viewClicked ->
                    //rimuovo oggetto dal carrello
                    Toast.makeText(holder.itemView.context, "Rimuovo l'oggetto ${null} dal carrello", Toast.LENGTH_SHORT).show()
                }
            }


            if (myArrayListImmagini.size > 0) {
                Glide.with(holder.itemView.context)
                    .load(myArrayListImmagini.get(0))
                    .into(holder.imageView)
            }
        }
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
    }
}
