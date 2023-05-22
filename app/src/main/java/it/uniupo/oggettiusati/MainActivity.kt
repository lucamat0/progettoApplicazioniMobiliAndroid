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

        auth = FirebaseAuth.getInstance()
        database = Firebase.firestore

        if(auth.currentUser == null) {
            setContentView(R.layout.activity_main)

            val emailView = findViewById<EditText>(R.id.email)
            val passwordView = findViewById<EditText>(R.id.password)
//            emailView.text.clear()
//            passwordView.text.clear()

            //Tastiera che si apre sul campo email
            emailView.requestFocus()

            val loginButton = findViewById<Button>(R.id.login)
            val signUpButton = findViewById<Button>(R.id.signUp)

            loginButton.setOnClickListener {
                Toast.makeText(this, "Accesso in corso...", Toast.LENGTH_SHORT).show()
                val email = emailView.text.toString()
                val password = passwordView.text.toString()

                checkCredentialsAndLogin(email, password)
            }

            signUpButton.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        }

    }

    /**
     * Checks
     *      if credential aren't blank or empty,
     *      if exists and are corrects on Firebase Authentication,
     * and once logged
     *      if user is *sospended* and if is *admin* through [updateUI]
     *
     * Note: admin **can't** be suspend
     *
     * @param [email] email as String used to perform authentication (try to login)
     * @param [password] password as String used to perform authentication (try to login)
     *
     */
    private fun checkCredentialsAndLogin(email: String, password: String) {
        if(email.isNotEmpty() && password.isNotEmpty() && email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign in", "user signed in")

                        val user = auth.currentUser

                        val userID = user?.uid

                        checkSuspendedAndLogin(userID)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "sign in failed ", task.exception)
                        Toast.makeText(baseContext, "Authentication failed: incorrect credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Authentication failed: credenziali vuote.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkSuspendedAndLogin(userId: String?) {
        val userRef: DocumentReference
        var utenteSospeso = true
        if(userId != null) {
            userRef = database.collection("utente").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    if(document.get("amministratore").toString().equals("1")) {
                        utenteSospeso = false //non diamo la possibilita' di avere l'amministratore sospeso (ne di sospendersi)
                    } else {
                        utenteSospeso = document.get("sospeso") == true
                    }
                } else { Log.w("document error", "Error: document is null") }

                if (utenteSospeso) {
                    Log.d("Sign in", "User is suspended")
                    Toast.makeText(baseContext, "Authentication failed: l'utente e' sospeso.", Toast.LENGTH_SHORT).show()
                } else {
                    updateUI(auth.currentUser)
                }
            }
        }
    }

    /**
     *
     * If user is not sospended the ui should update
     * This function check if user is admin and loads the corrispondent Activity
     *
     */

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            Log.w("debug-login", "Errore: utente vuoto")
        } else {
            val userID = user.uid
            lateinit var isAdmin: String
            val userRef = database.collection("utente").document(userID)

            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    isAdmin = document.get("amministratore").toString()
                    if (isAdmin.equals("0")) {
                        Toast.makeText(this, "Caricamento...", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, UserLoginActivity::class.java)
                        i.putExtra("userId", userID)
                        startActivity(i)
                        finish()
                    } else if (isAdmin.equals("1")) {
                        Toast.makeText(this, "Caricamento...", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, AdminLoginActivity::class.java)
                        i.putExtra("userId", userID)
                        startActivity(i)
                    } else {
                        //Toast.makeText(this, "Errore: isAdmin vale ${isAdmin}", Toast.LENGTH_LONG).show()
                        Log.w("admin field error", "Errore: isAdmin vale ${isAdmin}")
                    }
                } else {
                    Log.w("document error", "Error: document is null")
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val user = auth.currentUser
        if(user != null) {
            checkSuspendedAndLogin(user.uid)

//            FirebaseAuth.getInstance().signOut() //provvisiorio per testare il login
            //startActivity(Intent(this, LoginActivity::class.java))
        } else {
            Toast.makeText(this, "Utente non loggato al momento", Toast.LENGTH_SHORT).show()
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
                                    Log.d("Creazione documento utente", "La creazione dell'utente è andata a buon fine!")
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                .addOnFailureListener { e ->

                                    Log.w("Creazione documento utente", "Errore durante la creazione del documento associato all'utente", e)

                                    //Se il documento non si é riuscito a creare bisogna eliminare utente
                                    user.delete()

                                }
                        } else {
                            // La registrazione dell'utente non è andata a buon fine
                            Log.w("Creazione", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed: error creating user.", Toast.LENGTH_SHORT).show()
                        }
                    }
    */

    //fine test inserimento manuale

}