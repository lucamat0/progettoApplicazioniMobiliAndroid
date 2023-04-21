package it.uniupo.oggettiusati

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DettaglioOggettoActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oggetto)

        val database = Firebase.firestore

        val extras = intent.extras

        val annuncioId = extras?.getString("annuncioId").toString()

        database.collection("annunci").document(annuncioId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    findViewById<TextView>(R.id.nome).text = document["titolo"].toString()
                    findViewById<TextView>(R.id.categoria).text = "Categoria: ${document["categoria"].toString()}"
                    findViewById<TextView>(R.id.descrizione).text = document["descrizione"].toString()
                    findViewById<TextView>(R.id.prezzo).text = "${document["prezzo"].toString()} €"
                    val statoOgg = document["stato"] as Long
                    findViewById<Spinner>(R.id.stato).setSelection(if (statoOgg == 0L) 3 else if (statoOgg == 1L) 2 else if (statoOgg == 2L) 1 else 0)
                    findViewById<Switch>(R.id.spedizione).isChecked = document["disponibilitaSpedire"] as Boolean
                } else {
                    Log.d("DettaglioOggettoActivity.onCreate()", "No such document")
                }
            }
            .addOnFailureListener {exception ->
                Log.d("DettaglioOggettoActivity.onCreate()", "get failed with ", exception)
            }
        findViewById<TextView>(R.id.infoVenditore).text = auth.currentUser?.email
    }
}