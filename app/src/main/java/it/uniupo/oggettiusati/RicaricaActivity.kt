package it.uniupo.oggettiusati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.uniupo.oggettiusati.fragment.CartFragment
import kotlinx.coroutines.runBlocking

class RicaricaActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ricarica)

        val viewImporto = findViewById<NumberPicker>(R.id.importo_ricarica)
        viewImporto.maxValue = 300
        viewImporto.minValue = 5

        findViewById<Button>(R.id.richiedi_ricarica).setOnClickListener {
            val nomeCC = findViewById<EditText>(R.id.cc_name).text.toString()
            val numeroCC = findViewById<EditText>(R.id.cc_number).text.toString()
            val annoCC = findViewById<EditText>(R.id.cc_year).text.toString()
            val cvv = findViewById<EditText>(R.id.cvv).text.toString()

            if(nomeCC.isNotBlank() &&
                numeroCC.isNotBlank() &&
                annoCC.isNotBlank() &&
                cvv.isNotBlank()) {

                if(numeroCC.length == 16 &&
                    annoCC.length == 4 &&
                    cvv.length == 3 &&
                    annoCC.toInt() > 2023 && annoCC.toInt() < 2033 &&
                    numeroCC.filter { !(it.isWhitespace()) }.all { it.isDigit() } &&
                    annoCC.all { it.isDigit() }
                ) {

                    val importo = viewImporto.value.toDouble()

                    runBlocking{
                        CartFragment.salvaTransazioneSuFirestoreFirebase(auth.uid!!, importo, true)
                    }
                    Toast.makeText(this, "Ricarico di $importo€", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UserLoginActivity::class.java))

                } else {
                    Toast.makeText(this, "I campi hanno un formato errato e non sono validi, controllali.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Alcuni campi sono vuoti, compilali tutti per ricaricare.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}