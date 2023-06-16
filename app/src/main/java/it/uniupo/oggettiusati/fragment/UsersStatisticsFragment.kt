package it.uniupo.oggettiusati.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.R
import kotlinx.coroutines.runBlocking


class UsersStatisticsFragment : Fragment() {
    private val database = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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
