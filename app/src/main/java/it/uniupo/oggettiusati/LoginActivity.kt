package it.uniupo.oggettiusati



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

    /*
        val email = findViewById<EditText>(R.id.username);
        val password = findViewById<EditText>(R.id.password);

        val buttonLogin = findViewById<Button>(R.id.login);
        val buttonRegistrati = findViewById<Button>(R.id.registrati);

        buttonLogin.setOnClickListener{
            login(email,password);
        }

        buttonRegistrati.setOnClickListener{
           registrati();
        }
    */

        registrati("luca","amato",1,"lucaamato38@gmail.com","pippo");

    }


    fun login(username: EditText, password: EditText){

        /*
        if(username.toString().isEmpty()){

            username.error = "Inserisci un username valido!";
            username.requestFocus();
            return;
        }
        else if(password.toString().isEmpty()){

            password.error = "Inserisci una password valida!";
            password.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(username.toString(), password.toString())
            .addOnCompleteListener(this){ task->
                if(task.isSuccessful){
                    database = FirebaseDatabase.getIstance().getReference("utenti");

                    database.child(username.toString()).get().addOnSuccessListener{




                    }



                }
                else{
                    Toast.makeText(baseContext, "Autenticazione fallita", Toast.LENGTH_SHORT).show();
                }
            }
        */
    }


    fun registrati(nome:String,cognome:String,tipo:Int,email:String,password:String){

        val utente = hashMapOf<String,Any?>(
            "nome" to nome,
            "cognome" to cognome,
            "tipo" to tipo,
            "email" to email,
            "password" to password
        )

        database.collection("utenti").document("prova").set(utente);

        val collectionUtenti = database.collection("utenti");

        collectionUtenti.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("stampa utente", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("stampa utente", "Error getting documents.", exception)
            }
    }





}
