package it.uniupo.oggettiusati

import android.location.Location
import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.junit.*

import org.junit.Assert.*
import org.junit.runner.RunWith

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

    //Ci permette di accedere al Activity: UserLoginActivity
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(UserLoginActivity::class.java)

    //--- Inizio test con la @Before e @After che non funzionano (il test @Before non é detto che inizi per primo e il @After non é detto che parti per ultimo) ---
    /*
    lateinit var newAnnuncio1: Annuncio
    lateinit var newAnnuncio2: Annuncio
    lateinit var newAnnuncio3: Annuncio
    lateinit var newAnnuncio4: Annuncio

    @Before fun testSalvaAnnunciFirebaseFirestore() {
        try {
                GlobalScope.launch (Dispatchers.IO) {

                val geoPosition = Location("provider")
                geoPosition.altitude = 37.4220
                geoPosition.longitude = -122.0841

                val newAnnuncio = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                val primaInserimento = getNumeroElementiFirestore()

                Log.d("INSERIMENTO ANNUNCIO TEST", "Prima inserimento: $primaInserimento")

                newAnnuncio.salvaAnnuncioSuFirebase()

                val dopoInserimento = getNumeroElementiFirestore()

                Log.d("INSERIMENTO ANNUNCIO TEST", "Dopo inserimento: $dopoInserimento")

                assertEquals(primaInserimento + 1, dopoInserimento)

                newAnnuncio.eliminaAnnuncioDaFirebase()

                val dopoEliminazione = getNumeroElementiFirestore()

                Log.d("INSERIMENTO ANNUNCIO TEST", "Dopo eliminazione: $dopoEliminazione")

                assertEquals(primaInserimento, dopoEliminazione)

                newAnnuncio1 = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                newAnnuncio2 = Annuncio(
                    "userIdTestProva",
                    "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                    "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                    1100.00,
                    1,
                    false,
                    "elettronica/smartphone",
                    geoPosition
                )

                newAnnuncio3 = Annuncio(
                    "userIdTestProva",
                    "Vintage Leather Messenger Bag",
                    "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                    79.99,
                    3,
                    true,
                    "informatica/accessori",
                    geoPosition
                )

                newAnnuncio4 = Annuncio(
                    "userIdTestProva",
                    "Apple Watch Series 7 45mm GPS + Cellular",
                    "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                    499.00,
                    1,
                    true,
                    "wearable",
                    geoPosition
                )

                newAnnuncio1.salvaAnnuncioSuFirebase()
                newAnnuncio2.salvaAnnuncioSuFirebase()
                newAnnuncio3.salvaAnnuncioSuFirebase()
                newAnnuncio4.salvaAnnuncioSuFirebase()

                assertEquals(primaInserimento+4,getNumeroElementiFirestore())
            }
        }catch (e: Exception){
            Log.e("INSERIMENTO ANNUNCIO TEST","Errore nei test",e)
        }
    }

    @Test fun testRecuperaAnnunciPerTitoloFirebaseFirestore() {
        // Ottieni la reference all'activity
        activityScenarioRule.scenario.onActivity { activity ->
            GlobalScope.launch (Dispatchers.IO) {

                assertEquals(1,activity.recuperaAnnunciTitolo("Mr Robot: Season 1 Blu-Ray + Digital HD").size)
                assertEquals(1,activity.recuperaAnnunciTitolo("Apple Watch Series 7 45mm GPS + Cellular").size)
                assertEquals(1,activity.recuperaAnnunciTitolo("Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked").size)
                assertEquals(1,activity.recuperaAnnunciTitolo("Vintage Leather Messenger Bag").size)

                //assertEquals(0,activity.recuperaAnnunciTitolo("Apple iPhone 11 Pro").size)
            }
        }
    }

    @Test
    fun testRecuperaTuttiAnnunciFirebaseFirestore() {
        // Ottieni la reference all'activity
        activityScenarioRule.scenario.onActivity { activity ->
            GlobalScope.launch (Dispatchers.IO) {
                assertEquals(getNumeroElementiFirestore(), activity.recuperaTuttiAnnunci().size)
            }
        }
    }

    @Test
    fun testRecuperaAnnunciPerPrezzoInferiore() {
        // Ottieni la reference all'activity
        activityScenarioRule.scenario.onActivity { activity ->
            GlobalScope.launch (Dispatchers.IO) {

                assertEquals(4,activity.recuperaAnnunciPrezzoInferiore(1200).size)
                assertEquals(1,activity.recuperaAnnunciPrezzoInferiore(20).size)
                assertEquals(3,activity.recuperaAnnunciPrezzoInferiore(500).size)
                assertEquals(2,activity.recuperaAnnunciPrezzoInferiore(80).size)
                assertEquals(2,activity.recuperaAnnunciPrezzoInferiore(499).size)
                assertEquals(0,activity.recuperaAnnunciPrezzoInferiore(15).size)

            }
        }
    }

    @After fun testEliminaAnnunciFirebaseFirestore() {

        GlobalScope.launch (Dispatchers.IO) {

            val primaEliminazione = getNumeroElementiFirestore()

            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()

            assertEquals(primaEliminazione-4, getNumeroElementiFirestore())
        }
    }
    */

    //--- Inizio test che non funzionano, neanche se vengono eseguiti uno alla volta (Non avviene eliminazione degli annunci) ---

    @Test
    fun avviaTestSuFirebaseFirestore() = runBlocking{

        //--- Inizio test sulla funzione che inserisci elementi nel DB ---
        testSalvaAnnunciFirebaseFirestore()
        //--- Finte test sulla funzione che inserisce elementi nel DB ---

        //--- Inizio test sulla funzione che mi permette di recuperare tutti gli annunci che si trovano nel DB ---
        testRecuperaTuttiAnnunciFirebaseFirestore()
        //--- Fine test sulla funzione che mi permette di recuperare tutti gli annunci che si trovano nel DB ---

        //--- Inizio test sulla funzione che mi recupera gli elementi con prezzo inferiore a X ---
        testRecuperaAnnunciPerPrezzoInferiore()
        //--- Fine test sulla funzione che mi recupera gli elementi con prezzo inferiore a X ---

        //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo superiore a X ---
        testRecuperaAnnunciPerPrezzoSuperiore()
        //--- Fine test sulla funzione che mi recupera gli elementi con un prezzo superiore a X ---

        //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo compreso tra un range:  max<X>min ---
        testRecuperaAnnunciPerRange()
        //--- Fine test sulla funzione che mi recupera gli elementi con un prezzo compreso tra un range:  max<X>min ---
    }

    @Ignore
    suspend fun testSalvaAnnunciFirebaseFirestore() {
        try {
                val primaInserimento = getNumeroElementiFirestore()

                //--- Inserimento dati su Firestore Firebase ---

                val geoPosition = Location("provider")
                geoPosition.altitude = 37.4220
                geoPosition.longitude = -122.0841

                val newAnnuncio1 = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                val newAnnuncio2 = Annuncio(
                    "userIdTestProva",
                    "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                    "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                    1100.00,
                    1,
                    false,
                    "elettronica/smartphone",
                    geoPosition
                )

                val newAnnuncio3 = Annuncio(
                    "userIdTestProva",
                    "Vintage Leather Messenger Bag",
                    "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                    79.99,
                    3,
                    true,
                    "informatica/accessori",
                    geoPosition
                )

                val newAnnuncio4 = Annuncio(
                    "userIdTestProva",
                    "Apple Watch Series 7 45mm GPS + Cellular",
                    "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                    499.00,
                    1,
                    true,
                    "wearable",
                    geoPosition
                )

                newAnnuncio1.salvaAnnuncioSuFirebase()
                newAnnuncio2.salvaAnnuncioSuFirebase()
                newAnnuncio3.salvaAnnuncioSuFirebase()
                newAnnuncio4.salvaAnnuncioSuFirebase()

                //--- Fine Inserimento dati su Firestore Firebase ---

                val dopoInserimento = getNumeroElementiFirestore()

                assertEquals(4 + primaInserimento, dopoInserimento)

                //--- Eliminazione dati su Firestore Firebase ---
                newAnnuncio1.eliminaAnnuncioDaFirebase()
                newAnnuncio2.eliminaAnnuncioDaFirebase()
                newAnnuncio3.eliminaAnnuncioDaFirebase()
                newAnnuncio4.eliminaAnnuncioDaFirebase()
                //--- Fine eliminazione dati su Firestore Firebase ---

        } catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }

    @Ignore
    suspend fun testRecuperaAnnunciPerPrezzoInferiore() {
        try {
            //--- Inserimento dati su Firestore Firebase ---
            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841

            val newAnnuncio1 = Annuncio(
                "userIdTestProva",
                "Mr Robot: Season 1 Blu-Ray + Digital HD",
                "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                16.99,
                2,
                true,
                "filmETv/serieTv",
                geoPosition
            )

            val newAnnuncio2 = Annuncio(
                "userIdTestProva",
                "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                1100.00,
                1,
                false,
                "elettronica/smartphone",
                geoPosition
            )

            val newAnnuncio3 = Annuncio(
                "userIdTestProva",
                "Vintage Leather Messenger Bag",
                "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                79.99,
                3,
                true,
                "informatica/accessori",
                geoPosition
            )

            val newAnnuncio4 = Annuncio(
                "userIdTestProva",
                "Apple Watch Series 7 45mm GPS + Cellular",
                "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                499.00,
                1,
                true,
                "wearable",
                geoPosition
            )

            newAnnuncio1.salvaAnnuncioSuFirebase()
            newAnnuncio2.salvaAnnuncioSuFirebase()
            newAnnuncio3.salvaAnnuncioSuFirebase()
            newAnnuncio4.salvaAnnuncioSuFirebase()

            //--- Fine Inserimento dati su Firestore Firebase ---
            activityScenarioRule.scenario.onActivity { activity ->
                runBlocking {
                    assertEquals(4, activity.recuperaAnnunciPrezzoInferiore(1200).size)
                    assertEquals(1, activity.recuperaAnnunciPrezzoInferiore(20).size)
                    assertEquals(3, activity.recuperaAnnunciPrezzoInferiore(500).size)
                    assertEquals(2, activity.recuperaAnnunciPrezzoInferiore(80).size)
                    assertEquals(2, activity.recuperaAnnunciPrezzoInferiore(499).size)
                    assertEquals(0, activity.recuperaAnnunciPrezzoInferiore(15).size)
                }
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---


        } catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }

    @Ignore
    suspend fun testRecuperaAnnunciPerPrezzoSuperiore() {
        try {
            //--- Inserimento dati su Firestore Firebase ---

            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841

            val newAnnuncio1 = Annuncio(
                "userIdTestProva",
                "Mr Robot: Season 1 Blu-Ray + Digital HD",
                "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                16.99,
                2,
                true,
                "filmETv/serieTv",
                geoPosition
            )

            val newAnnuncio2 = Annuncio(
                "userIdTestProva",
                "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                1100.00,
                1,
                false,
                "elettronica/smartphone",
                geoPosition
            )

            val newAnnuncio3 = Annuncio(
                "userIdTestProva",
                "Vintage Leather Messenger Bag",
                "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                79.99,
                3,
                true,
                "informatica/accessori",
                geoPosition
            )

            val newAnnuncio4 = Annuncio(
                "userIdTestProva",
                "Apple Watch Series 7 45mm GPS + Cellular",
                "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                499.00,
                1,
                true,
                "wearable",
                geoPosition
            )

            newAnnuncio1.salvaAnnuncioSuFirebase()
            newAnnuncio2.salvaAnnuncioSuFirebase()
            newAnnuncio3.salvaAnnuncioSuFirebase()
            newAnnuncio4.salvaAnnuncioSuFirebase()

            //--- Fine Inserimento dati su Firestore Firebase ---

            activityScenarioRule.scenario.onActivity { activity ->
                runBlocking {
                    assertEquals(0, activity.recuperaAnnunciPrezzoSuperiore(1200).size)
                    assertEquals(3, activity.recuperaAnnunciPrezzoSuperiore(20).size)
                    assertEquals(1, activity.recuperaAnnunciPrezzoSuperiore(500).size)
                    assertEquals(2, activity.recuperaAnnunciPrezzoSuperiore(80).size)
                    assertEquals(1, activity.recuperaAnnunciPrezzoSuperiore(499).size)
                    assertEquals(4, activity.recuperaAnnunciPrezzoSuperiore(15).size)
                }
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---

        } catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }

    @Ignore
    suspend fun testRecuperaAnnunciPerRange() {
        try {
            //--- Inserimento dati su Firestore Firebase ---

            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841

            val newAnnuncio1 = Annuncio(
                "userIdTestProva",
                "Mr Robot: Season 1 Blu-Ray + Digital HD",
                "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                16.99,
                2,
                true,
                "filmETv/serieTv",
                geoPosition
            )

            val newAnnuncio2 = Annuncio(
                "userIdTestProva",
                "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                1100.00,
                1,
                false,
                "elettronica/smartphone",
                geoPosition
            )

            val newAnnuncio3 = Annuncio(
                "userIdTestProva",
                "Vintage Leather Messenger Bag",
                "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                79.99,
                3,
                true,
                "informatica/accessori",
                geoPosition
            )

            val newAnnuncio4 = Annuncio(
                "userIdTestProva",
                "Apple Watch Series 7 45mm GPS + Cellular",
                "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                499.00,
                1,
                true,
                "wearable",
                geoPosition
            )

            newAnnuncio1.salvaAnnuncioSuFirebase()
            newAnnuncio2.salvaAnnuncioSuFirebase()
            newAnnuncio3.salvaAnnuncioSuFirebase()
            newAnnuncio4.salvaAnnuncioSuFirebase()

            //--- Fine Inserimento dati su Firestore Firebase ---

            activityScenarioRule.scenario.onActivity { activity ->
                runBlocking {
                    assertEquals(1, activity.recuperaAnnunciPrezzoRange(450,1000).size)
                    assertEquals(2, activity.recuperaAnnunciPrezzoRange(15,100).size)
                    assertEquals(1, activity.recuperaAnnunciPrezzoRange(15,20).size)
                    assertEquals(0, activity.recuperaAnnunciPrezzoRange(499,1100).size)
                    assertEquals(2, activity.recuperaAnnunciPrezzoRange(498,1101).size)
                    assertEquals(4, activity.recuperaAnnunciPrezzoRange(15,1200).size)
                }
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---

        } catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }

    @Ignore
    suspend fun testRecuperaTuttiAnnunciFirebaseFirestore() {
        try {
                val geoPosition = Location("provider")
                geoPosition.altitude = 37.4220
                geoPosition.longitude = -122.0841

                val newAnnuncio1 = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                val newAnnuncio2 = Annuncio(
                    "userIdTestProva",
                    "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                    "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                    1100.00,
                    1,
                    false,
                    "elettronica/smartphone",
                    geoPosition
                )

                val newAnnuncio3 = Annuncio(
                    "userIdTestProva",
                    "Vintage Leather Messenger Bag",
                    "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                    79.99,
                    3,
                    true,
                    "informatica/accessori",
                    geoPosition
                )

                val newAnnuncio4 = Annuncio(
                    "userIdTestProva",
                    "Apple Watch Series 7 45mm GPS + Cellular",
                    "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                    499.00,
                    1,
                    true,
                    "wearable",
                    geoPosition
                )

                newAnnuncio1.salvaAnnuncioSuFirebase()
                newAnnuncio2.salvaAnnuncioSuFirebase()
                newAnnuncio3.salvaAnnuncioSuFirebase()
                newAnnuncio4.salvaAnnuncioSuFirebase()

                //--- Fine Inserimento dati su Firestore Firebase ---

                val numeroAnnunci = getNumeroElementiFirestore()

                activityScenarioRule.scenario.onActivity { activity ->
                    runBlocking{
                        assertEquals(numeroAnnunci, activity.recuperaTuttiAnnunci().size)
                    }
                }

                //--- Eliminazione dati su Firestore Firebase ---
                newAnnuncio1.eliminaAnnuncioDaFirebase()
                newAnnuncio2.eliminaAnnuncioDaFirebase()
                newAnnuncio3.eliminaAnnuncioDaFirebase()
                newAnnuncio4.eliminaAnnuncioDaFirebase()
                //--- Fine eliminazione dati su Firestore Firebase ---
        }catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }




    //--- Fine test che funzionano, se vengono eseguiti uno alla volta ---

    //--- Metodo di supporto, che mi serve per recupera il numero di documenti nella collezione annunci che sono salvati su FireStore ---
    @Ignore
    suspend fun getNumeroElementiFirestore(): Int {

        val myCollection = Annuncio.database.collection("annunci")

        val myDocuments = myCollection.get().await()

        return myDocuments.size()
    }
    //--- Fine metodo di supporto ---

    /*
    @Test
    fun testModificaAnnunciFirebaseFirestore() {
        // Ottieni la reference all'activity
        activityScenarioRule.scenario.onActivity { activity ->

            GlobalScope.launch (Dispatchers.IO) {
                try {
                    val userId = "userIdTestProva"

                    val geoPosition = Location("provider")
                    geoPosition.altitude = 37.4220
                    geoPosition.longitude = -122.0841

                    val newAnnuncio = Annuncio(
                        userId,
                        "Mr Robot: Season 1 Blu-Ray + Digital HD",
                        "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                        16.99,
                        2,
                        true,
                        "filmETv/serieTv",
                        geoPosition
                    )

                    newAnnuncio.salvaAnnuncioSuFirebase()

                    //Log.d("MODIFICA ANNUNCIO TEST","Prima della modifica: $newAnnuncio")

                    assertEquals(1 , activity.recuperaAnnunciTitolo("Mr Robot: Season 1 Blu-Ray + Digital HD").size)
                    assertEquals(0 , activity.recuperaAnnunciTitolo("Mr Robot: Season 2 Blu-Ray + Digital HD").size)

                    assertEquals("Annuncio(userId='userIdTestProva', titolo='Mr Robot: Season 1 Blu-Ray + Digital HD', descrizione='Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.', prezzo=16.99, stato=2, disponibilitaSpedire=true, categoria='filmETv/serieTv', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio.annuncioId}')",newAnnuncio.toString())

                    newAnnuncio.setTitolo("Mr Robot: Season 2 Blu-Ray + Digital HD")

                    //Log.d("MODIFICA ANNUNCIO TEST","Dopo la modifica: $newAnnuncio")

                    assertEquals("Annuncio(userId='userIdTestProva', titolo='Mr Robot: Season 2 Blu-Ray + Digital HD', descrizione='Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.', prezzo=16.99, stato=2, disponibilitaSpedire=true, categoria='filmETv/serieTv', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio.annuncioId}')",newAnnuncio.toString())

                    assertEquals(0 , activity.recuperaAnnunciTitolo("Mr Robot: Season 1 Blu-Ray + Digital HD").size)
                    assertEquals(1 , activity.recuperaAnnunciTitolo("Mr Robot: Season 2 Blu-Ray + Digital HD").size)

                    newAnnuncio.eliminaAnnuncioDaFirebase()

                }catch (e: Exception){
                    Log.e("MODIFICA ANNUNCIO TEST","Errore nei test",e)
                }
            }
        }
    }


*/

    /*
    @Test
    fun testRecuperaAnnunciPerPrezzoInferiore() {
        try {
            GlobalScope.launch(Dispatchers.IO) {

                val primaInserimento = getNumeroElementiFirestore()

                //--- Inserimento dati su Firestore Firebase ---

                val geoPosition = Location("provider")
                geoPosition.altitude = 37.4220
                geoPosition.longitude = -122.0841

                val newAnnuncio1 = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                val newAnnuncio2 = Annuncio(
                    "userIdTestProva",
                    "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                    "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                    1100.00,
                    1,
                    false,
                    "elettronica/smartphone",
                    geoPosition
                )

                val newAnnuncio3 = Annuncio(
                    "userIdTestProva",
                    "Vintage Leather Messenger Bag",
                    "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                    79.99,
                    3,
                    true,
                    "informatica/accessori",
                    geoPosition
                )

                val newAnnuncio4 = Annuncio(
                    "userIdTestProva",
                    "Apple Watch Series 7 45mm GPS + Cellular",
                    "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                    499.00,
                    1,
                    true,
                    "wearable",
                    geoPosition
                )

                newAnnuncio1.salvaAnnuncioSuFirebase()
                newAnnuncio2.salvaAnnuncioSuFirebase()
                newAnnuncio3.salvaAnnuncioSuFirebase()
                newAnnuncio4.salvaAnnuncioSuFirebase()
            activityScenarioRule.scenario.onActivity { activity ->
                GlobalScope.launch(Dispatchers.IO) {



                    //--- Fine Inserimento dati su Firestore Firebase ---



                    //--- Eliminazione dati su Firestore Firebase ---
                    newAnnuncio1.eliminaAnnuncioDaFirebase()
                    newAnnuncio2.eliminaAnnuncioDaFirebase()
                    newAnnuncio3.eliminaAnnuncioDaFirebase()
                    newAnnuncio4.eliminaAnnuncioDaFirebase()
                    //--- Fine eliminazione dati su Firestore Firebase ---

                }
            }
        }catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }
*/

/*
    @Test
    fun testRecuperaTuttiAnnunciFirebaseFirestore() {
        try {
            activityScenarioRule.scenario.onActivity { activity ->
                GlobalScope.launch(Dispatchers.IO) {

                    val primaInserimento = getNumeroElementiFirestore()

                    //--- Inserimento dati su Firestore Firebase ---

                    val geoPosition = Location("provider")
                    geoPosition.altitude = 37.4220
                    geoPosition.longitude = -122.0841

                    val newAnnuncio1 = Annuncio(
                        "userIdTestProva",
                        "Mr Robot: Season 1 Blu-Ray + Digital HD",
                        "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                        16.99,
                        2,
                        true,
                        "filmETv/serieTv",
                        geoPosition
                    )

                    val newAnnuncio2 = Annuncio(
                        "userIdTestProva",
                        "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                        "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                        1100.00,
                        1,
                        false,
                        "elettronica/smartphone",
                        geoPosition
                    )

                    val newAnnuncio3 = Annuncio(
                        "userIdTestProva",
                        "Vintage Leather Messenger Bag",
                        "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                        79.99,
                        3,
                        true,
                        "informatica/accessori",
                        geoPosition
                    )

                    val newAnnuncio4 = Annuncio(
                        "userIdTestProva",
                        "Apple Watch Series 7 45mm GPS + Cellular",
                        "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                        499.00,
                        1,
                        true,
                        "wearable",
                        geoPosition
                    )

                    newAnnuncio1.salvaAnnuncioSuFirebase()
                    newAnnuncio2.salvaAnnuncioSuFirebase()
                    newAnnuncio3.salvaAnnuncioSuFirebase()
                    newAnnuncio4.salvaAnnuncioSuFirebase()

                    //--- Fine Inserimento dati su Firestore Firebase ---

                    assertEquals(getNumeroElementiFirestore(), activity.recuperaTuttiAnnunci().size)

                    //--- Eliminazione dati su Firestore Firebase ---
                    newAnnuncio1.eliminaAnnuncioDaFirebase()
                    newAnnuncio2.eliminaAnnuncioDaFirebase()
                    newAnnuncio3.eliminaAnnuncioDaFirebase()
                    newAnnuncio4.eliminaAnnuncioDaFirebase()
                    //--- Fine eliminazione dati su Firestore Firebase ---

                }
            }
        }catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }



    @Test
    fun testRecuperaTuttiAnnunciFirebaseFirestore() {
        try {
            GlobalScope.launch(Dispatchers.IO) {

                val primaInserimento = getNumeroElementiFirestore()

                //--- Inserimento dati su Firestore Firebase ---

                val geoPosition = Location("provider")
                geoPosition.altitude = 37.4220
                geoPosition.longitude = -122.0841

                val newAnnuncio1 = Annuncio(
                    "userIdTestProva",
                    "Mr Robot: Season 1 Blu-Ray + Digital HD",
                    "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                    16.99,
                    2,
                    true,
                    "filmETv/serieTv",
                    geoPosition
                )

                val newAnnuncio2 = Annuncio(
                    "userIdTestProva",
                    "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                    "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                    1100.00,
                    1,
                    false,
                    "elettronica/smartphone",
                    geoPosition
                )

                val newAnnuncio3 = Annuncio(
                    "userIdTestProva",
                    "Vintage Leather Messenger Bag",
                    "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
                    79.99,
                    3,
                    true,
                    "informatica/accessori",
                    geoPosition
                )

                val newAnnuncio4 = Annuncio(
                    "userIdTestProva",
                    "Apple Watch Series 7 45mm GPS + Cellular",
                    "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
                    499.00,
                    1,
                    true,
                    "wearable",
                    geoPosition
                )

                newAnnuncio1.salvaAnnuncioSuFirebase()
                newAnnuncio2.salvaAnnuncioSuFirebase()
                newAnnuncio3.salvaAnnuncioSuFirebase()
                newAnnuncio4.salvaAnnuncioSuFirebase()

                //--- Fine Inserimento dati su Firestore Firebase ---

                val numeroAnnunci = getNumeroElementiFirestore()

                activityScenarioRule.scenario.onActivity { activity ->
                    GlobalScope.launch(Dispatchers.IO) {
                        assertEquals(numeroAnnunci, activity.recuperaTuttiAnnunci().size)
                    }
                }

                //--- Eliminazione dati su Firestore Firebase ---
                newAnnuncio1.eliminaAnnuncioDaFirebase()
                newAnnuncio2.eliminaAnnuncioDaFirebase()
                newAnnuncio3.eliminaAnnuncioDaFirebase()
                newAnnuncio4.eliminaAnnuncioDaFirebase()
                //--- Fine eliminazione dati su Firestore Firebase ---
            }
        }catch (e: Exception) {
            Log.e("INSERIMENTO ANNUNCIO TEST", "Errore nei test", e)
        }
    }

 */
}