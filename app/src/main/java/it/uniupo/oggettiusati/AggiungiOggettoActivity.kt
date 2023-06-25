package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.stream.Collectors

/**
 * Activity per l'aggiunta o modifica di un oggetto
 *
 * @author Amato Luca
 * @author Busto Matteo
 */
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
        val viewCategoriaOgg = findViewById<Spinner>(R.id.categoria)
        val viewSottoCategOgg = findViewById<Spinner>(R.id.sottocategoria)
        val viewTestoPosizioneOgg = findViewById<EditText>(R.id.posizione)
        val viewDescrizioneOgg = findViewById<EditText>(R.id.descrizione)
        val viewTestoPrezzoOgg = findViewById<EditText>(R.id.prezzo)
        val viewStatoOgg = findViewById<Spinner>(R.id.stato)
        val viewSpedizioneOgg = findViewById<SwitchCompat>(R.id.spedizione)

        val categorie: List<UserLoginActivity.Categoria>

        runBlocking {
            categorie = UserLoginActivity.recuperaCategorieFirebase()
            val spinnerCategorieAdapter: ArrayAdapter<String> = ArrayAdapter(this@AggiungiOggettoActivity, android.R.layout.simple_spinner_dropdown_item, categorie.stream().map { categoria -> categoria.nome }.collect(Collectors.toList()))
            spinnerCategorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            viewCategoriaOgg.adapter = spinnerCategorieAdapter

            viewCategoriaOgg.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val categoriaSelezionata = categorie[viewCategoriaOgg.selectedItemPosition]
                    if(UserLoginActivity.hasSottocategorie(categoriaSelezionata)) {
                        findViewById<LinearLayout>(R.id.layout_sottocategoria).visibility = View.VISIBLE

                        val sottoCategorie = categoriaSelezionata.sottocategorie!!.stream().map { sottocategoria -> sottocategoria.nome }.collect(Collectors.toList())
                        val spinnerSottoCategAdapter: ArrayAdapter<String> = ArrayAdapter(this@AggiungiOggettoActivity, android.R.layout.simple_spinner_dropdown_item, sottoCategorie)
                        spinnerSottoCategAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                        viewSottoCategOgg.adapter = spinnerSottoCategAdapter
                    } else {
                        findViewById<LinearLayout>(R.id.layout_sottocategoria).visibility = View.GONE
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }


        val btnCreaOggetto = findViewById<Button>(R.id.crea_nuovo_oggetto)
        if(flagModifica == true) {
            btnCreaOggetto.text = "salva modifiche"
            findViewById<TextView>(R.id.titolo_activity_aggiungi_oggetto).text = "Modifica oggetto:"
            if(annuncioId != null){
                runBlocking {
                    val annuncioCorrente = database.collection(Annuncio.nomeCollection).document(annuncioId).get().await()

                    viewNomeOgg.setText(annuncioCorrente.getString("titolo"))
                    //viewCategoriaOgg.setSelection(/*indice dello spinner che contiene il nome della categoria il cui id e' indcato nell annuncio*/)
                    val posizione = annuncioCorrente.getGeoPoint("posizione")

                    var geocodeAddressesMatches: List<Address>? = null

                    try {
                        geocodeAddressesMatches = Geocoder(this@AggiungiOggettoActivity).getFromLocation(posizione!!.latitude, posizione.longitude, 1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val locality: String?


                    if (!geocodeAddressesMatches.isNullOrEmpty()) {
                        locality = geocodeAddressesMatches[0].locality //comune
                        viewTestoPosizioneOgg.setText("$locality")
                    } else
                        Toast.makeText(this@AggiungiOggettoActivity, "Error getting address ", Toast.LENGTH_LONG).show()

//                    viewTestoPosizioneOgg.setText("${posizione?.latitude} ${posizione?.longitude}")

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
            val categoriaOgg = categorie[viewCategoriaOgg.selectedItemPosition].id
            val sottoCategOgg = categorie[viewCategoriaOgg.selectedItemPosition].sottocategorie?.toList()?.get(viewSottoCategOgg.selectedItemPosition)?.id
            val testoPosizioneOgg = viewTestoPosizioneOgg.text.toString()
            val descrizioneOgg = viewDescrizioneOgg.text.toString()
            val testoPrezzoOgg = viewTestoPrezzoOgg.text.toString()

            if(arrayOf(nomeOgg,
                    testoPosizioneOgg,
                    descrizioneOgg, //per lo stato e la spedizione dobbiamo chiedere conferma della selezione?
                    testoPrezzoOgg).all { s -> AggiungiRecensioneActivity.validString(s)} /*&& almenoUnaFotoCaricata()*/) {
                //da testoPosizioneOgg (indirizzo) creare oggetto con coordinate
                val posizioneOgg = Location("provider")

//                var markerInItaly = LatLng(45.0, 8.0)

                var geocodeCoordinatesMatches: List<Address>? = null
                try {
                    geocodeCoordinatesMatches = Geocoder(this).getFromLocationName(testoPosizioneOgg, 1)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (!geocodeCoordinatesMatches.isNullOrEmpty()) {
                    posizioneOgg.latitude = geocodeCoordinatesMatches[0].latitude
                    posizioneOgg.longitude = geocodeCoordinatesMatches[0].longitude

                    val prezzoOgg = testoPrezzoOgg.toDouble()
                    val newAnnuncio = Annuncio(auth.uid!!, nomeOgg, descrizioneOgg, prezzoOgg, viewStatoOgg.selectedItemPosition, viewSpedizioneOgg.isChecked, categoriaOgg, sottoCategOgg, posizioneOgg)

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
                    Toast.makeText(this, "Errore, indirizzo non valido", Toast.LENGTH_SHORT).show()
//                    posizioneOgg.latitude = 45.37
//                    posizioneOgg.longitude = 8.22
                }
            } else {
                Toast.makeText(this, "Riempi tutti i campi, alcuni sono vuoti.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Avvia activity per selezionare un insieme di immagini
     *
     * @author Amato Luca
     */
    private fun selezionaImmagini() {

        val intent = Intent()

        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Seleziona immagini"), 100)
    }

    /**
     * Gestisce il risultato del activity chiamata con startActivityForResult()
     *
     * @author Amato Luca
     * @param requestCode Codice della richiesta
     * @param resultCode Codice di risultato restituito dall'activity
     * @param data L'intent contenente i dati restituiti dall'activity
     */
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