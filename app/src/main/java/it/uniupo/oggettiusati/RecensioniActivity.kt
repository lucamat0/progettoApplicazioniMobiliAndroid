package it.uniupo.oggettiusati

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.uniupo.oggettiusati.adapter.RecensioniAdapter

class RecensioniActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recensioni)

        val recensioniDaMostrare :HashMap<String, Recensione> = HashMap()

        recensioniDaMostrare.put("1",Recensione("bel prdotto", "funziona molto bene e mi piace", "marco", 1685238057))
        recensioniDaMostrare.put("2",Recensione("grande occasione", "qualita' prezzo ok, durevolezza meno", "johnny", 1685228277))
        recensioniDaMostrare.put("3",Recensione("Ottime cuffie, fatta eccezione per la manopola del volume.",
            "Batte le mie altre cuffie e il prezzo è buono. Si adatta perfettamente, non ho usato il microfono. Il mio ultimo auricolare di marca simile si è rotto, perché l'ho sollevato da un lato da solo e la plastica ha iniziato a rompersi fino a quando non ha ceduto. Afferra sempre l'auricolare con entrambe le mani.",
            "Philip Ray Bessa", 1682132400000))
        recensioniDaMostrare.put("4",Recensione("Adoro il design ma deluso dal colore",
            "Ho appena ricevuto il set la settimana scorsa e penso di essere un po' deluso dal colore. Ho ordinato la crema come mostrato nel sito web ma quello che è arrivato è un vero pistacchio. Lo dice anche nella confezione. Non avevo bisogno di un pistacchio con l'arredamento della mia casa, ma immagino che il colore stia crescendo su di me. Non ho ancora deciso se lo sostituirò, non ho ancora iniziato a usarlo. Dato un 4 stelle perché altrimenti sembra un set di stoviglie robusto e abbastanza resistente ai graffi. Il design del labbro rialzato è decisamente molto bello, il motivo per cui ho scelto questo prodotto.",
            "Nik", 1683132403760))
        recensioniDaMostrare.put("5",Recensione("Immagine e prodotto corrispondono perfettamente", "stavo cercando un fazzoletto di rame e un bidone della spazzatura che sembra fantastico e funziona bene", "Caterina L. Pianta", 1684532403760))

        //fill Recyclerview
        val recyclerVu = findViewById<RecyclerView>(R.id.recyclerview_recensioni)
        recyclerVu?.layoutManager = LinearLayoutManager(this)
        val adapter = RecensioniAdapter(recensioniDaMostrare)
        recyclerVu?.adapter = adapter
    }

    data class Recensione(val titolo: String, val testo: String, val autore: String, val data: Long)
}