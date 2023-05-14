package it.uniupo.oggettiusati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class AggiungiOggettoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aggiungi_oggetto)

        val btnCreaOggetto = findViewById<Button>(R.id.crea_nuovo_oggetto)
        btnCreaOggetto.setOnClickListener {
            //qui codice per creare un nuovo oggetto su firebase

            startActivity(Intent(this, UserLoginActivity::class.java))
        }
    }
}