package it.uniupo.oggettiusati

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.a05_01_auth.SignUpActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    //Equivale alla dichiarazione di variabile statiche in java
    companion object {

        public lateinit var database: FirebaseFirestore;

        public lateinit var currentUser: FirebaseUser;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance();
        database = Firebase.firestore;

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.login)
        val signUpButton = findViewById<Button>(R.id.signUp)

        loginButton.setOnClickListener {
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")

                        currentUser = auth.currentUser!!

                        updateUI();
                    } else
                        updateUI();
                }

        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }


    public  fun updateUI() {
        if(currentUser == null){
            Toast.makeText(this, "Authentication failed: utente vuoto.", Toast.LENGTH_SHORT).show()
            Log.w("debug-login", "signInWithEmail failure")
        }
        else{

            //Se viene loggato è perchè utente esiste.
            val userRif = database.collection("users").document(currentUser!!.uid)

            // Da implementare la logica!

            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }

    //-- Provvisorio ---
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

}