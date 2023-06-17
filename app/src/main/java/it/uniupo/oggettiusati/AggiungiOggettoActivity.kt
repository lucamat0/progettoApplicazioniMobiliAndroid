package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AggiungiOggettoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    private var myImmaginiAnnuncio = ArrayList<Uri>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aggiungi_oggetto)

        val flagModifica = intent.extras?.getBoolean("editMode")
        val annuncioId = intent.extras?.getString("annuncioId")

        val viewNomeOgg = findViewById<EditText>(R.id.nome)
        val viewCategoriaOgg = findViewById<EditText>(R.id.categoria)
        val viewTestoPosizioneOgg = findViewById<EditText>(R.id.posizione)
        val viewDescrizioneOgg = findViewById<EditText>(R.id.descrizione)
        val viewTestoPrezzoOgg = findViewById<EditText>(R.id.prezzo)
        val viewStatoOgg = findViewById<Spinner>(R.id.stato)
        val viewSpedizioneOgg = findViewById<SwitchCompat>(R.id.spedizione)

        val btnCreaOggetto = findViewById<Button>(R.id.crea_nuovo_oggetto)
        if(flagModifica == true) {
            btnCreaOggetto.text = "salva modifiche"
            findViewById<TextView>(R.id.titolo_activity_aggiungi_oggetto).text = "Modifica oggetto:"
            if(annuncioId != null){
                runBlocking {
                    val annuncioCorrente = database.collection(Annuncio.nomeCollection).document(annuncioId).get().await()

                    viewNomeOgg.setText(annuncioCorrente.getString("titolo"))
                    viewCategoriaOgg.setText(annuncioCorrente.getString("categoria"))
                    val posizione = annuncioCorrente.getGeoPoint("posizione")
                    viewTestoPosizioneOgg.setText("${posizione?.latitude} ${posizione?.longitude}")
                    viewDescrizioneOgg.setText(annuncioCorrente.getString("descrizione"))
                    viewTestoPrezzoOgg.setText(annuncioCorrente.getDouble("prezzo").toString())
                    viewStatoOgg.setSelection(annuncioCorrente.getLong("stato")!!.toInt())
                    viewSpedizioneOgg.isChecked = annuncioCorrente.getBoolean("disponibilitaSpedire") == true
                }
            }


        }

        val btnPickImg = findViewById<Button>(R.id.pick_photo)
        btnPickImg.setOnClickListener {
            selezionaImmagini()
            //if(immaginiSelezionateCorrettamente()) {
//            it.text = "" why not works?
            val txt = "Immagini selezionate"
            btnPickImg.text = txt
            //}
        }

        btnCreaOggetto.setOnClickListener {
            //qui codice per creare un nuovo oggetto su firebase
            val nomeOgg = viewNomeOgg.text.toString()
            val categoriaOgg = viewCategoriaOgg.text.toString()
            val testoPosizioneOgg = viewTestoPosizioneOgg.text.toString()
            val descrizioneOgg = viewDescrizioneOgg.text.toString()
            val testoPrezzoOgg = viewTestoPrezzoOgg.text.toString()

            if(arrayOf(nomeOgg,
                    testoPosizioneOgg,
                    descrizioneOgg, //per lo stato e la spedizione dobbiamo chiedere conferma della selezione?
                    testoPrezzoOgg).all { s -> AggiungiRecensioneActivity.validString(s)} /*&& almenoUnaFotoCaricata()*/) {
                //da testoPosizioneOgg (indirizzo) creare oggetto con coordinate
                val posizioneOgg = Location("provider")
                posizioneOgg.latitude = 45.37
                posizioneOgg.longitude = 8.22

                val prezzoOgg = testoPrezzoOgg.toDouble()
                val newAnnuncio = Annuncio(auth.uid!!, nomeOgg, descrizioneOgg, prezzoOgg, viewStatoOgg.selectedItemPosition, viewSpedizioneOgg.isChecked, categoriaOgg, posizioneOgg)

                runBlocking {
                    if(flagModifica == true) {
                        // modificaAnnuncio()
                        Toast.makeText(this@AggiungiOggettoActivity, "Modifico...", Toast.LENGTH_LONG).show()
                    } else {
                        newAnnuncio.salvaAnnuncioSuFirebase(myImmaginiAnnuncio)
                    }
                    startActivity(Intent(this@AggiungiOggettoActivity, UserLoginActivity::class.java))
                }
            } else {
                Toast.makeText(this, "Riempi tutti i campi, alcuni sono vuoti.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun selezionaImmagini() {

        val intent = Intent()

        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Seleziona immagini"), 100)
    }

    //override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = runBlocking {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                // L'utente ha selezionato più immagini
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        myImmaginiAnnuncio.add(imageUri)
                    }
                // L'utente ha selezionato una singola immagine
                } else {
                    val imageUri = data.data!!
                    myImmaginiAnnuncio.add(imageUri)
                }
            }
        }
    }
}