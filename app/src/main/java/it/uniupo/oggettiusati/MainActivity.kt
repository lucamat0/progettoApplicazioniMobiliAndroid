package it.uniupo.oggettiusati

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.type.Date
import it.uniupo.a05_01_auth.SignUpActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // -> Utile per vedere se si tratta del amministratore o no! - Da utilizzare quando implementiamo la logica del loginButton!
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.login)
        val signUpButton = findViewById<Button>(R.id.signUp)

        loginButton.setOnClickListener {

            Toast.makeText(baseContext, "Logica login da implementare!", Toast.LENGTH_SHORT).show()

            // Qui dobbiamo implementare la logica del login... XML per diversi utenti? Amministratore e non amministratore ?

            startActivity(Intent(this, LoginActivity::class.java))
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}