package it.uniupo.a05_01_auth

import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.R

private lateinit var auth: FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance();

        database = Firebase.database;

        // -- SignUp Activity --
        val nome = findViewById<EditText>(R.id.nome);
        val cognome = findViewById<EditText>(R.id.cognome);
        val email = findViewById<EditText>(R.id.email);
        val password = findViewById<EditText>(R.id.password);
        val dataNascita = findViewById<EditText>(R.id.dataDiNascita);

        val buttonSignUp = findViewById<Button>(R.id.registrati);

        buttonSignUp.setOnClickListener{
            if(email.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()){
                Toast.makeText(this, "Email e password validi", Toast.LENGTH_SHORT).show()

                //Almeno un: numero, lettera maiuscola, lettera minuscola, carattere speciale, no spazi bianchi, lunga almeno 8 caratteri. -> DA SCRIVERE IN MANIERA PIÚ EFFICENTE, SE POSSIBILE.

                if(password.text.toString().matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+\$).{8,}\$"))) {
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Creazione", "createUserWithEmail:success")

                        val user = auth.currentUser

                        // recupero Id utente, appena memorizzato, ovviamente,
                        // avendolo essendo appena creato NON é possibile che sia uguale a null,
                        // quindi lo specifico con !!
                        val userId = user!!.uid

                        val database = Firebase.database.reference.child("users").child(userId)

                        val userValues = hashMapOf(
                            "nome" to nome.text.toString(),
                            "cognome" to cognome.text.toString(),
                            "dataNascita" to dataNascita.text.toString(),
                        )

                        database.setValue(userValues)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext, "I dati sono stati salvati con successo", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(baseContext, "Si è verificato un errore durante il salvataggio dei dati", Toast.LENGTH_SHORT).show()
                            }
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
            else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                //updateUI(null)
            }
        }
    }
}