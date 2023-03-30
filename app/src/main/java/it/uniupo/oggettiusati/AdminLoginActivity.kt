package it.uniupo.oggettiusati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class AdminLoginActivity : UserLoginActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        val extras = intent.extras

        userId = extras?.getString("userId").toString()

        lateinit var username : String

        val userRef = this.database.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if(document != null){
                username = document.get("nome").toString()
            } else {
                Log.w("document error","Error: document is null")
            }

            Toast.makeText(this, "Benvenuto ${username}!", Toast.LENGTH_LONG).show()
        }

        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //--- Deve poter eliminare utenti o sospenderli dalle attività ---
    private suspend fun eliminaUtente(userId: String){

        try {
            val myCollection = this.database.collection("users");

            val myDocument = myCollection.document(userId)

            myDocument.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ELIMINAZIONE UTENTE", "Documento eliminato con successo")

                    //Eliminazione del utente dal Authentication???

                } else {
                    Log.e("ELIMINAZIONE UTENTE", "Errore durante l'eliminazione del documento", task.exception)
                }
            }.await()


            myDocument.delete().await()
        }catch (e: Exception){
            Log.e("ERRORE ELIMINA UTENTE","Durante l'eliminazione del utente c'é stato un errore!", e)
        }

    }

    private suspend fun sospendiUtente(userId: String){

        try {
            val myCollection = this.database.collection("users");

            val myDocument = myCollection.document(userId)

            myDocument.update("sospeso", true).await()
        }catch (e: Exception){
            Log.e("ERRORE SOSPENDI UTENTE","Durante la sospensione del utente c'é stato un errore!", e)
        }
    }

    //--- Fine eliminazione e sospensione utente ---




}
