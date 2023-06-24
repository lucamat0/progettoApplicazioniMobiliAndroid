package it.uniupo.oggettiusati

import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.chat.ChatActivity
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.FavoritesFragment

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * Activity che ci permette di mostrare i dettagli di un Annuncio
 *
 * @author Amato Luca
 * @author Busto Matteo
 */
class DettaglioOggettoActivity : AppCompatActivity(), OnMapReadyCallback {

    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore
    private lateinit var coordinateOgg: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oggetto)

//        supportActionBar?.setDisplayShowTitleEnabled(false)
        runBlocking{
            supportActionBar?.setTitle(UserLoginActivity.recuperaUtente(auth.uid!!).nome)
        }

        val myAnnuncio: Annuncio = intent.getParcelableExtra("annuncio", Annuncio::class.java)!!
        val isAdmin = intent.getBooleanExtra("isAdmin", false)

        val myDocumentRefCategoria = database.collection("categoria").document(myAnnuncio.getCategoria())

        runBlocking {
            var myIdSottocategoria: String? = myAnnuncio.getSottocategoria()
            if (myIdSottocategoria != null) {
                findViewById<TextView>(R.id.categoria).text = "Categoria: ${
                    myDocumentRefCategoria.get().await().getString("nome")
                } - ${
                    myDocumentRefCategoria.collection("sottocategoria").document(myIdSottocategoria)
                        .get().await().getString("nome")
                }"
            } else {
                findViewById<TextView>(R.id.categoria).text =
                    "Categoria: ${myDocumentRefCategoria.get().await().getString("nome")}"
            }
        }

        findViewById<TextView>(R.id.nome).text = myAnnuncio.getTitolo()
        findViewById<TextView>(R.id.posizione).text = "Coordinate oggetto: Lat ${myAnnuncio.getPosizione().latitude}, Lon ${myAnnuncio.getPosizione().longitude}"
        findViewById<TextView>(R.id.descrizione).text = "Descrizione: ${myAnnuncio.getDescrizione()}"
        findViewById<TextView>(R.id.prezzo).text = myAnnuncio.getPrezzoToString()
        // 0 = difettoso, 1 = qualche lieve difetto, 2 = usato ma in perfette condizioni, 3 = nuovo
        findViewById<TextView>(R.id.stato).text = if(myAnnuncio.getStato() == 0) "Stato: difettoso" else if(myAnnuncio.getStato() == 1) "Stato: qualche lieve difetto" else if(myAnnuncio.getStato() == 2) "Stato: usato ma in perfette condizioni" else "Stato: nuovo"
        val spediz = "Spedizione: ${if(myAnnuncio.getDisponibilitaSpedire()) "Si" else "No"}"
        findViewById<TextView>(R.id.spedizione).text = spediz

        var nomeVenditore :String
        val proprietarioAnnuncio = myAnnuncio.getProprietario()
        runBlocking {
            nomeVenditore = "Proprietario: ${UserLoginActivity.recuperaUtente(proprietarioAnnuncio).getNomeCognome()}"
        }
        findViewById<TextView>(R.id.nomeVenditore).text = nomeVenditore

        //immagini oggetto
//        val imgScaricate = arrayOf("img1", "img2")
//        for(imgEl in imgScaricate) {
//            val img = ImageView(this)
//            img.setImageResource(R.drawable.sea_wave_beautifully_1920x1080)
//            img.adjustViewBounds = true
//            val lP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
//            lP.setMargins(
//                (this.resources.displayMetrics.density * 10).toInt(),
//                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, this.resources.displayMetrics).toInt(),
//                resources.getDimension(R.dimen.photo_margin).toInt(),
//                resources.getDimension(R.dimen.photo_margin).toInt()
//            )
//            img.layoutParams = lP
//            findViewById<LinearLayout>(R.id.contenitore_immagini).addView(img)
//        }
        coordinateOgg = myAnnuncio.getPosizione()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.object_position) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val img = ImageView(this)
        img.setImageResource(R.drawable.sea_wave_beautifully_1920x1080)
        img.adjustViewBounds = true
        img.layoutParams = ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)

        val lP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        lP.setMargins(
            (this.resources.displayMetrics.density * 10).toInt(),
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, this.resources.displayMetrics).toInt(),
            resources.getDimension(R.dimen.photo_margin).toInt(),
            resources.getDimension(R.dimen.photo_margin).toInt())

        img.layoutParams = lP

        findViewById<LinearLayout>(R.id.contenitore_immagini).addView(img)

//        img.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
//        img.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
//        img.requestLayout()

        val img2 = ImageView(this)
        img2.setImageResource(R.drawable.sea_wave_beautifully_1920x1080)
        img2.adjustViewBounds = true
        img2.layoutParams = lP
        findViewById<LinearLayout>(R.id.contenitore_immagini).addView(img2)
        //fine immagini

        val btnRecensioniVenditore = findViewById<Button>(R.id.visualizza_recensioni_venditore)
        btnRecensioniVenditore.setOnClickListener {
            val i = Intent(this, RecensioniActivity::class.java)
            i.putExtra("idVenditore", proprietarioAnnuncio)
            startActivity(i)
        }

        findViewById<Button>(R.id.chatta_con_proprietario).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("nome", nomeVenditore)
            intent.putExtra("uid", proprietarioAnnuncio)
            startActivity(intent)
        }

        val utenteProprietario = myAnnuncio.isProprietario(auth.uid!!)

        if(utenteProprietario || isAdmin) { //l'annuncio appartiene all'utente autenticato:
            if(utenteProprietario)
                btnRecensioniVenditore.visibility = View.GONE //Must be one of: View.VISIBLE, View.INVISIBLE, View.GONE
            // non e' possibile inserirlo nei preferiti ne metterlo nel carrello per acquistarlo
            if(utenteProprietario)
                findViewById<LinearLayout>(R.id.layout_contatta_aggiungi).visibility = View.GONE
            else {
                findViewById<Button>(R.id.aggiungi_carrello).visibility = View.GONE
                findViewById<Button>(R.id.aggiungi_preferiti).visibility = View.GONE
            }

            if(myAnnuncio.getRichiesta()) {
                if(!myAnnuncio.isVenduto()) {
                    Log.d("venduto", "${myAnnuncio.isVenduto()}")
                    findViewById<LinearLayout>(R.id.layout_richiesta).visibility = View.VISIBLE
                    if(isAdmin) {
                        findViewById<TextView>(R.id.titolo_richiesta).text = "Richiesta di acquisto"
                        findViewById<LinearLayout>(R.id.accetta_rifiuta).visibility = View.GONE
                        listOf(findViewById<Button>(R.id.accetta),
                            findViewById(R.id.rifiuta)).map { it.isEnabled = false }
                    }
                    findViewById<Button>(R.id.visualizza_recensioni_acquirente).setOnClickListener {
                        //startActivity(Recensioni.kt)
                    }

                    findViewById<Button>(R.id.accetta).setOnClickListener {
                        val idAcquirente = myAnnuncio.getAcquirente()
                        if(idAcquirente != null) {
                            runBlocking {
                                myAnnuncio.setVenduto(/*idAcquirente*/)
                            }
                        }
                        findViewById<LinearLayout>(R.id.layout_richiesta).visibility = View.GONE
                    }

                    findViewById<Button>(R.id.rifiuta).setOnClickListener {
                        runBlocking { myAnnuncio.setEliminaRichiesta(auth.uid.toString()) }
                        findViewById<Button>(R.id.layout_richiesta).visibility = View.GONE
                    }
                }

            } else { //se non c'è una richiesta e sono il proprietario/admin posso modificare ed eliminare l'oggetto
                findViewById<Button>(R.id.modifica_oggetto).visibility = View.VISIBLE
                // creare funzione e aggiungerlo anche per admin
                findViewById<Button>(R.id.modifica_oggetto).setOnClickListener {
                    val i = Intent(this, AggiungiOggettoActivity::class.java)
                    i.putExtra("editMode", true)
                    i.putExtra("annuncioId", myAnnuncio.getAnnuncioId())
                    startActivity(i)
                }

                findViewById<Button>(R.id.elimina_oggetto).visibility = View.VISIBLE
                // creare funzione e aggiungerlo anche per admin
                findViewById<Button>(R.id.elimina_oggetto).setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle("Attenzione")
                        .setMessage("Sei sicuro di voler eliminare l'oggetto?")
                        .setPositiveButton("Si") { dialog :DialogInterface, _:Int ->
                            dialog.dismiss()
                            startActivity(Intent(this, UserLoginActivity::class.java))
                            runBlocking {
//                                eliminaAnnuncioFirebase()
                            }
                        }
                        .setNegativeButton("No") { dialog :DialogInterface, _:Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        } else {
            listOf(findViewById<Button>(R.id.modifica_oggetto),
            findViewById(R.id.elimina_oggetto)).map { it.isEnabled = false }
            runBlocking {
                if(isPreferito(auth.uid!!, myAnnuncio.getAnnuncioId())){
                    findViewById<Button>(R.id.aggiungi_preferiti).visibility = View.GONE
                } else {
                    findViewById<Button>(R.id.aggiungi_preferiti).setOnClickListener {
                        runBlocking {
                            //inserisci oggetto nei preferiti
                            FavoritesFragment.inserisciAnnuncioPreferitoFirebaseFirestore(auth.uid!!, myAnnuncio.getAnnuncioId(), this@DettaglioOggettoActivity)
                        }
                        findViewById<Button>(R.id.aggiungi_preferiti).visibility = View.GONE
                    }
                }

                if(isCarrello(auth.uid!!, myAnnuncio.getAnnuncioId())){
                    findViewById<Button>(R.id.aggiungi_carrello).visibility = View.GONE
                } else {
                    findViewById<Button>(R.id.aggiungi_carrello).setOnClickListener {
                        runBlocking {
                            CartFragment.inserisciAnnuncioCarrelloFirebaseFirestore(auth.uid!!, myAnnuncio.getAnnuncioId())
                        }
                        findViewById<Button>(R.id.aggiungi_carrello).visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Verifica se un determinato annuncio è nella lista dei preferiti dell'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @param annuncioId Identificativo dell'annuncio
     * @return true se l'annuncio è nei preferiti altrimenti false
     */
    private suspend fun isPreferito(userId: String, annuncioId: String): Boolean{

        val myCollectionPreferito = this.database.collection(UserLoginActivity.Utente.nomeCollection).document(userId).collection("preferito")

        for(myDocument in myCollectionPreferito.get().await().documents)
            if (myDocument.get("annuncioId") == annuncioId)
                return true
        return false
    }

    /**
     * Verifica se un determinato annuncio è nel carrello dell'utente specificato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     * @param annuncioId Identificativo dell'annuncio
     * @return true se l'annuncio è nel carrello altrimenti false
     */
    private suspend fun isCarrello(userId: String, annuncioId: String): Boolean{

        val myCollectionPreferito = this.database.collection(UserLoginActivity.Utente.nomeCollection).document(userId).collection("carrello")

        for(myDocument in myCollectionPreferito.get().await().documents)
            if (myDocument.id == annuncioId)
                return true
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val posiz = LatLng(coordinateOgg.latitude, coordinateOgg.longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(posiz)
                .title("Posizione oggetto")
                .snippet("")
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(posiz))
    }

}