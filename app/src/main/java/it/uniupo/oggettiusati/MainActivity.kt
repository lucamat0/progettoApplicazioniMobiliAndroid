package it.uniupo.oggettiusati

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.type.Date

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val nomeCognome = findViewById<EditText>(R.id.nomeCognome)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val dataNascita = findViewById<EditText>(R.id.dataDiNascita)

        val button = findViewById<Button>(R.id.registrati);

        button.setOnClickListener{

            if(email.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()){

                Toast.makeText(this, "Email e password validi", Toast.LENGTH_SHORT).show()

                //Almeno un: numero, lettera maiuscola, lettera minuscola, carattere speciale, no spazi bianchi, lunga almeno 8 caratteri. -> DA SCRIVERE IN MANIERA PIÚ EFFICENTE, SE POSSIBILE.
                if(password.text.toString().matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+\$).{8,}\$"))) {
                    auth.createUserWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    ).addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Creazione", "createUserWithEmail:success")

                                val user = auth.currentUser

                                // recupero Id utente, appena memorizzato, ovviamente,
                                // avendolo essendo appena creato NON é possibile che sia uguale a null,
                                // quindi lo specifico con !!
                                val userId = user!!.uid

                                /*

                                -> Da aprire il collegamento con Real Time DataBase e salvare le informazioni.

                                val database = Firebase.database.reference.child("users").child(userId)

                                val userValues = hashMapOf(
                                    "nomeCognome" to nomeCognome.text.toString(),
                                    "dataNascita" to dataNascita.text.toString()
                                )

                                database.setValue(userValues)
                                    .addOnSuccessListener {
                                        // I dati sono stati salvati con successo
                                    }
                                    .addOnFailureListener {
                                        // Si è verificato un errore durante il salvataggio dei dati
                                    }

                                */
                            }
                            else {
                                // La registrazione dell'utente non è andata a buon fine
                                Log.w("Creazione", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }

                            //updateUI(user)
                        }
                }
                else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
            else{
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                //updateUI(null)
            }
        }
    }
}