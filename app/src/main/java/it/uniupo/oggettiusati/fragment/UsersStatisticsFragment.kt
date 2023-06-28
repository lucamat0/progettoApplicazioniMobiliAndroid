package it.uniupo.oggettiusati.fragment

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.UserLoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.stream.Collectors
import kotlin.streams.toList


class UsersStatisticsFragment : Fragment() {

    private val database = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    lateinit var categorie: List<UserLoginActivity.Categoria>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_users_statistics, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView
        val distanceSlider = fragmentRootView.findViewById<Slider>(R.id.adminDistanceSlider)
        val testoDistanza = fragmentRootView.findViewById<TextView>(R.id.max_distance_admin)

        var updTxt = "Distanza max: ${distanceSlider?.value}km"
        testoDistanza?.text = updTxt

        distanceSlider?.setLabelFormatter { value -> "$value km" }

        distanceSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //...
            }

            override fun onStopTrackingTouch(slider: Slider) {
                updTxt = "Distanza max: ${distanceSlider.value}km"
                testoDistanza?.text = updTxt
            }
        })



        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val numTotOgg = numeroOggettiInVendita()
            val tMedioGiorni = String.format("%.4f", calcolaTempoMedioAnnunciVenduti())
            activity?.findViewById<TextView>(R.id.num_ogg_in_vendita)?.text = "Oggetti in vendita in questo momento: ${numTotOgg}"
            activity?.findViewById<TextView>(R.id.tempo_medio_vendita)?.text = "Tempo medio vendita di un oggetto: ${tMedioGiorni} giorni "

            activity?.findViewById<Button>(R.id.btn_ricerca_admin)?.setOnClickListener {
                val lat = activity?.findViewById<EditText>(R.id.latitudine_ricerca_admin)?.text.toString()
                val lon = activity?.findViewById<EditText>(R.id.longitudine_ricerca_admin)?.text.toString()
                val dist = activity?.findViewById<Slider>(R.id.adminDistanceSlider)?.value?.toInt()!!
                if(arrayOf(lat, lon).all { it.isNotBlank() } && dist > 0) {
                    runBlocking {
                        val posiz = Location("provider")
                        posiz.latitude = lat.toDouble()
                        posiz.longitude = lon.toDouble()
                        val numOggDistanzaPunto = numeroOggettiInVenditaPerRaggioDistanza(posiz, dist) //ricercaOggettiDistanzaMinore().size
                        activity?.findViewById<TextView>(R.id.risultato_ricerca_admin)?.text = "Oggetti trovati: ${numOggDistanzaPunto}"
                    }

                } else {
                    Toast.makeText(activity, "Alcuni campi vuoti, compilali", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val viewCategorie = requireActivity().findViewById<Spinner>(R.id.categorie)
        val viewSottoCateg = requireActivity().findViewById<Spinner>(R.id.sottocategorie)

        runBlocking{
            categorie = UserLoginActivity.recuperaCategorieFirebase()
        }

        val spinnerCategorieAdapter: ArrayAdapter<String> = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, categorie.stream().map { categoria -> categoria.nome }.collect(
            Collectors.toList()))
        spinnerCategorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        viewCategorie.adapter = spinnerCategorieAdapter

        val eTextNomeAggCateg = requireActivity().findViewById<EditText>(R.id.nome_aggiornato_categoria)
        val eTextNuovaSottoCateg = requireActivity().findViewById<EditText>(R.id.nome_nuova_sottocategoria)

        viewCategorie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val categoriaSelezionata = categorie[viewCategorie.selectedItemPosition]
                eTextNomeAggCateg.setText(categoriaSelezionata.nome)

                val layoutSottocategorie = activity!!.findViewById<LinearLayout>(R.id.layout_sottocategorie)

                if(UserLoginActivity.hasSottocategorie(categoriaSelezionata)) {
                    layoutSottocategorie.visibility = View.VISIBLE

                    val sottoCategorie = categoriaSelezionata.sottocategorie!!.stream().map { sottocategoria -> sottocategoria.nome }.collect(
                        Collectors.toList())
                    val spinnerSottoCategAdapter: ArrayAdapter<String> = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, sottoCategorie)
                    spinnerSottoCategAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                    viewSottoCateg.adapter = spinnerSottoCategAdapter
                } else {
                    layoutSottocategorie.visibility = View.GONE
                }


                val categoria = categorie[viewCategorie.selectedItemPosition]
                eTextNuovaSottoCateg.hint = "Nuova sottocategoria di ${categoria.nome}"

            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        val eTextNomeAggSottocateg = requireActivity().findViewById<EditText>(R.id.nome_aggiornato_sottocategoria)

        viewSottoCateg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val categoriaSelezionata = categorie[viewCategorie.selectedItemPosition]

                if(UserLoginActivity.hasSottocategorie(categoriaSelezionata)) {
                    val nomeSottoCateg = categorie[viewCategorie.selectedItemPosition].sottocategorie!!.toList().get(viewSottoCateg.selectedItemPosition).nome
                    eTextNomeAggSottocateg.setText(nomeSottoCateg)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        requireActivity().findViewById<Button>(R.id.aggiungi_categoria).setOnClickListener {
            val eTextNomeNuovaCateg = requireActivity().findViewById<EditText>(R.id.nome_nuova_categoria)
            val nomeNuovaCateg = eTextNomeNuovaCateg.text.toString()
            if(nomeNuovaCateg.isBlank())
                Toast.makeText(activity, "Il nome non puo'essere vuoto", Toast.LENGTH_SHORT).show()
            else {
                runBlocking{ creaNuovaCategoriaFirebaseFirestore(nomeNuovaCateg) }
                eTextNomeNuovaCateg.setText("")
                Toast.makeText(activity, "Categoria aggiunta", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().findViewById<Button>(R.id.modifica_categoria).setOnClickListener {
            val idCategoria = categorie[viewCategorie.selectedItemPosition].id
            val nomeAggCateg = eTextNomeAggCateg.text.toString()
            if(nomeAggCateg.isBlank())
                Toast.makeText(activity, "Il nome non puo'essere vuoto", Toast.LENGTH_SHORT).show()
            else {
                runBlocking{ modificaCategoriaFirebaseFirestore(idCategoria, nomeAggCateg) }
                Toast.makeText(activity, "Categoria modificata", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().findViewById<Button>(R.id.modifica_sottocategoria).setOnClickListener {
            val categoria = categorie[viewCategorie.selectedItemPosition]
            val idCategoria = categoria.id
            val idSottoCateg = categorie[viewCategorie.selectedItemPosition].sottocategorie!!.toList()[viewSottoCateg.selectedItemPosition].id
            val nomeAggSottoCateg = eTextNomeAggSottocateg.text.toString()
            if(nomeAggSottoCateg.isBlank())
                Toast.makeText(activity, "Il nome non puo'essere vuoto", Toast.LENGTH_SHORT).show()
            else {
                runBlocking{ modificaSottocategoriaFirebaseFirestore(idCategoria, idSottoCateg, nomeAggSottoCateg) }
                Toast.makeText(activity, "Sottocategoria di ${categoria.nome} modificata", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().findViewById<Button>(R.id.aggiungi_sottocategoria).setOnClickListener {
            val categoria = categorie[viewCategorie.selectedItemPosition]
            val idCategoria = categoria.id

            val nomeNuovaSottoCateg = eTextNuovaSottoCateg.text.toString()
            if(nomeNuovaSottoCateg.isBlank())
                Toast.makeText(activity, "Il nome non puo'essere vuoto", Toast.LENGTH_SHORT).show()
            else {
                runBlocking{ creaNuovaSottocategoriaFirebaseFirestore(idCategoria, nomeNuovaSottoCateg) }
                eTextNuovaSottoCateg.setText("")
                Toast.makeText(activity, "Sottocategoria di ${categoria.nome} aggiunta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //--- Accesso a dati statistici ---

    /**
     * Restituisce il numero di oggetti attualmente in vendita
     *
     * @author Amato Luca
     * @return Numero di oggetti
     */
    private suspend fun numeroOggettiInVendita(): Int {
        return database.collection(Annuncio.nomeCollection).whereEqualTo("venduto", false).get().await().size()
    }

    /**
     * Restituisce il numero di oggetti attualmente in vendita entro una certa distanza dalla posizione dell'utente
     *
     * @author Amato Luca
     * @param posizioneUtente Posizione dell'utente
     * @param distanzaKmMax Distanza massima in km entro cui cercare gli oggetti in vendita
     * @return Numero di oggetti
     */
    private suspend fun numeroOggettiInVenditaPerRaggioDistanza(posizioneUtente: Location, distanzaKmMax: Int): Int {
        return UserLoginActivity.recuperaAnnunciFiltrati(
            null,
            null,
            null,
            null,
            posizioneUtente,
            distanzaKmMax
        ).size
    }


    /**
     * Calcola il tempo medio degli Annunci che sono stati venduti
     *
     * @author Amato Luca
     * @return tempo medio annunci venduti
     */
    private suspend fun calcolaTempoMedioAnnunciVenduti(): Double{

        val myUtenti = UserLoginActivity.recuperaUtenti(auth.uid!!)
            .stream().filter{ utente-> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() != 0.0 } }.toList()

        return myUtenti.sumOf { utente -> runBlocking {  utente.calcolaTempoMedioAnnunciVenduti() } } / myUtenti.size
    }

    /**
     * Crea una nuova categoria
     *
     * @author Amato Luca
     * @param nomeNuovaCategoria Nome della nuova categoria
     */
    private suspend fun creaNuovaCategoriaFirebaseFirestore(nomeNuovaCategoria: String){
        database.collection("categoria").add(hashMapOf("nome" to  nomeNuovaCategoria)).await()
    }

    /**
     * Modifica una categoria esistente
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria
     * @param nomeAggiornatoCategoria Nome aggiornato della categoria
     */
    private suspend fun modificaCategoriaFirebaseFirestore(idCategoria: String,nomeAggiornatoCategoria: String){
        database.collection("categoria").document(idCategoria).update("nome", nomeAggiornatoCategoria).await()
    }

    /**
     * Modifica una sottocategoria esistente
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria a cui appartiene la sottocategoria
     * @param idSottocategoria Identificativo della sottocategoria
     * @param nomeAggiornatoSottocategoria Nome aggiornato della sottocategoria
     */
    private suspend fun modificaSottocategoriaFirebaseFirestore(idCategoria: String, idSottocategoria: String, nomeAggiornatoSottocategoria: String){
        database.collection("categoria").document(idCategoria).collection("sottocategoria").document(idSottocategoria).update("nome", nomeAggiornatoSottocategoria).await()
    }

    /**
     * Crea una nuova sottocategoria
     *
     * @author Amato Luca
     * @param idCategoria Identificativo della categoria a cui appartiene la sottocategoria
     * @param nomeNuovaSottocategoria Nome della nuova sottocategoria
     */
    private suspend fun creaNuovaSottocategoriaFirebaseFirestore(idCategoria: String, nomeNuovaSottocategoria: String){
        database.collection("categoria").document(idCategoria).collection("sottocategoria").add(
            hashMapOf("nome" to nomeNuovaSottocategoria)
        ).await()
    }
}
