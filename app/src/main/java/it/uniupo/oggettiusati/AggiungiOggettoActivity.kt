package it.uniupo.oggettiusati

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
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
        var userInteraction = false
        var annuncioCorrenteN: Annuncio? = null

        if(flagModifica == true && annuncioId != null) {
                runBlocking {
                    annuncioCorrenteN = UserLoginActivity.documentoAnnuncioToObject(
                        database.collection(Annuncio.nomeCollection).document(annuncioId).get()
                            .await()
                    )
                }
        }

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
                    if(userInteraction == false) {
                        if(annuncioCorrenteN != null) {

                            val annuncioCorrente = annuncioCorrenteN as Annuncio
                            impostaCategoriaOggetto(categorie, annuncioCorrente, viewCategoriaOgg, viewSottoCategOgg)
                        }
                        userInteraction = true
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }


        val btnCreaOggetto = findViewById<Button>(R.id.crea_nuovo_oggetto)
        if(flagModifica == true) {
            btnCreaOggetto.text = "salva modifiche"
            findViewById<TextView>(R.id.titolo_activity_aggiungi_oggetto).text = "Modifica oggetto:"
            if(annuncioCorrenteN != null){

                val annuncioCorrente = annuncioCorrenteN as Annuncio
                viewNomeOgg.setText(annuncioCorrente.getTitolo())

                impostaCategoriaOggetto(categorie, annuncioCorrente, viewCategoriaOgg, viewSottoCategOgg)

                val posizione = annuncioCorrente.getPosizione()

                var geocodeAddressesMatches: List<Address>? = null

                try {
                    geocodeAddressesMatches = Geocoder(this@AggiungiOggettoActivity).getFromLocation(posizione.latitude, posizione.longitude, 1)
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

                viewDescrizioneOgg.setText(annuncioCorrente.getDescrizione())
                viewTestoPrezzoOgg.setText(annuncioCorrente.getPrezzo().toString())
                viewStatoOgg.setSelection(annuncioCorrente.getStato())
                viewSpedizioneOgg.isChecked = annuncioCorrente.getDisponibilitaSpedire()

                findViewById<HorizontalScrollView>(R.id.immagini_oggetto).visibility = View.VISIBLE

                val imgScaricate = arrayOf("img1", "img2")
//                for(imgEl in imgScaricate) {
//                    val img = ImageView(this)
//                    img.setImageResource(R.drawable.sea_wave_beautifully_1920x1080)
//                    img.adjustViewBounds = true
//                    val lP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
//                    lP.setMargins(
//                        (this.resources.displayMetrics.density * 10).toInt(),
//                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, this.resources.displayMetrics).toInt(),
//                        resources.getDimension(R.dimen.photo_margin).toInt(),
//                        resources.getDimension(R.dimen.photo_margin).toInt()
//                    )
//                    img.layoutParams = lP
//
//                    img.setTag(0, imgEl.id)
//                    img.setOnClickListener {
//                        cancellaImmagine(img.getTag(0))
//                    }
//
//                    findViewById<LinearLayout>(R.id.contenitore_immagini).addView(img)
//                }

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
            val idCategoriaOgg = categorie[viewCategoriaOgg.selectedItemPosition].id
            val idSottoCategOgg = categorie[viewCategoriaOgg.selectedItemPosition].sottocategorie?.toList()?.get(viewSottoCategOgg.selectedItemPosition)?.id
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
                    val newAnnuncio = Annuncio(auth.uid!!, nomeOgg, descrizioneOgg, prezzoOgg, viewStatoOgg.selectedItemPosition, viewSpedizioneOgg.isChecked, idCategoriaOgg, idSottoCategOgg, posizioneOgg)

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

    private fun impostaCategoriaOggetto(
        categorie: List<UserLoginActivity.Categoria>,
        annuncioCorrente: Annuncio,
        viewCategoriaOgg: Spinner,
        viewSottoCategOgg: Spinner
    ) {
        val categoria = categorie.indexOf(UserLoginActivity.Categoria(annuncioCorrente.getCategoria()))
        viewCategoriaOgg.setSelection(categoria)
        if(annuncioCorrente.getSottocategoria() != null)
            viewSottoCategOgg.setSelection(categorie.get(categoria).sottocategorie!!.indexOf(UserLoginActivity.Categoria(annuncioCorrente.getSottocategoria()!!)))
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