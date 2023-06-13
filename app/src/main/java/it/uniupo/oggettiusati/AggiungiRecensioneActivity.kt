package it.uniupo.oggettiusati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AggiungiRecensioneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aggiungi_recensione)

        findViewById<Button>(R.id.aggiungi_ricerca).setOnClickListener {
            val titolo = findViewById<EditText>(R.id.titolo_nuova_recensione).text
            val descrizione = findViewById<EditText>(R.id.descrizione_nuova_recensione).text

            if(validString(titolo.toString()) && validString(descrizione.toString()) ){
                //chiama aggiungiRecensione()
//                Toast.makeText(this, "Salvata", Toast.LENGTH_LONG).show()

                //val idUtenteRecensito = intent.extras?.getString("idUtenteRecensito")

                //se tutto andato a buon fine
//                Annuncio().setRecensito(idUtenteRecensito)

//                Toast.makeText(this, idUtenteRecensito, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, UserLoginActivity::class.java))
            } else {
                Toast.makeText(this, "Riempi tutti i campi, alcuni sono vuoti.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        internal fun validString(titolo: String): Boolean {
            return titolo.isNotBlank() //&& titolo.isNotEmpty()
        }
    }

}