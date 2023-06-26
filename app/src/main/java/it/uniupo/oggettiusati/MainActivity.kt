package it.uniupo.oggettiusati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()

    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        database = Firebase.firestore

        if (auth.currentUser == null) {
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
     * Verifica le credenziali dell'utente e effettua il login se le credenziali sono valide
     *
     * @author Amato Luca
     * @param email Email dell'utente
     * @param password Password dell'utente
     */
    private fun checkCredentialsAndLogin(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        runBlocking {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign in", "user signed in")

                            val user = auth.currentUser

                            val userId = user!!.uid

                            login(userId)
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "Sign in failed ", task.exception)

                        Toast.makeText(
                            baseContext,
                            "Authentication failed: incorrect credentials.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "Authentication failed: credenziali vuote.", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Effettua il login per l'utente specificato se non e' sospeso e/o eliminato
     *
     * @author Amato Luca
     * @param userId Identificativo dell'utente
     */
    private suspend fun login(userId: String){

        val myUtente = UserLoginActivity.recuperaUtente(userId)

        if (!(myUtente.getSospeso() || myUtente.getEliminato()))
            updateUI(userId)
        else{

            Log.d("Sign in", "User is suspended or deleted")

            if(myUtente.getEliminato())
                Toast.makeText(baseContext, "Authentication failed: l'utente non esiste.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(baseContext, "Authentication failed: l'utente e' sospeso.", Toast.LENGTH_SHORT).show()

            FirebaseAuth.getInstance().signOut() //forzo l'uscita
        }
    }

    /**
     * Aggiorna l'interfaccia dell'utente in base al suo identificativo
     *
     * @author Amato Luca
     * @param userId Identificato dell'utente
     */
    private fun updateUI(userId: String) {

        val userCollection = database.collection(UserLoginActivity.Utente.nomeCollection).document(userId)

        userCollection.get().addOnSuccessListener { document ->
                if (document != null) {
                    if(document.getBoolean("amministratore") as Boolean) {
                        Toast.makeText(this, "Caricamento...", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AdminLoginActivity::class.java))
                    }
                    else{
                        Toast.makeText(this, "Caricamento...", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, UserLoginActivity::class.java))
                        finish()
                    }
                } else {
                    Log.w("document error", "Error: document is null")
                }
            }
    }

    /**
     * Callback chiamato quando l'activity sta diventando visibile, effettua il controllo se l'utente è già autenticato e aggiorna l'interfaccia
     *
     * @author Amato Luca
     */
    public override fun onStart() {
        super.onStart()

        runBlocking {
            // Check if user is signed in (non-null) and update UI accordingly.
            val userId = auth.currentUser?.uid
            if (userId != null)
                login(userId)
            else
                Toast.makeText(this@MainActivity, "Utente non loggato al momento", Toast.LENGTH_SHORT).show()
        }
    }
}