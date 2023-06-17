package it.uniupo.oggettiusati.adapter

import android.content.Intent
import android.media.Image
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
import it.uniupo.oggettiusati.AggiungiRecensioneActivity
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.DettaglioOggettoActivity
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.FavoritesFragment
import kotlinx.coroutines.runBlocking

class CustomAdapter(private val myArrayList: HashMap<String, Annuncio>, private val layout: Int, private val isAdmin: Boolean) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
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
        val myAnnuncio = myArrayList.toList()
        val annuncioCorrente = myAnnuncio[position].second
        runBlocking {

            //sets the text to the textview from our itemHolder class
            holder.textView.text = annuncioCorrente.getTitolo()

            holder.priceTextView.text = annuncioCorrente.getPrezzoToString()

            holder.card.setOnClickListener { viewClicked ->
                try {
                    val intent =
                        Intent(holder.itemView.context, DettaglioOggettoActivity::class.java)

                    intent.putExtra("annuncio", annuncioCorrente)
                    intent.putExtra("isAdmin", isAdmin)

                    viewClicked.context.startActivity(intent)
                } catch (e: BadParcelableException) {
                    Log.e(
                        "AnnuncioSerialization",
                        "Errore nella serializzazione dell'oggetto Annuncio: ${e.message}"
                    )
                }
            }

            if (layout == R.layout.card_view_remove_buy_design) {
                // il layout della card caricato contiene i bottoni rimuovi e richiedi oggetto

                if (annuncioCorrente.getAcquirente().equals(auth.uid) && annuncioCorrente.getRichiesta()) {
                    uiRequestFromCurrentUser(holder)
                    if(annuncioCorrente.isVenduto() && (!annuncioCorrente.getProprietarioRecensito())) {
                        holder.imgReqSent?.visibility = View.GONE
                        val btnAcquirente = holder.btnRecensisciVenditore
                        btnAcquirente?.visibility = View.VISIBLE
                        btnAcquirente?.setOnClickListener {
//                            Activity CREA RECENSIONE
                            val i = Intent(holder.itemView.context, AggiungiRecensioneActivity::class.java)
                                .putExtra("idUtenteRecensito", annuncioCorrente.getProprietario())
                            it.context.startActivity(i)
                        }
                    }
                } else {
                    if(holder.btnRemove != null) {
                        holder.btnRemove.setOnClickListener { viewClicked ->
                            runBlocking {
                                CartFragment.eliminaAnnuncioCarrelloFirebaseFirestore(auth.uid!!, annuncioCorrente.getAnnuncioId())
                                Toast.makeText(holder.itemView.context, "Rimuovo l'oggetto ${null} dal carrello", Toast.LENGTH_SHORT).show()
                                holder.card.visibility = View.GONE
                                val intent = Intent(holder.itemView.context, UserLoginActivity::class.java)
                                viewClicked.context.startActivity(intent)
                            }
                        }
                    }

                    if(holder.btnRequest != null) {
                        holder.btnRequest.setOnClickListener {
                            runBlocking {
                                annuncioCorrente.setRichiesta(auth.uid!!)
                            }
                            uiRequestFromCurrentUser(holder)
                        }
                    }
                }
            } else if(layout == R.layout.card_view_remove_design) {
                if(holder.btnRemove != null) {
                    holder.btnRemove.setOnClickListener { //viewClicked ->
                        //rimuovo oggetto dai preferiti
                        runBlocking {
                            FavoritesFragment.eliminaAnnuncioPreferitoFirebaseFirestore(auth.uid!!, annuncioCorrente.getAnnuncioId(), holder.itemView.context)
                            val intent = Intent(holder.itemView.context, UserLoginActivity::class.java)
                            it.context.startActivity(intent)
                        }
                        Toast.makeText(holder.itemView.context, "Rimuovo l'oggetto $annuncioCorrente dai preferiti", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if(layout == R.layout.card_view_design){
                if(annuncioCorrente.getRichiesta()) {
                    if(annuncioCorrente.isProprietario(auth.uid!!)) {
                        holder.imgNotification?.visibility = View.VISIBLE
                        if(annuncioCorrente.isVenduto()) {
                            holder.imgNotification?.setImageResource(R.drawable.baseline_sell_50)
                            if(!annuncioCorrente.getAcquirenteRecensito()) {
                                val btnVenditore = holder.btnRecensisciAcquirente
                                btnVenditore?.visibility = View.VISIBLE
                                btnVenditore?.setOnClickListener {
//                            Activity CREA RECENSIONE
                                    val i = Intent(holder.itemView.context, AggiungiRecensioneActivity::class.java)
                                        .putExtra("idUtenteRecensito", annuncioCorrente.getAcquirente())
                                    it.context.startActivity(i)
                                }
                            }
                        }
                    }
                }
            }

            val myArrayListImmagini = annuncioCorrente.recuperaImmaginiSuFirebase()
            if (myArrayListImmagini.size > 0) {
                Glide.with(holder.itemView.context)
                    .load(myArrayListImmagini.get(0))
                    .into(holder.imageView)
            }
        }
    }

    private fun uiRequestFromCurrentUser(holder: ViewHolder) {
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
        val btnRecensisciVenditore :ImageButton? = itemView.findViewById(R.id.inserisci_recensione_venditore)
        val btnRecensisciAcquirente :ImageButton? = itemView.findViewById(R.id.inserisci_recensione_acquirente)
    }
}
