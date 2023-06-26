package it.uniupo.oggettiusati.fragment

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
import java.util.stream.Collectors


class UsersStatisticsFragment : Fragment() {
//    private val database = Firebase.firestore
//    private val auth = FirebaseAuth.getInstance()

    lateinit var categorie: List<UserLoginActivity.Categoria>
//    lateinit var viewCategorie: Spinner

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

        val viewCategorie = fragmentRootView.findViewById<Spinner>(R.id.categorie)
        val viewSottoCateg = fragmentRootView.findViewById<Spinner>(R.id.sottocategorie)

        runBlocking{
            categorie = UserLoginActivity.recuperaCategorieFirebase()
        }

        val spinnerCategorieAdapter: ArrayAdapter<String> = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, categorie.stream().map { categoria -> categoria.nome }.collect(
            Collectors.toList()))
        spinnerCategorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        viewCategorie.adapter = spinnerCategorieAdapter

        viewCategorie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val categoriaSelezionata = categorie[viewCategorie.selectedItemPosition]
                fragmentRootView.findViewById<EditText>(R.id.nome_aggiornato_categoria).setText(categoriaSelezionata.nome)

                if(UserLoginActivity.hasSottocategorie(categoriaSelezionata)) {
                    fragmentRootView.findViewById<LinearLayout>(R.id.layout_sottocategorie).visibility = View.VISIBLE

                    val sottoCategorie = categoriaSelezionata.sottocategorie!!.stream().map { sottocategoria -> sottocategoria.nome }.collect(
                        Collectors.toList())
                    val spinnerSottoCategAdapter: ArrayAdapter<String> = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, sottoCategorie)
                    spinnerSottoCategAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                    viewSottoCateg.adapter = spinnerSottoCategAdapter
                } else {
                    fragmentRootView.findViewById<LinearLayout>(R.id.layout_sottocategorie).visibility = View.GONE
                }

                val viewNuovaSottoCateg = fragmentRootView.findViewById<EditText>(R.id.nome_nuova_sottocategoria)
                val categoria = categorie[viewCategorie.selectedItemPosition]
                viewNuovaSottoCateg.hint = "Nuova sottocategoria di ${categoria.nome}"

            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

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
                    fragmentRootView.findViewById<EditText>(R.id.nome_aggiornato_sottocategoria).setText(nomeSottoCateg)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        fragmentRootView.findViewById<Button>(R.id.aggiungi_categoria).setOnClickListener {
            val nomeNuovaCateg = fragmentRootView.findViewById<EditText>(R.id.nome_nuova_categoria).text

        }

        fragmentRootView.findViewById<Button>(R.id.modifica_categoria).setOnClickListener {
            val idCategoria = categorie[viewCategorie.selectedItemPosition].id
            val nomeAggCateg = fragmentRootView.findViewById<EditText>(R.id.nome_aggiornato_categoria).text

        }

        fragmentRootView.findViewById<Button>(R.id.modifica_sottocategoria).setOnClickListener {
            val idCategoria = categorie[viewCategorie.selectedItemPosition].id
            val idSottoCateg = categorie[viewCategorie.selectedItemPosition].sottocategorie?.toList()?.get(viewSottoCateg.selectedItemPosition)?.id
            val nomeAggSottoCateg = fragmentRootView.findViewById<EditText>(R.id.nome_aggiornato_sottocategoria).text

        }

        fragmentRootView.findViewById<Button>(R.id.aggiungi_sottocategoria).setOnClickListener {
            val categoria = categorie[viewCategorie.selectedItemPosition]
            val idCategoria = categoria.id
            val viewNuovaSottoCateg = fragmentRootView.findViewById<EditText>(R.id.nome_nuova_sottocategoria)
            val nomeAggSottoCateg = viewNuovaSottoCateg.text

        }





        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val numTotOgg = 0
            val tMedioGiorni = 0.0
            activity?.findViewById<TextView>(R.id.num_ogg_in_vendita)?.text = "Oggetti in vendita in questo momento: ${numTotOgg}"
            activity?.findViewById<TextView>(R.id.tempo_medio_vendita)?.text = "Tempo medio vendita di un oggetto: ${tMedioGiorni} giorni "

            activity?.findViewById<Button>(R.id.btn_ricerca_admin)?.setOnClickListener {
                val lat = activity?.findViewById<EditText>(R.id.latitudine_ricerca_admin)?.text.toString()
                val lon = activity?.findViewById<EditText>(R.id.longitudine_ricerca_admin)?.text.toString()
                val dist = activity?.findViewById<Slider>(R.id.adminDistanceSlider)?.value?.toInt()!!
                if(arrayOf(lat, lon).all { it.isNotBlank() } && dist > 0) {
                    val numOggDistanzaPunto = 0 //ricercaOggettiDistanzaMinore().size
                    activity?.findViewById<TextView>(R.id.risultato_ricerca_admin)?.text = "Oggetti trovati: ${numOggDistanzaPunto}"
                } else {
                    Toast.makeText(activity, "Alcuni campi vuoti, compilali", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
