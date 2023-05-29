package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class SignUpActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // -- SignUp Activity --
        val nome = findViewById<EditText>(R.id.nome)
        val cognome = findViewById<EditText>(R.id.cognome)
        val email = findViewById<EditText>(R.id.email)
        val numeroDiTelefono = findViewById<EditText>(R.id.numeroDiTelefono)
        val password = findViewById<EditText>(R.id.password)
        val dataNascita = findViewById<EditText>(R.id.dataDiNascita)

        val buttonSignUp = findViewById<Button>(R.id.registrati)

        buttonSignUp.setOnClickListener {
            if (email.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()) {
                Toast.makeText(this, "Email e password non vuoti", Toast.LENGTH_SHORT).show()

                //Almeno un: numero, lettera maiuscola, lettera minuscola, carattere speciale, no spazi bianchi, lunga almeno 8 caratteri. -> DA SCRIVERE IN MANIERA PIÚ EFFICENTE, SE POSSIBILE.
                //Il numero di telefono é composto da 10 numeri
                if (password.text.toString()
                        .matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+\$).{8,}\$")) && numeroDiTelefono.text.length == 10
                ) {
                    auth.createUserWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    ).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Creazione", "createUserWithEmail:success")

                            user = auth.currentUser!!

                            //If the new account was created, the user is also signed in, use getCurrentUser() to get user info.
                            //noi pero' vogliamo che esca ed esegua il login con le sue nuove credenziali
                            if(auth.currentUser != null)
                                FirebaseAuth.getInstance().signOut()

                            // recupero Id utente appena memorizzato
                            val userId = user.uid

                            runBlocking {

                                if (salvaUtenteSuFirebaseFirestore(
                                        userId,
                                        nome.text.toString(),
                                        cognome.text.toString(),
                                        dataNascita.text.toString(),
                                        numeroDiTelefono.text.toString()
                                    ) == null
                                ) {
                                    //Se non si é riuscito a creare il documento bisogna eliminare utente
                                    user.delete()
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            // La registrazione dell'utente non è andata a buon fine
                            Log.w("Creazione", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed: error creating user.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Authentication error. Sign-up failed: weak password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Authentication error. Sign-up failed: empty credentials.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val buttonLogin = findViewById<Button>(R.id.login)
        buttonLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }

    //Se tutto è andato bene ritorna idDocumento appena creato, che coincide con id del utente che si trova in autentication.
    suspend fun salvaUtenteSuFirebaseFirestore(
        utenteDaSalvareId: String,
        nome: String,
        cognome: String,
        dataNascita: String,
        numeroDiTelefono: String
    ): String? {

        //Applico il controllo che il numero deve essere composto da 10 caratteri numerici,
        //per una questione di riusabilità, questo controllo viene comunque effettuato nel front end
        if (numeroDiTelefono.length == 10) {
            val userValues = hashMapOf(
                "nome" to nome,
                "cognome" to cognome,
                "dataNascita" to dataNascita,
                "numeroDiTelefono" to numeroDiTelefono,
                "amministratore" to 0,
                "sospeso" to false,
                "eliminato" to false
            )

            val inserimentoDatiUtente =
                database.collection("utente").document(utenteDaSalvareId).set(userValues).await()

            //Tutto é andato bene
            if (inserimentoDatiUtente == null) {
                Log.d(
                    "Creazione documento utente",
                    "La creazione dell'utente è andata a buon fine!"
                )
                startActivity(Intent(this, MainActivity::class.java))

                return utenteDaSalvareId
            } else {
                Log.e(
                    "Creazione documento utente",
                    "Errore durante la creazione del documento associato all'utente"
                )
                return null
            }
        } else {
            return null
        }
    }
}