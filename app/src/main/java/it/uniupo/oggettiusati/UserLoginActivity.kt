package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserLoginActivity : AppCompatActivity()  {

    private lateinit var auth: FirebaseAuth
    private  lateinit var database: FirebaseFirestore

    //Indica id dell'utente loggato
    private var userId : String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_logged)

        auth = FirebaseAuth.getInstance();
        database = Firebase.firestore
        val extras = intent.extras

        //Da cambiare, bisogna ripescarlo!
        userId = extras?.getString("userId").toString();
        var username = ""

        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if(document != null){
                username = document.get("nome").toString()
            } else {
                Log.w("document error","Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        // -- Test funzionamento metodi nella classe annuncio --

        val newAnnuncio = Annuncio(userId, "Mr Robot: Season 1 Blu-Ray + Digital HD", "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.", 16.99, 2, true, "filmETv/serieTv");
        newAnnuncio.toString();
        newAnnuncio.salvaAnnuncioSuFirebase(database);


        val buttonModifica = findViewById<Button>(R.id.modifica)

        val buttonElimina = findViewById<Button>(R.id.elimina)

        buttonModifica.setOnClickListener{

            newAnnuncio.titolo = "Mr Robot: Season 2 Blu-Ray + Digital HD";

            newAnnuncio.modificaAnnuncioSuFirebase(database);
        }

        buttonElimina.setOnClickListener{
            newAnnuncio.eliminaAnnuncioDaFirebase(database);
        }

        // -- Fine Test funzionamento metodi nella classe annuncio --

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}