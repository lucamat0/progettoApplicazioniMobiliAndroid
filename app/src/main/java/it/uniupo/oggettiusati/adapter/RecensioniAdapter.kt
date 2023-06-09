package it.uniupo.oggettiusati.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.RecensioniActivity
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecensioniAdapter(private val listaRecensioni: List<RecensioniActivity.Recensione>) : RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_recensioni_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaRecensioni.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewTitolo.text = listaRecensioni[position].titoloRecensione
        holder.textViewTesto.text = listaRecensioni[position].descrizioniRecensione
        holder.textViewAutore.text = listaRecensioni[position].idUtenteEspresso
        holder.textViewData.text = listaRecensioni[position].votoAlUtente.toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewTitolo :TextView = itemView.findViewById(R.id.titolo_recensione)
        val textViewTesto :TextView = itemView.findViewById(R.id.testo_recensione)
        val textViewAutore :TextView = itemView.findViewById(R.id.autore_recensione)
        val textViewData :TextView = itemView.findViewById(R.id.data_recensione)
    }
}
