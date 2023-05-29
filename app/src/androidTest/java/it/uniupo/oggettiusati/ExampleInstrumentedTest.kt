package it.uniupo.oggettiusati

import android.location.Location
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.HomeFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.junit.*

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.ArrayList

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

/*
    @Test fun riempi(): Unit = runBlocking{

        val geoPosition = Location("provider")
        geoPosition.altitude = 37.4220
        geoPosition.longitude = -122.0841

        val newAnnuncio1 = Annuncio(
            "alan.turing",
            "Mr Robot: Season 1 Blu-Ray + Digital HD",
            "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
            16.99,
            2,
            true,
            "filmETv/serieTv",
            geoPosition
        )

        newAnnuncio1.salvaAnnuncioSuFirebase()

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                //activity.userId = "rbwh8rCBGmV6lv4Kum3eLcTeJFl1"

                val myElementoPreferito1 = activity.inserisciAnnuncioPreferitoFirebaseFirestore(
                    "rbwh8rCBGmV6lv4Kum3eLcTeJFl1",
                    "DxXnVuNyWXKmT3gcAYq4"
                )
            }
        }
    }
*/

    lateinit var myAnnunci : ArrayList <Annuncio>
    val database = Firebase.firestore

    private lateinit var scenarioCartFragment: FragmentScenario<CartFragment>
    private lateinit var scenarioHomeFragment: FragmentScenario<HomeFragment>

    @Before fun testSalvaAnnunciUtentiFirebaseFirestoreInizializzaFragment(): Unit = runBlocking{

        myAnnunci = ArrayList<Annuncio>()

        val scenarioSignUpActivity = ActivityScenario.launch(SignUpActivity::class.java)

        scenarioSignUpActivity.onActivity { activity ->
            runBlocking {
                activity.salvaUtenteSuFirebaseFirestore(
                    "alan.turing",
                    "Alan",
                    "Turing",
                    "23/06/1912",
                    "3358924674"
                )

                activity.salvaUtenteSuFirebaseFirestore(
                    "ada.loelace",
                    "Ada",
                    "Lovelace",
                    "10/12/1815",
                    "0212345671"
                )

                activity.salvaUtenteSuFirebaseFirestore(
                    "tim.bernerslee",
                    "Tim",
                    "Berners-Lee",
                    "08/06/1955",
                    "3358924574"
                )
            }
        }

        val geoPosition = Location("provider")
        geoPosition.altitude = 37.4220
        geoPosition.longitude = -122.0841

        val calendarInizioVenditaAnnuncio1 = Calendar.getInstance()
        calendarInizioVenditaAnnuncio1.add(Calendar.DAY_OF_YEAR, -5)
        val timeStampInizioVenditaAnnuncio1 = calendarInizioVenditaAnnuncio1.timeInMillis

        val calendarFineVenditaAnnuncio1 = Calendar.getInstance()
        calendarFineVenditaAnnuncio1.add(Calendar.DAY_OF_YEAR, -2)
        val timeStampFineVenditaAnnuncio1 = calendarFineVenditaAnnuncio1.timeInMillis

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Mr Robot: Season 1 Blu-Ray + Digital HD",
            "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
            16.99,
            2,
            true,
            "filmETv/serieTv",
            geoPosition,
            timeStampInizioVenditaAnnuncio1,
            timeStampFineVenditaAnnuncio1,
            "alan.turing"
        ))

        myAnnunci.add(Annuncio(
            "alan.turing",
            "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
            "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
            1100.00,
            1,
            false,
            "elettronica/smartphone",
            geoPosition
        ))

        val calendarInizioVenditaAnnuncio3 = Calendar.getInstance()
        calendarInizioVenditaAnnuncio3.add(Calendar.DAY_OF_YEAR, -9)
        val timeStampInizioVenditaAnnuncio3 = calendarInizioVenditaAnnuncio3.timeInMillis

        val calendarFineVenditaAnnuncio3 = Calendar.getInstance()
        calendarFineVenditaAnnuncio3.add(Calendar.DAY_OF_YEAR, -6)
        val timeStampFineVenditaAnnuncio3 = calendarFineVenditaAnnuncio3.timeInMillis

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Vintage Leather Messenger Bag",
            "This vintage-inspired leather messenger bag is perfect for carrying your laptop and everyday essentials. With a spacious main compartment, multiple pockets, and an adjustable strap, it's both stylish and functional.",
            79.99,
            3,
            true,
            "informatica/accessori",
            geoPosition,
            timeStampInizioVenditaAnnuncio3,
            timeStampFineVenditaAnnuncio3,
            "alan.turing"
        ))

        val calendarInizioVenditaAnnuncio4 = Calendar.getInstance()
        calendarInizioVenditaAnnuncio4.add(Calendar.DAY_OF_YEAR, -10)
        val timeStampInizioVenditaAnnuncio4 = calendarInizioVenditaAnnuncio4.timeInMillis

        val calendarFineVenditaAnnuncio4 = Calendar.getInstance()
        calendarFineVenditaAnnuncio4.add(Calendar.DAY_OF_YEAR, -4)
        val timeStampFineVenditaAnnuncio4 = calendarFineVenditaAnnuncio4.timeInMillis


        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Apple Watch Series 7 45mm GPS + Cellular",
            "The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.",
            499.00,
            1,
            true,
            "wearable",
            geoPosition,
            timeStampInizioVenditaAnnuncio4,
            timeStampFineVenditaAnnuncio4,
            "alan.turing"
        ))

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Samsung Galaxy S22 Ultra",
            "The Samsung Galaxy S22 Ultra is the ultimate smartphone, with a stunning 6.8-inch dynamic AMOLED display, 5G connectivity, and a powerful Snapdragon 898 processor. Capture stunning photos and videos with the quad-camera setup and enjoy all-day battery life.",
            1199.00,
            1,
            true,
            "electronics",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "alan.turing",
            "Canon EOS R5 Mirrorless Camera",
            "The Canon EOS R5 is a professional-grade mirrorless camera with a 45 megapixel full-frame sensor, 8K video capabilities, and in-body image stabilization. Capture stunning photos and videos in any lighting conditions with fast autofocus and advanced shooting modes.",
            3899.00,
            1,
            true,
            "electronics",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "alan.turing",
            "Peloton Bike+",
            "The Peloton Bike+ is the ultimate indoor cycling experience, with a 24-inch touchscreen display, live and on-demand classes, and a library of thousands of workouts. Get personalized coaching and metrics to help you reach your fitness goals.",
            2495.00,
            1,
            true,
            "fitness",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "alan.turing",
            "Sony WH-1000XM4 Wireless Noise Cancelling Headphones",
            "The Sony WH-1000XM4 headphones are the ultimate wireless listening experience, with industry-leading noise cancellation, Bluetooth connectivity, and up to 30 hours of battery life. Get immersive sound and customizable touch controls for a personalized listening experience.",
            349.99,
            1,
            true,
            "electronics",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Peloton Tread+",
            "The Peloton Tread+ is the ultimate home treadmill, with a 32-inch touchscreen display, live and on-demand classes, and a library of thousands of workouts. Get personalized coaching and metrics to help you reach your fitness goals.",
            4295.00,
            1,
            true,
            "fitness",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Nintendo Switch OLED Model",
            "The Nintendo Switch OLED Model is the ultimate gaming console, with a vibrant 7-inch OLED screen, enhanced audio, and up to 9 hours of battery life. Play your favorite games at home or on-the-go with detachable Joy-Con controllers.",
            349.99,
            1,
            true,
            "gaming",
            geoPosition
        ))

        myAnnunci.add(Annuncio(
            "ada.loelace",
            "Peloton Bike Bootcamp",
            "The Peloton Bike Bootcamp is the ultimate fitness experience, combining indoor cycling and strength training in one workout. Get personalized coaching and metrics to help you reach your fitness goals with live and on-demand classes.",
            2495.00,
            1,
            true,
            "fitness",
            geoPosition
        ))

        for(myAnnuncio in myAnnunci)
            myAnnuncio.salvaAnnuncioSuFirebase(null)

        scenarioCartFragment = launchFragmentInContainer()
        scenarioHomeFragment = launchFragmentInContainer()

    }
/*
    @After fun testEliminaAnnunciUtentiFirebaseFirestore():Unit = runBlocking{

        for(myAnnuncio in myAnnunci)
            myAnnuncio.eliminaAnnuncioDaFirebase()

        val myCollectionUtente = database.collection("utente")

        myCollectionUtente.document("ada.loelace").delete().await()
        myCollectionUtente.document("alan.turing").delete().await()
        myCollectionUtente.document("tim.bernerslee").delete().await()
    }

 */
    /*

    @Test fun testRicercaAnnuncioFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollectionUtente = database.collection("utente");

                val myDocumento = myCollectionUtente.document("alan.turing")

                val myCollectionRicerca = myDocumento.collection("ricerca")

                val primaInserimentoNumeroRicerche = myCollectionRicerca.get().await().size()

                val idParametriRicerca = activity.inserisciRicercaSuFirebaseFirestore("alan.turing", null, true, null, 150)

                assertEquals(primaInserimentoNumeroRicerche+1, myCollectionRicerca.get().await().size())

                val a = Annuncio(
                    "ada.loelace",
                    "Smartwatch Xiaomi Mi Band 6",
                    "Vendo smartwatch Xiaomi Mi Band 6, nuovo e ancora nella confezione originale. Monitoraggio della salute, notifiche, controllo della musica e altro ancora. Compatibile con Android e iOS.",
                    99.0,
                    3,
                    true,
                    "wearable"
                )

                a.salvaAnnuncioSuFirebase(null)
                myAnnunci.add(a)

                assertTrue(activity.controllaStatoRicercheAnnunci("alan.turing"))

                val b = Annuncio(
                    "ada.loelace",
                    "MacBook Pro 15 pollici",
                    "Vendo MacBook Pro 15 pollici, usato ma in perfette condizioni. Processore Intel Core i7, 16 GB di RAM, 512 GB di storage SSD. Inclusi caricabatterie e custodia in pelle.",
                    1700.0,
                    2,
                    true,
                    "electronics"
                )

                b.salvaAnnuncioSuFirebase(null)
                myAnnunci.add(b)

                assertFalse(activity.controllaStatoRicercheAnnunci("alan.turing"))

                activity.eliminaRicercaFirebaseFirestore("alan.turing",idParametriRicerca)
            }
        }
    }

    @Test fun testRecuperaAnnunciCarrelloFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {
                val myElementoNelCarrello2 = activity.inserisciAnnuncioCarrelloFirebaseFirestore("ada.loelace", myAnnunci.get(1).getAnnuncioId())
                val myElementoNelCarrello1 = activity.inserisciAnnuncioCarrelloFirebaseFirestore("ada.loelace", myAnnunci.get(0).getAnnuncioId())

                val myHashMapAda = activity.recuperaAnnunciCarrelloFirebaseFirestore("ada.loelace")

                assertEquals("Annuncio(userId='ada.loelace', titolo='Mr Robot: Season 1 Blu-Ray + Digital HD', descrizione='Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.', prezzo=16.99, stato=2, disponibilitaSpedire=true, categoria='filmETv/serieTv', posizione=Location[provider 0.000000,-122.084100 et=0], getgetAnnuncioId()()='${myAnnunci.get(0).getAnnuncioId()}')",myHashMapAda[myAnnunci.get(0).getAnnuncioId()].toString())
                assertEquals("Annuncio(userId='alan.turing', titolo='Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myHashMapAda[myAnnunci.get(1).getAnnuncioId()].toString())

                assertEquals(0,activity.recuperaAnnunciCarrelloFirebaseFirestore("alan.turing").size)

                activity.eliminaAnnuncioCarrelloFirebaseFirestore("ada.loelace", myElementoNelCarrello1)
                activity.eliminaAnnuncioCarrelloFirebaseFirestore("ada.loelace", myElementoNelCarrello2)
            }
        }
    }

    @Test fun testRecuperaAnnunciPreferitoFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myElementoPreferito1 = activity.inserisciAnnuncioPreferitoFirebaseFirestore("ada.loelace", myAnnunci.get(1).getAnnuncioId())
                val myElementoPreferito2 = activity.inserisciAnnuncioPreferitoFirebaseFirestore("ada.loelace", myAnnunci.get(0).getAnnuncioId())

                val myHashMapAda = activity.recuperaAnnunciPreferitiFirebaseFirestore("ada.loelace")

                assertEquals("Annuncio(userId='ada.loelace', titolo='Mr Robot: Season 1 Blu-Ray + Digital HD', descrizione='Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.', prezzo=16.99, stato=2, disponibilitaSpedire=true, categoria='filmETv/serieTv', posizione=Location[provider 0.000000,-122.084100 et=0], getgetAnnuncioId()()='${myAnnunci.get(0).getAnnuncioId()}')",
                    myHashMapAda!![myAnnunci.get(0).getAnnuncioId()].toString())
                assertEquals("Annuncio(userId='alan.turing', titolo='Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",
                    myHashMapAda!![myAnnunci.get(1).getAnnuncioId()]!!.toString())

                assertNull(activity.recuperaAnnunciPreferitiFirebaseFirestore("alan.turing"))

                activity.eliminaAnnuncioPreferitoFirebaseFirestore("ada.loelace", myElementoPreferito1)
                activity.eliminaAnnuncioPreferitoFirebaseFirestore("ada.loelace", myElementoPreferito2)

                val myCollection = database.collection("utente")

                myCollection.document("ada.loelace").delete().await()
                myCollection.document("alan.turing").delete().await()
            }
        }
    }

    @Test fun testInserisciEliminaAnnuncioPreferitoFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollection = database.collection("utente")

                val myDocument = myCollection.document("ada.lovelace")

                val myPreferiti = myDocument.collection("preferito")

                assertEquals(0, myPreferiti.get().await().size())

                val myElementoPreferito1 = activity.inserisciAnnuncioPreferitoFirebaseFirestore("ada.lovelace", myAnnunci.get(0).getAnnuncioId())
                val myElementoPreferito2 = activity.inserisciAnnuncioPreferitoFirebaseFirestore("ada.lovelace", myAnnunci.get(1).getAnnuncioId())

                assertEquals(2, myPreferiti.get().await().size())

                activity.eliminaAnnuncioPreferitoFirebaseFirestore("ada.lovelace", myElementoPreferito1)
                activity.eliminaAnnuncioPreferitoFirebaseFirestore("ada.lovelace", myElementoPreferito2)

                assertEquals(0, myPreferiti.get().await().size())
            }
        }
    }

    @Test fun testInserisciEliminaAnnuncioCarrelloFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollection = database.collection("utente")

                val myDocument = myCollection.document("ada.lovelace")

                val myCarrello = myDocument.collection("carrello")

                assertEquals(0, myCarrello.get().await().size())

                val myElementoNelCarrello1 = activity.inserisciAnnuncioCarrelloFirebaseFirestore("ada.lovelace", myAnnunci.get(0).getAnnuncioId())
                val myElementoNelCarrello2 = activity.inserisciAnnuncioCarrelloFirebaseFirestore("ada.lovelace", myAnnunci.get(1).getAnnuncioId())

                assertEquals(2, myCarrello.get().await().size())

                activity.eliminaAnnuncioCarrelloFirebaseFirestore("ada.lovelace", myElementoNelCarrello1)
                activity.eliminaAnnuncioCarrelloFirebaseFirestore("ada.lovelace", myElementoNelCarrello2)

                assertEquals(0, myCarrello.get().await().size())
            }
        }
    }

    @Test fun testIsAcquistabileProdotto() {

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {
                //Effettuo una ricarica di 100€
                val idTransazioneAlan = activity.salvaTransazioneSuFirestoreFirebase("alan.turing", 100.00, true)

                assertEquals(true, activity.isAcquistabile("alan.turing", 15.0))
                assertEquals(true, activity.isAcquistabile("alan.turing", 100.0))
                assertEquals(false, activity.isAcquistabile("alan.turing", 110.0))
                assertEquals(false, activity.isAcquistabile("alan.turing", 100.1))

              database.collection("utente").document("alan.turing").collection("transazione").document(idTransazioneAlan).delete().await()
            }
        }
    }

    //--- Inizio test, sulla funzione che mi inserisce le transazioni (ricariche/acquisti) su Firebase Firestore ---
    @Test fun testSalvaTransazioneSuFirestoreFirebase() {

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val idTransazioniAlan = ArrayList<String>()

                //Effettuo una ricarica di 100€
                idTransazioniAlan.add(activity.salvaTransazioneSuFirestoreFirebase("alan.turing", 100.00, true))

                val myCollection = database.collection("utente")

                val myCollectionTransazioneAlan =
                    myCollection.document("alan.turing").collection("transazione")

                //Il delta, terzo parametro, mi permette di specificare un errore, che posso accettare per considerare il test valido,
                //Se: valore calcolato - valore atteso < 0.1 il test viene considerato superato, sennó viene sollevata un eccezione
                assertEquals(100.0, activity.saldoAccount(myCollectionTransazioneAlan), 0.1)

                idTransazioniAlan.add(activity.salvaTransazioneSuFirestoreFirebase("alan.turing", 50.0, false))

                assertEquals(50.0, activity.saldoAccount(myCollectionTransazioneAlan), 0.1)

                //--- Elimina le transazioni utilizzate per i test ---
                for(myTransazione in idTransazioniAlan)
                    myCollectionTransazioneAlan.document(myTransazione).delete().await()
            }
        }
    }

    //--- Inizio test sulla funzione che mi ritorna gli utenti, all'interno di una HashMap, con il punteggio, delle recensioni, più alto ---
    @Test fun testClassificaUtentiRecensitiConVotoPiuAlto() {


        val myCollectionUtente = database.collection("utente")

        val myCollectionRecensioneAda = myCollectionUtente.document("ada.loelace").collection("recensione")
        val myCollectionRecensioneAlan = myCollectionUtente.document("alan.turing").collection("recensione")

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
                runBlocking {

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI", activity.inserisciRecensioneSuFirebaseFirestore("Ottimo prodotto", "Ho acquistato questo prodotto e sono rimasto molto soddisfatto. La qualità è eccellente e il prezzo è competitivo. Inoltre, la spedizione è stata rapida e il servizio clienti è stato molto disponibile. Consiglio vivamente questo prodotto!", 5, "ada.lovelace")!!.toString())

                    val idRecensioniAlan = ArrayList<String>()
                    idRecensioniAlan.add(activity.inserisciRecensioneSuFirebaseFirestore("Innovativo", "Questo prodotto è un vero e proprio gioiello di tecnologia. Molto innovativo e funzionale. Il prezzo è elevato, ma ne vale la pena. La spedizione è stata rapida e il servizio clienti è stato impeccabile.", 5, "alan.turing")!!)
                    idRecensioniAlan.add(activity.inserisciRecensioneSuFirebaseFirestore("Non all'altezza delle aspettative", "Mi aspettavo di più da questo prodotto, soprattutto considerando il prezzo elevato. La qualità non è eccezionale e alcuni componenti sembrano fragili. La spedizione è stata abbastanza veloce.", 2, "alan.turing")!!)
                    idRecensioniAlan.add(activity.inserisciRecensioneSuFirebaseFirestore("Buon rapporto qualità-prezzo", "Il prezzo di questo prodotto è conveniente e la qualità è buona. La spedizione è stata veloce e il venditore si è dimostrato disponibile nel rispondere alle mie domande. Consiglio questo prodotto.", 4, "alan.turing")!!)
                    idRecensioniAlan.add(activity.inserisciRecensioneSuFirebaseFirestore("Non soddisfacente", "Purtroppo questo prodotto non ha soddisfatto le mie aspettative. La qualità non è eccezionale e alcuni componenti sembrano fragili. La spedizione è stata abbastanza veloce, ma il servizio clienti non è stato molto disponibile.", 2, "alan.turing")!!)

                    val idRecensioniAda = ArrayList<String>()
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Ottimo prodotto", "Ho acquistato questo prodotto e sono rimasto molto soddisfatto. La qualità è eccellente e il prezzo è competitivo. Inoltre, la spedizione è stata rapida e il servizio clienti è stato molto disponibile. Consiglio vivamente questo prodotto!", 5, "ada.loelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Imballo non adeguato", "Il prodotto è arrivato danneggiato a causa di un'imballo insufficiente. Il venditore si è dimostrato disponibile nel risolvere il problema, ma avrei preferito ricevere il prodotto in perfette condizioni fin dall'inizio.", 2, "ada.loelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Prodotto discreto", "Il prodotto ha un prezzo conveniente, ma la qualità non è eccezionale. Adatto per un utilizzo occasionale, ma non lo consiglio per un uso intenso. La spedizione è stata abbastanza rapida.", 3, "ada.loelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Non consigliato", "Ho avuto diversi problemi con questo prodotto e il venditore non si è dimostrato disponibile nel risolverli. Sconsiglio l'acquisto di questo prodotto.", 1, "ada.loelace")!!)

                    val myHashMediaRecensioni = activity.classificaUtentiRecensitiConVotoPiuAlto()

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI", myHashMediaRecensioni["ada.lovelace"].toString() )

                    assertEquals(2.75,myHashMediaRecensioni["ada.loelace"]!!,0.1)
                    assertEquals(3.25,myHashMediaRecensioni["alan.turing"]!!,0.1)
                    assertEquals(0.0,myHashMediaRecensioni["tim.bernerslee"]!!,0.1)

                    assertEquals("{alan.turing=3.25, ada.loelace=2.75, tim.bernerslee=0.0}", myHashMediaRecensioni.toString())

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI", idRecensione1Ada)

                    //--- Elimina recensioni fatte su Ada ---
                    for(idRecensioneAda in idRecensioniAda)
                        myCollectionRecensioneAda.document(idRecensioneAda).delete().await()

                    //--- Elimina recensioni fatte su Alan ---
                    for(idRecensioneAlan in idRecensioniAlan)
                        myCollectionRecensioneAlan.document(idRecensioneAlan).delete().await()
                }
            }

        //Log.d("TEST CLASSIFICA UTENTI RECENSITI","Prima 2")
    }

    //--- Inizio test sulla funzione che mi inserisci un documento per ogni utente ---
    @Test fun testInserisciUtenteFirebaseFirestore(): Unit = runBlocking {

        val myCollection = database.collection("utente")

        val primaInserimento = myCollection.get().await().size()

        val scenarioSignUpActivity = ActivityScenario.launch(SignUpActivity::class.java)

        scenarioSignUpActivity.onActivity { activity ->
            runBlocking {
                activity.salvaUtenteSuFirebaseFirestore(
                    "utenteDiProva",
                    "Giuseppe",
                    "Marconi",
                    "10/02/1980",
                    "3358924674"
                )

                assertEquals(primaInserimento+1, myCollection.get().await().size())
            }
        }

        //Dopo il test, elimino il documento associato al utente.
        myCollection.document("utenteDiProva").delete().await()
    }\
    */


/*
    //--- Inizio test sulla funzione che mi inserisce una recensione a un utente: creato, recensito e eliminato ---
    @Test fun testInserisciRecensioneUtenteFirebaseFirestore(){

        scenarioCartFragment.onFragment { fragment ->
            //scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {
                val myDocumentUtente = database.collection("utente").document("alan.turing")

                val myCollectionRecensione = myDocumentUtente.collection("recensione")

                val numeroDocsRecensioni = myCollectionRecensione.get().await().size()

                //val idRecensione = activity.inserisciRecensioneSuFirebaseFirestore("Fantastico prodotto! Consigliatissimo!", "Ho acquistato questo prodotto e sono rimasto estremamente soddisfatto. La qualità è eccellente e corrisponde perfettamente alla descrizione fornita dal venditore. Inoltre, il prezzo è competitivo rispetto ad altri prodotti simili sul mercato. La spedizione è stata rapida e il servizio clienti è stato disponibile e cortese nel rispondere alle mie domande. Consiglio vivamente questo prodotto a chiunque sia alla ricerca di un'ottima soluzione per le proprie esigenze.",5,"alan.turing")

                val idRecensione = fragment.inserisciRecensioneSuFirebaseFirestore(
                    "Fantastico prodotto! Consigliatissimo!",
                    "Ho acquistato questo prodotto e sono rimasto estremamente soddisfatto. La qualità è eccellente e corrisponde perfettamente alla descrizione fornita dal venditore. Inoltre, il prezzo è competitivo rispetto ad altri prodotti simili sul mercato. La spedizione è stata rapida e il servizio clienti è stato disponibile e cortese nel rispondere alle mie domande. Consiglio vivamente questo prodotto a chiunque sia alla ricerca di un'ottima soluzione per le proprie esigenze.",
                    5,
                    "alan.turing"
                )

                assertEquals(
                    numeroDocsRecensioni + 1,
                    database.collection("utente").document("alan.turing").collection("recensione")
                        .get().await().size()
                )

                val idRecensioneVotoNonValido = fragment.inserisciRecensioneSuFirebaseFirestore(
                    "Fantastico prodotto! Consigliatissimo!",
                    "Ho acquistato questo prodotto e sono rimasto estremamente soddisfatto. La qualità è eccellente e corrisponde perfettamente alla descrizione fornita dal venditore. Inoltre, il prezzo è competitivo rispetto ad altri prodotti simili sul mercato. La spedizione è stata rapida e il servizio clienti è stato disponibile e cortese nel rispondere alle mie domande. Consiglio vivamente questo prodotto a chiunque sia alla ricerca di un'ottima soluzione per le proprie esigenze.",
                    6,
                    "alan.turing"
                )

                assertEquals(null, idRecensioneVotoNonValido)

                if (idRecensione != null)
                    //Elimino il documento, che ho appena inserito
                    myCollectionRecensione.document(idRecensione).delete().await()
            }
        }
    }
*/
    /*                                              !--- Attenzione ---!
    --- Nel caso in cui io ho un parametro di ricarca, es. prezzo superiore, il metodo non mi ritorna HashMap giusto per la pagina 2,
    numeri di elementi ritornati sbagliati ---
    */
/*
    @Test fun testRecuperaAnnunciNonVendutiPerMostrarliNellaHome(): Unit = runBlocking {

        val a = Annuncio(
            "francesca.neri",
            "ASUS ZenBook 14",
            "Vendo ASUS ZenBook 14 UX425EA-BM033T, nuovo e ancora nella confezione originale. Processore Intel Core i5-1135G7, 8 GB di RAM, 512 GB di SSD, schermo Full HD da 14 pollici. Design ultra-sottile e leggero. Includo la garanzia di 2 anni.",
            799.0,
            3,
            true,
            "computer"
        )

        a.salvaAnnuncioSuFirebase(null)
        myAnnunci.add(a)

        val b = Annuncio(
            "maria.rossi",
            "Samsung Galaxy S21 Ultra",
            "Vendo Samsung Galaxy S21 Ultra, come nuovo, usato solo per un mese. Colore Phantom Black, 256 GB di storage, 12 GB di RAM. Fotocamera posteriore quadrupla da 108 MP. Display Dynamic AMOLED 2X da 6.8 pollici. Includo la scatola originale, la garanzia e una custodia in omaggio.",
            950.0,
            3,
            true,
            "smartphone"
        )

        b.salvaAnnuncioSuFirebase(null)
        myAnnunci.add(b)

        val c = Annuncio(
            "giovanni.bianchi",
            "Dell XPS 13",
            "Vendo Dell XPS 13 9310, come nuovo, usato solo per qualche settimana. Processore Intel Core i7-1185G7, 16 GB di RAM, 512 GB di SSD, schermo InfinityEdge da 13.4 pollici. Design ultra-sottile e leggero. Includo la garanzia di 2 anni.",
            1399.0,
            3,
            true,
            "computer"
        )

        c.salvaAnnuncioSuFirebase(null)
        myAnnunci.add(c)

        //val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        //--- Fine Inserimento dati su Firestore Firebase ---
        //scenarioUserLoginActivity.onActivity { activity ->
        scenarioHomeFragment.onFragment { fragment ->
            runBlocking {

                //Nel caso in cui, inserissi un numero di pagina non valida mi ritorna null.
                assertNull(fragment.recuperaAnnunciPerMostrarliNellaHome(0))

                //Nel caso in cui, non andassi a invocare nessun metodo che mi filtra, mi recupera tutti gli annunci presenti. (MAX 10)
                assertEquals(10,fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                assertEquals(1,fragment.recuperaAnnunciPerMostrarliNellaHome(2)!!.size)

                fragment.recuperaAnnunciPrezzoInferiore(80)

                assertEquals(0,fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                fragment.recuperaTuttiAnnunci()

                fragment.recuperaAnnunciTitolo("Dell XPS 13")

                assertEquals(1, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                fragment.recuperaTuttiAnnunci()

                assertEquals(10, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                assertEquals(1, fragment.recuperaAnnunciPerMostrarliNellaHome(2)!!.size)

                fragment.recuperaAnnunciDisponibilitaSpedire(true)

                assertEquals(10, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                fragment.recuperaAnnunciPrezzoRange(18,1200)

                //Spedire + range.
                assertEquals(5, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                //prezzo superiore + spedire
                fragment.recuperaAnnunciPrezzoSuperiore(1200)

                assertEquals(5, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                //prezzo superiore + spedire
                fragment.recuperaAnnunciPrezzoInferiore(20)

                assertEquals(0, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                fragment.recuperaAnnunciPrezzoRange(10,5000)
                assertEquals(10, fragment.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                assertEquals(1, fragment.recuperaAnnunciPerMostrarliNellaHome(2)!!.size)
            }
        }
    }
*/
/*
    //--- Inizio test sulla funzione che mi recupera gli elementi con prezzo inferiore a X ---
    @Test fun testRecuperaAnnunciPerPrezzoInferioreNonVendutiFirebaseFirestore(){

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            //--- Fine Inserimento dati su Firestore Firebase ---
            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking {

                    //Log.d("TEST", activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.toString())

                    activity.recuperaAnnunciPrezzoInferiore(1200)
                    assertEquals(4, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoInferiore(20)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoInferiore(500)
                    assertEquals(2, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoInferiore(80)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoInferiore(499)
                    assertEquals(2, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoInferiore(15)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                }
            }
    }
*/
    /*
    //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo superiore a X ---
    @Test fun testRecuperaAnnunciPerPrezzoSuperioreNonVendutiFirebaseFirestore(){
            //--- Inserimento dati su Firestore Firebase ---
            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking {
                    activity.recuperaAnnunciPrezzoSuperiore(1200)
                    assertEquals(4, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoSuperiore(20)
                    assertEquals(8, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoSuperiore(500)
                    assertEquals(6, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoSuperiore(80)
                    assertEquals(8, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoSuperiore(499)
                    assertEquals(6, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoSuperiore(15)
                    assertEquals(8, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(2)!!.size)
                }
            }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo compreso tra un range:  max<X>min ---
    @Test fun testRecuperaAnnunciPerRangeNonVendutiFirebaseFirestore(){

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking {

                    activity.recuperaAnnunciPrezzoRange(450,1000)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(15,100)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(15,500)
                    assertEquals(2, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(499,1100)
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(498,1101)
                    assertEquals(1, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(15,1200)
                    assertEquals(4, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciPrezzoRange(2000,5000)
                    assertEquals(4, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                }
            }
    }

    //--- Inizio test sulla funzione che mi permette di recuperare tutti gli annunci che si trovano nel DB ---
    @Test fun testRecuperaTuttiAnnunciNonVendutiFirebaseFirestore(){

                //--- Fine Inserimento dati su Firestore Firebase ---
                val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

                scenarioUserLoginActivity.onActivity { activity ->
                    runBlocking{
                        activity.recuperaTuttiAnnunci()
                        assertEquals(8, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                        assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(2)!!.size)
                    }
                }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi che hanno quel titolo ---
    @Test fun testRecuperaAnnunciPerTitoloNonVendutiFirebaseFirestore(){

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking{

                    activity.recuperaAnnunciTitolo("Mr Robot: Season 1 Blu-Ray + Digital HD")
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciTitolo("Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked")
                    assertEquals(1, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciTitolo("Vintage Leather Messenger Bag")
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciTitolo("Apple Watch Series 7 45mm GPS + Cellular")
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciTitolo("Apple iPhone 11 Pro")
                    assertEquals(0, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                }
            }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi che hanno disponibilita a essere spediti ---
    @Test fun testRecuperaAnnunciPerDisponibilitaSpedireFirebaseFirestore(){

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking{
                    activity.recuperaAnnunciDisponibilitaSpedire(true)
                    assertEquals(7, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)

                    activity.recuperaAnnunciDisponibilitaSpedire(false)
                    assertEquals(1, activity.recuperaAnnunciPerMostrarliNellaHome(1)!!.size)
                }
            }
    }

    //--- Inizio test sulle funzioni che mi modificano gli annunci: titolo, descrizione, categoria e prezzo ---
    @Test fun testModificaAnnunciTitoloDescrizioneCategoriaPrezzoFirebaseFirestore(): Unit = runBlocking{

            assertEquals("Annuncio(userId='alan.turing', titolo='Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myAnnunci.get(1).toString())

            myAnnunci.get(1).setTitolo("Apple Watch Series 7 45mm GPS + Cellular")

            assertEquals("Annuncio(userId='alan.turing', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myAnnunci.get(1).toString())

            myAnnunci.get(1).setDescrizione("The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.")

            assertEquals("Annuncio(userId='alan.turing', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myAnnunci.get(1).toString())

            myAnnunci.get(1).setCategoria("wearable")

            assertEquals("Annuncio(userId='alan.turing', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='wearable', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myAnnunci.get(1).toString())

            myAnnunci.get(1).setPrezzo(499.0)

            assertEquals("Annuncio(userId='alan.turing', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=499.0, stato=1, disponibilitaSpedire=false, categoria='wearable', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], getgetAnnuncioId()()='${myAnnunci.get(1).getAnnuncioId()}')",myAnnunci.get(1).toString())
    }

    @Test fun testTempoMedioAcquistoPerUtente(){

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
            runBlocking {
                assertEquals(4.0, activity.calcolaTempoMedioAnnunciUtenteVenduto("ada.loelace")!!,0.1)
                assertNull(activity.calcolaTempoMedioAnnunciUtenteVenduto("alan.turing"))
            }
        }
    }

    @Test fun testNumeroOggettiVendita(){

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
            runBlocking {
                assertEquals(8, activity.numeroOggettiInVendita())
            }
        }
    }

    @Test fun testNumeroOggettiVenditaSpecificoUtente() {

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
            runBlocking {
                assertEquals(4, activity.numeroOggettiInVenditaPerSpecificoUtente("ada.loelace"))
                assertEquals(4, activity.numeroOggettiInVenditaPerSpecificoUtente("alan.turing"))
                assertEquals(0, activity.numeroOggettiInVenditaPerSpecificoUtente("tim.bernerslee"))
            }
        }
    }

    @Test fun testSospendiUtente(){

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollection = database.collection("utente")

                val myDocument = myCollection.document("alan.turing")

                assertFalse(myDocument.get().await().getBoolean("sospeso")!!)

                activity.sospendiUtente("alan.turing")

                assertTrue(myDocument.get().await().getBoolean("sospeso")!!)
            }
        }
    }

    @Test fun testRecuperaRicercheSalvateFirebaseFirestore(){

        val scenarioAdminLoginActivity = ActivityScenario.launch(AdminLoginActivity::class.java)

        scenarioAdminLoginActivity.onActivity { activity ->
            runBlocking {

            }
        }
    }

     */

    // --- Inizio funzione che testa il mantenimento delle informazioni aggiornate nel HashMap considerando DB ---
    // --> Non funziona ma HashMap è correttamente aggiornata, anche il DB <--
    //!-- Penso che sia per il fatto che addSnapshotListener lavora in "maniera asincrona" --!
/*
    @Test fun testSubscribeRealTimeDatabase(): Unit = runBlocking{

        val scenarioSignUpActivity = ActivityScenario.launch(SignUpActivity::class.java)

        scenarioSignUpActivity.onActivity { activity ->
            runBlocking {
                activity.salvaUtenteSuFirebaseFirestore(
                    "alan.turing",
                    "Alan",
                    "Turing",
                    "23/06/1912",
                    "3358924674"
                )

                activity.salvaUtenteSuFirebaseFirestore(
                    "ada.lovelace",
                    "Ada",
                    "Lovelace",
                    "10/12/1815",
                    "0212345671"
                )
            }
        }

            //--- Inserimento dati su Firestore Firebase ---
            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841

            val newAnnuncio1 = Annuncio(
                "ada.lovelace",
                "Mr Robot: Season 1 Blu-Ray + Digital HD",
                "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
                16.99,
                2,
                true,
                "filmETv/serieTv",
                geoPosition
            )

            val newAnnuncio2 = Annuncio(
                "ada.lovelace",
                "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                1100.00,
                1,
                false,
                "elettronica/smartphone",
                geoPosition
            )


            newAnnuncio1.salvaAnnuncioSuFirebase()
            newAnnuncio2.salvaAnnuncioSuFirebase()
            //--- Fine Inserimento dati su Firestore Firebase ---


        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                var myElementoPreferito1 = activity.inserisciAnnuncioPreferitoFirebaseFirestore(
                    "alan.turing",
                    newAnnuncio1.getAnnuncioId()
                )

                val query = activity.subscribeRealTimeDatabasePreferiti("alan.turing")

                //--- Metodo utilizzato per il mantenimento delle informazioni aggiornate ---
                if(query != null)
                    activity.subscribeRealTimeDatabase(query)

                val documentoRif =
                    database.collection(Annuncio.nomeCollection).document(newAnnuncio1.getAnnuncioId())

                Log.d("Prima update", newAnnuncio1.getAnnuncioId())

                runBlocking {
                    //simula una modifica effettuata da un altro client
                    documentoRif.update(
                        "titolo",
                        "Mr. Robot 2",
                        "descrizione",
                        "Second season of the critically acclaimed TV series about a cybersecurity engineer turned hacker."
                    ).await()
                }

                Thread.sleep(1000)

                assertEquals("", activity.myAnnunciPreferiti[newAnnuncio1.getAnnuncioId()].toString())

                activity.eliminaAnnuncioPreferitoFirebaseFirestore(
                    "ada.lovelace",
                    myElementoPreferito1
                )
            }
        }

        val myCollection = database.collection("utente")

        myCollection.document("ada.lovelace").delete().await()
        myCollection.document("alan.turing").delete().await()

        //--- Eliminazione dati su Firestore Firebase ---
        newAnnuncio1.eliminaAnnuncioDaFirebase()
        newAnnuncio2.eliminaAnnuncioDaFirebase()
        //--- Fine eliminazione dati su Firestore Firebase ---
    }
*/
    // --- Fine funzione che testa il mantenimento delle informazioni aggiornate nel HashMap considerando DB ---

    //--- Metodo di supporto, che mi serve per recupera il numero di documenti nella collezione annunci che sono salvati su FireStore ---
    @Ignore
    suspend fun getNumeroElementiFirestore(): Int {

        val myCollection = database.collection(Annuncio.nomeCollection)

        val myDocuments = myCollection.get().await()

        return myDocuments.size()
    }
    //--- Fine metodo di supporto ---

    //--- Metodo utilizzato per inserire un annuncio come preferito a un utente ---
    /*
    @Test fun inserisciAnnuncioPreferito(): Unit = runBlocking{

        val geoPosition = Location("provider")
        geoPosition.altitude = 37.4220
        geoPosition.longitude = -122.0841

        val newAnnuncio1 = Annuncio(
            "alan.turing",
            "Mr Robot: Season 1 Blu-Ray + Digital HD",
            "Mr. Robot, is a techno thriller that follows Elliot, a young programmer, who works as a cyber-security engineer by day and as a vigilante hacker by night.",
            16.99,
            2,
            true,
            "filmETv/serieTv",
            geoPosition
        )

        newAnnuncio1.salvaAnnuncioSuFirebase()

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {
                val myElementoPreferito1 = activity.inserisciAnnuncioPreferitoFirebaseFirestore(
                    "wTuCWaYaPVP59Oh0iG2El9BcEq22",
                    newAnnuncio1.getAnnuncioId()
                )
            }
        }
    }
*/
    //--- Fine metodo utilizzato per inserire un annuncio come preferito a un utente ---
}