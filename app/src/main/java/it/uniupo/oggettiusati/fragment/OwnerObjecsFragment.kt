package it.uniupo.oggettiusati.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.uniupo.oggettiusati.R
import it.uniupo.oggettiusati.adapter.CustomAdapter
import kotlinx.coroutines.runBlocking

class OwnerObjecsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_owner_objects, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView

        return fragmentRootView
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            //getting the recyclerView by its id
            val recyclerVu = view?.findViewById<RecyclerView>(R.id.recyclerview_owner_objects)
            //this creates a vertical layout Manager
            recyclerVu?.layoutManager = LinearLayoutManager(activity)

            //Ogni volta che il mio fragment viene messo in primo piano recupero i miei annunci preferiti
            val annunciProprietario = FavoritesFragment.recuperaAnnunciPreferitiFirebaseFirestore(auth.uid!!, activity!!)

            //this will pass the ArrayList to our Adapter
            val adapter = CustomAdapter(annunciProprietario, R.layout.card_view_design)

            //setting the Adapter with the recyclerView
            recyclerVu?.adapter = adapter
        }
    }

}
