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
import it.uniupo.oggettiusati.AdminLoginActivity
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



        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val numTotOgg = AdminLoginActivity.numeroOggettiInVendita()
            val tMedioGiorni = AdminLoginActivity.calcolaTempoMedioAnnunciVenduti()
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
                        val numOggDistanzaPunto = AdminLoginActivity.numeroOggettiInVenditaPerRaggioDistanza(posiz, dist) //ricercaOggettiDistanzaMinore().size
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
                runBlocking{ AdminLoginActivity.creaNuovaCategoriaFirebaseFirestore(nomeNuovaCateg) }
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
                runBlocking{ AdminLoginActivity.modificaCategoriaFirebaseFirestore(idCategoria, nomeAggCateg) }
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
                runBlocking{ AdminLoginActivity.modificaSottocategoriaFirebaseFirestore(idCategoria, idSottoCateg, nomeAggSottoCateg) }
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
                runBlocking{ AdminLoginActivity.creaNuovaSottocategoriaFirebaseFirestore(idCategoria, nomeNuovaSottoCateg) }
                eTextNuovaSottoCateg.setText("")
                Toast.makeText(activity, "Sottocategoria di ${categoria.nome} aggiunta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
