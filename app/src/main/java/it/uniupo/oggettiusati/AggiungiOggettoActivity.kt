package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

class AggiungiOggettoActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()

    var myImmaginiAnnuncio = ArrayList<Uri>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aggiungi_oggetto)

        val btnCreaOggetto = findViewById<Button>(R.id.crea_nuovo_oggetto)

        findViewById<Button>(R.id.pick_photo).setOnClickListener {
            selezionaImmagini()
        }

        btnCreaOggetto.setOnClickListener {
            //qui codice per creare un nuovo oggetto su firebase
            val nomeOgg = findViewById<EditText>(R.id.nome).text.toString()
            val categoriaOgg = findViewById<EditText>(R.id.categoria).text.toString()
            val posizioneOgg = Location("provider")
            posizioneOgg.latitude = 45.37
            posizioneOgg.longitude = 8.22
            val descrizioneOgg = findViewById<EditText>(R.id.descrizione).text.toString()
            val prezzoOgg = findViewById<EditText>(R.id.prezzo).text.toString().toDouble()
            val statoOgg = findViewById<Spinner>(R.id.stato)

//            val fotoOgg =
            val spedizioneOgg = findViewById<SwitchCompat>(R.id.spedizione)

            val newAnnuncio = Annuncio(auth.uid!!, nomeOgg, descrizioneOgg, prezzoOgg, statoOgg.selectedItemPosition, spedizioneOgg.isChecked, categoriaOgg, posizioneOgg)

            runBlocking {
                newAnnuncio.salvaAnnuncioSuFirebase(myImmaginiAnnuncio)

                startActivity(Intent(this@AggiungiOggettoActivity, UserLoginActivity::class.java))
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

            /*
            //Esempio di funzionamento!
            val userId = "testId"
            val newAnnuncio = Annuncio(userId, "Mr Robot: Season 3 Blu-Ray + Digital HD", "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.", 16.99, 2, true, "filmETv/serieTv")
            newAnnuncio.salvaAnnuncioSuFirebase(myImmaginiAnnuncio)
            */
        }
    }


}