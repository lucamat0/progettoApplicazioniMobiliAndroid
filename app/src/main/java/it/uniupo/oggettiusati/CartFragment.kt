package it.uniupo.oggettiusati

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.util.Date

class CartFragment : Fragment() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //...
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_cart, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView

        //getting the recyclerView by its id
        val recyclerVu = fragmentRootView?.findViewById<RecyclerView>(R.id.recyclerview_cart)
        //this creates a vertical layout Manager
        recyclerVu?.layoutManager = LinearLayoutManager(activity)
        //this will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(HashMap(), R.layout.card_view_remove_design) //DA RIEMPIRE
        //setting the Adapter with the recyclerView
        recyclerVu?.adapter = adapter




        return fragmentRootView //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        Toast.makeText(activity, "Sei nella sezione carrello", Toast.LENGTH_SHORT).show()

    }

}
