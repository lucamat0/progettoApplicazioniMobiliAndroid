package it.uniupo.oggettiusati

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.Annuncio.Companion.database
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Ignore

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("it.uniupo.oggettiusati", appContext.packageName)
    }

    @Test
    fun testSalvaAnnunciSuFirebase() {

        val userId = "oUu3I6zcCDh8pFaVHwfDQAnDDP53";

        val newAnnuncio = Annuncio(
            userId,
            "Mr Robot: Season 1 Blu-Ray + Digital HD",
            "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
            16.99,
            2,
            true,
            "filmETv/serieTv",
            GeoPoint(40.3440, 73.5938)
        )

        GlobalScope.launch (Dispatchers.IO) {

            val primaInserimento = getNumeroElementiFirestore()

            newAnnuncio.salvaAnnuncioSuFirebase()

            assertEquals(primaInserimento + 1, getNumeroElementiFirestore())

            newAnnuncio.eliminaAnnuncioDaFirebase()

            assertEquals(primaInserimento, getNumeroElementiFirestore())
        }
    }

    @Ignore
    //Metodo di supporto, che mi serve per recupera il numero di documenti nella collezione annunci che sono salvati su FireStore
    suspend fun getNumeroElementiFirestore(): Int{

        val myCollection = Annuncio.database.collection("annunci");

        val myDocuments = myCollection.get().await()

        return myDocuments.size()
    }
}