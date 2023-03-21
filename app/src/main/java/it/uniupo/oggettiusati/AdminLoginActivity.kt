package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AdminLoginActivity : AppCompatActivity() {

    private  lateinit var database: FirebaseFirestore
    private var userId : String = "0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        //admin activity

        database = Firebase.firestore

        val extras = intent.extras
        userId = extras?.getString("userId").toString()
        var username = ""

        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if(document != null){
                username = document.get("nome").toString()
            } else {
                Log.w("document error","Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}