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
import com.google.firebase.auth.FirebaseUser
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

        database = Firebase.database
        auth = FirebaseAuth.getInstance();

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.login)
        val signUpButton = findViewById<Button>(R.id.signUp)

        loginButton.setOnClickListener {

            //Toast.makeText(baseContext, "Logica login da implementare!", Toast.LENGTH_SHORT).show()

            // Qui dobbiamo implementare la logica del login... XML per diversi utenti? Amministratore e non amministratore ?

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed: incorrect credentials.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }

        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }

    //login
    private fun updateUI(user: FirebaseUser?) {
        if(user == null){
            Toast.makeText(baseContext, "Authentication failed: utente vuoto.", Toast.LENGTH_SHORT).show()
            Log.w("debug-login", "signInWithEmail failure")
        }
        else{
            val currentUser = auth.currentUser

            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null)
            FirebaseAuth.getInstance().signOut();
            //startActivity(Intent(this,LoginActivity::class.java))
        else{
            Toast.makeText(this, "Utente non loggato al momento", Toast.LENGTH_LONG).show()
        }
    }

    //fine login
}