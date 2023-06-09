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

class RecensioniAdapter(private val listaRecensioni: HashMap<String, RecensioniActivity.Recensione>) : RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_recensioni_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaRecensioni.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recensioni = listaRecensioni.toList()

        holder.textViewTitolo.text = recensioni[position].second.titolo
        holder.textViewTesto.text = recensioni[position].second.testo
        holder.textViewAutore.text = recensioni[position].second.autore
        holder.textViewData.text = getDateInstance().format(Date(recensioni[position].second.data))
//        holder.textViewData.text = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.ITALY).format(Date(recensioni[position].second.data))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewTitolo :TextView = itemView.findViewById(R.id.titolo_recensione)
        val textViewTesto :TextView = itemView.findViewById(R.id.testo_recensione)
        val textViewAutore :TextView = itemView.findViewById(R.id.autore_recensione)
        val textViewData :TextView = itemView.findViewById(R.id.data_recensione)
    }
}
