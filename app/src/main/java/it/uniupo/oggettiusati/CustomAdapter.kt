package it.uniupo.oggettiusati

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){

    //create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder{

        //inflates the card_view_design view
        //that is used to hold list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    //binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int){

        val ItemsViewModel = mList[position]

        //sets the image to the imageview from our itemHolder class
        holder.imageView.setImageResource(ItemsViewModel.image)

        //sets the text to the textview from our itemHolder class
        holder.textView.text = ItemsViewModel.title

        holder.priceTextView.text = ItemsViewModel.price.toString() + " €"

        holder.card.setOnClickListener { viewClicked ->
            val i = Intent(holder.itemView.context, DettaglioOggettoActivity::class.java)
//            val idAnnuncio = holder.itemView.findViewById<LinearLayout>(R.id.cardLayout)
            i.putExtra("annuncioId", ItemsViewModel.annuncioId)
            viewClicked.context.startActivity(i)

            //toast tenuto per avere oggetto contesto e test
//            Toast.makeText(holder.itemView.context, "Benvenuto ${holder.textView.text}!", Toast.LENGTH_LONG).show()
        }
    }

    //return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.titolo)
        val priceTextView: TextView = itemView.findViewById(R.id.prezzo)
        val card: CardView = itemView.findViewById(R.id.cardVu)
    }
}