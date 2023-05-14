package it.uniupo.oggettiusati

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Date

class ChatFragment : Fragment() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //...
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentRootView = inflater.inflate(R.layout.fragment_home, container, false)
        //context: activity
        //view or fragmentRootView object to use to call findViewById(): fragmentRootView



        return fragmentRootView //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //perform here operation when fragment changes and this become visible (i.e. do updates dynamically when fragment is again visible)

        Toast.makeText(activity, "Sei nella sezione chat", Toast.LENGTH_SHORT).show()
    }

}
