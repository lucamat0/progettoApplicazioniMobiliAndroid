package it.uniupo.oggettiusati

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class SignUpActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val database = Firebase.firestore

    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        // -- SignUp Activity --
        val nome = findViewById<EditText>(R.id.nome).text.toString()
        val cognome = findViewById<EditText>(R.id.cognome).text.toString()
        val email = findViewById<EditText>(R.id.email).text.toString()
        val numeroDiTelefono = findViewById<EditText>(R.id.numeroDiTelefono).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        val btnMostraCalendario = findViewById<Button>(R.id.mostra_calendario)
        val btnNascondiCalendario = findViewById<Button>(R.id.nascondi_calendario)
        val datePicker = findViewById<DatePicker>(R.id.dataDiNascita)
        val mostraDataSelezionata = findViewById<TextView>(R.id.data)

        btnMostraCalendario.setOnClickListener {
            datePicker.visibility = View.VISIBLE
            btnNascondiCalendario.visibility = View.VISIBLE
        }
        btnNascondiCalendario.setOnClickListener {
            datePicker.visibility = View.GONE
            btnNascondiCalendario.visibility = View.GONE
        }

        var dataNascita = ""
        datePicker.setOnDateChangedListener { /*view*/ _, year, monthOfYear, dayOfMonth ->
            dataNascita = "${dayOfMonth}/${monthOfYear + 1}/${year}"
            mostraDataSelezionata.text = dataNascita
        }

        val buttonSignUp = findViewById<Button>(R.id.registrati)

        buttonSignUp.setOnClickListener {
            findViewById<TextView>(R.id.error_message).text = ""
            if (email.isNotBlank() &&
                password.isNotBlank() &&
                nome.isNotBlank() &&
                cognome.isNotBlank() &&
                numeroDiTelefono.isNotBlank() &&
                dataNascita.isNotBlank()) {
                Toast.makeText(this, "Campi non vuoti", Toast.LENGTH_SHORT).show()

                //Almeno un: numero, lettera maiuscola, lettera minuscola, carattere speciale, no spazi bianchi, lunga almeno 8 caratteri. -> DA SCRIVERE IN MANIERA PIÚ EFFICENTE, SE POSSIBILE.
                //Il numero di telefono é composto da 10 numeri
                if (password
                        .matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+\$).{8,}\$")) &&
                    numeroDiTelefono.length == 10 && numeroDiTelefono.isDigitsOnly()
                ) {
                    auth.createUserWithEmailAndPassword(
                        email,
                        password
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
                                        nome,
                                        cognome,
                                        dataNascita,
                                        numeroDiTelefono
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
                            findViewById<TextView>(R.id.error_message).text = task.exception?.message
                        }
                    }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Authentication error. Sign-up failed: weak password or wrong phone number format.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Authentication error. Sign-up failed: empty fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val buttonLogin = findViewById<Button>(R.id.login)
        buttonLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }

//    private fun validaDataGgMmAaaa(dataNascita: String): Boolean {
//        if(dataNascita.length != 10 || !(dataNascita.replace("/","").isDigitsOnly()) || !dataNascita.contains("/"))
//            return false
//        val numEl = arrayOf(2, 2, 4)
//        val numDate = arrayOf(31, 12, 2022)
//        val monthNotThirtyOneDay = arrayOf(2, 4, 6, 9, 11)
//        val dayNumNonthNotThirtyOne = arrayOf(28, 30)
//
//        val dateToken = dataNascita.split("/")
//        for ((i, token) in dateToken.withIndex()){
//            if(token.length != numEl[i] || token.toInt() > numDate[i])
//                return false
//        }
//
//        if(dateToken[0].toInt() in monthNotThirtyOneDay)
//
//
//        return true
//    }

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
                "eliminato" to false,
                "userId" to utenteDaSalvareId
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