package it.uniupo.oggettiusati

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        // -- SignUp Activity --

        lateinit var nome :String
        lateinit var cognome :String
        lateinit var email :String
        lateinit var numeroDiTelefono :String
        lateinit var password :String
        lateinit var confermaPassword :String

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
            nome = findViewById<EditText>(R.id.nome).text.toString()
            cognome = findViewById<EditText>(R.id.cognome).text.toString()
            email = findViewById<EditText>(R.id.email).text.toString()
            numeroDiTelefono = findViewById<EditText>(R.id.numeroDiTelefono).text.toString()
            password = findViewById<EditText>(R.id.password).text.toString()
            confermaPassword = findViewById<EditText>(R.id.ripeti_password).text.toString()

            findViewById<TextView>(R.id.error_message).text = ""
            if (email.isNotBlank() &&
                password.isNotBlank() &&
                confermaPassword.isNotBlank() &&
                nome.isNotBlank() &&
                cognome.isNotBlank() &&
                numeroDiTelefono.isNotBlank() &&
                dataNascita.isNotBlank()) {
                Toast.makeText(this, "Campi non vuoti", Toast.LENGTH_SHORT).show()

                //Almeno un: numero, lettera maiuscola, lettera minuscola, carattere speciale, no spazi bianchi, lunga almeno 8 caratteri. -> DA SCRIVERE IN MANIERA PIÚ EFFICENTE, SE POSSIBILE.
                //Il numero di telefono é composto da 10 numeri
                if (password
                        .matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+\$).{8,}\$")) &&
                    password == confermaPassword &&
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
                        "Authentication error. Sign-up failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    findViewById<TextView>(R.id.error_message).text = "Weak password, wrong phone number format or passwords don't matches."
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Authentication error. Sign-up failed: empty fields.",
                    Toast.LENGTH_SHORT
                ).show()
                findViewById<TextView>(R.id.error_message).text = "Alcuni campi sono vuoti, compila tutti i campi"
                Log.d("registrazione", "[${email}] [${password}] [${nome}] [${cognome}] [${numeroDiTelefono}] [${dataNascita}]")
            }
        }

        val buttonLogin = findViewById<Button>(R.id.login)
        buttonLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /**
     * Salva i dettagli dell'utente su Firebase
     *
     * @author Amato Luca
     * @param utenteDaSalvareId Identificativo dell'utente da salvare, equivale a quello generato da auth
     * @param nome Nome dell'utente
     * @param cognome Cognome dell'utente
     * @param dataNascita Data di nascita dell'utente
     * @param numeroDiTelefono Numero di telefono dell'utente
     * @return Identificato dell'utente salvato se l'operazione ha avuto successo altrimenti null
     */
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
                "amministratore" to false,
                "sospeso" to false,
                "eliminato" to false,
                "userId" to utenteDaSalvareId
            )

            database.collection(UserLoginActivity.Utente.nomeCollection).document(utenteDaSalvareId).set(userValues).await()

            startActivity(Intent(this, MainActivity::class.java))
            finish()

            return utenteDaSalvareId
        }
        Log.e("Creazione documento utente", "Errore durante la creazione del documento associato all'utente")
        return null
    }
}