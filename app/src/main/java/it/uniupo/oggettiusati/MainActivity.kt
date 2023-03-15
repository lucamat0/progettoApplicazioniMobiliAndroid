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


class MainActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance();

    private lateinit var database: FirebaseFirestore

    val currentUser = auth.currentUser //istanza dell'utente loggato di FB Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance();
        database = Firebase.firestore;

        val emailView = findViewById<EditText>(R.id.email)
        val passwordView = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.login)
        val signUpButton = findViewById<Button>(R.id.signUp)

        loginButton.setOnClickListener {
            val email = emailView.text.toString()
            val password = passwordView.text.toString()
            if(email.isNotEmpty() && password.isNotEmpty() && email.isNotBlank() && password.isNotBlank()){
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign-in", "user signed in")
                            updateUI(currentUser)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign-in", "sign in failed ", task.exception)
                            Toast.makeText(baseContext, "Authentication failed: incorrect credentials.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Authentication failed: credenziali vuote.", Toast.LENGTH_LONG).show()
            }
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateUI(user: FirebaseUser?) {
        if(user == null){
            Log.w("debug-login", "Errore: utente vuoto")
        }
        else{
            val userID = currentUser?.uid
            var isAdmin = ""
            if(userID != null){

                val userRef = database.collection("users").document(userID)
                userRef.get().addOnSuccessListener { document ->
                    if(document != null){
                        isAdmin = document.get("amministratore").toString()
                    } else {
                        Log.w("document error","Error: document is null")
                    }

                    if(isAdmin.equals("0")){
                        startActivity(Intent(this, UserLoginActivity::class.java))
                    } else if(isAdmin.equals("1")){
                        startActivity(Intent(this, AdminLoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "Errore: isadmin vale ${isAdmin}", Toast.LENGTH_LONG).show()
                    }

                }
            } else {
                Log.w("update ui user","Error: user is null")
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if(currentUser != null)
            FirebaseAuth.getInstance().signOut(); //provvisiorio per testare il login
            //startActivity(Intent(this,LoginActivity::class.java))
        else{
            Toast.makeText(this, "Utente non loggato al momento", Toast.LENGTH_LONG).show()
        }
    }

}