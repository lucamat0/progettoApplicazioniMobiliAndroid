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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()

    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = Firebase.firestore

        val emailView = findViewById<EditText>(R.id.email)
        val passwordView = findViewById<EditText>(R.id.password)

        //Tastiera che si apre sul campo email
        emailView.requestFocus()

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

                            val user = auth.currentUser

                            //codice da rifattorizzare
                            val userID = user?.uid
                            val userRef : DocumentReference
                            var utenteSospeso = true
                            if(userID != null) {
                                userRef = database.collection("users").document(userID)
                                userRef.get().addOnSuccessListener { document ->
                                    if (document != null) {
                                        if(document.get("amministratore").toString().equals("1")) {
                                            utenteSospeso = false
                                        } else {
                                            utenteSospeso = document.get("sospeso") == true
                                        }
                                    } else {
                                        Log.w("document error", "Error: document is null")
                                    }

                                    if (utenteSospeso) {
                                        Log.d("Sign-in", "User is suspended")
                                        Toast.makeText(baseContext, "Authentication failed: l'utente e' sospeso.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        updateUI(user)
                                    }
                                }
                            }
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
        if (user == null) {
            Log.w("debug-login", "Errore: utente vuoto")
        } else {
            val userID = user.uid

            lateinit var isAdmin : String
            val userRef = database.collection("users").document(userID)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    isAdmin = document.get("amministratore").toString()
                } else {
                    Log.w("document error", "Error: document is null")
                }

                if (isAdmin.equals("0")) {
                    val i = Intent(this, UserLoginActivity::class.java)
                    i.putExtra("userId", userID)
                    startActivity(i)
                    finish()
                } else if (isAdmin.equals("1")) {
                    val i = Intent(this, AdminLoginActivity::class.java)
                    i.putExtra("userId", userID)
                    startActivity(i)
                } else {
                    //Toast.makeText(this, "Errore: isAdmin vale ${isAdmin}", Toast.LENGTH_LONG).show()
                    Log.w("admin field error", "Errore: isAdmin vale ${isAdmin}")
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val user = auth.currentUser
        if(user != null) {
            FirebaseAuth.getInstance().signOut() //provvisiorio per testare il login
            //startActivity(Intent(this,LoginActivity::class.java))
        }else{
            Toast.makeText(this, "Utente non loggato al momento", Toast.LENGTH_LONG).show()
        }
    }

    //test inserimento manuale

    /*auth.createUserWithEmailAndPassword("admin@gmail.com", "Test+123").addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Creazione", "createUserWithEmail:success")

                            val user = auth.currentUser

                            // recupero Id utente appena memorizzato,
                            // avendolo appena creato NON é possibile che sia null,
                            // quindi lo specifico con !!
                            val userId = user!!.uid

                            val userValues = hashMapOf(
                                "nome" to "admin",
                                "cognome" to "surmin",
                                "dataNascita" to "02/04/2008",
                                "amministratore" to 1,
                                "sospeso" to false
                            )

                            database.collection("users").document(userId)
                                .set(userValues)
                                .addOnSuccessListener {
                                    Log.d("Creazione documento utente","La creazione dell'utente è andata a buon fine!")
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                .addOnFailureListener{ e ->

                                    Log.w("Creazione documento utente","Errore durante la creazione del documento associato all'utente",e)

                                    //Se il documento non si é riuscito a creare bisogna eliminare utente
                                    user.delete()

                                }
                        }
                        else {
                            // La registrazione dell'utente non è andata a buon fine
                            Log.w("Creazione", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed: error creating user.", Toast.LENGTH_SHORT).show()
                        }
                    }
    */

    //fine test inserimento manuale

}