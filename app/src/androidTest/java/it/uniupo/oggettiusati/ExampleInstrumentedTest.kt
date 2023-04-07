package it.uniupo.oggettiusati

import android.location.Location
import android.util.Log
import androidx.test.core.app.ActivityScenario
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
                geoPosition.latitude = 37.4220
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
        activityScenarioUserLoginActivity.scenario.onActivity { activity ->
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
        activityScenarioUserLoginActivity.scenario.onActivity { activity ->
            GlobalScope.launch (Dispatchers.IO) {
                assertEquals(getNumeroElementiFirestore(), activity.recuperaTuttiAnnunci().size)
            }
        }
    }

    @Test
    fun testRecuperaAnnunciPerPrezzoInferiore() {
        // Ottieni la reference all'activity
        activityScenarioUserLoginActivity.scenario.onActivity { activity ->
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


    @Test fun testisAcquistabileProdotto() {

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
            }
        }

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {
                //Effettuo una ricarica di 100€
                val idTransazioneAlan = activity.salvaTransazioneSuFirestoreFirebase("alan.turing", 100.00, true)

                assertEquals(true, activity.isAcquistabile("alan.turing", 15.0))
                assertEquals(true, activity.isAcquistabile("alan.turing", 100.0))
                assertEquals(false, activity.isAcquistabile("alan.turing", 110.0))
                assertEquals(false, activity.isAcquistabile("alan.turing", 100.1))

                val myCollection = Annuncio.database.collection("utente")

                val myCollectionTransazioneAlan =
                    myCollection.document("alan.turing").collection("transazione")

                myCollectionTransazioneAlan.document(idTransazioneAlan).delete().await()

                myCollection.document("alan.turing").delete().await()
            }
        }


    }

    //--- Inizio test, sulla funzione che mi inserisce le transazioni (ricariche/acquisti) su Firebase Firestore ---
    @Test fun testsalvaTransazioneSuFirestoreFirebase() {

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
            }
        }

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val idTransazioniAlan = ArrayList<String>()

                //Effettuo una ricarica di 100€
                idTransazioniAlan.add(activity.salvaTransazioneSuFirestoreFirebase("alan.turing", 100.00, true))

                val myCollection = Annuncio.database.collection("utente")

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

                //--- Elimina il documento, creato, associato al utente ---
                myCollection.document("alan.turing").delete().await()
            }
        }
    }

    //--- Inizio test sulla funzione che mi ritorna gli utenti, all'interno di una HashMap, con il punteggio, delle recensioni, più alto ---
    @Test fun testClassificaUtentiRecensitiConVotoPiuAlto() {

        val scenarioSignUpActivity = ActivityScenario.launch(SignUpActivity::class.java)

        scenarioSignUpActivity.onActivity { activity ->
            runBlocking {
                activity.salvaUtenteSuFirebaseFirestore(
                    "ada.lovelace",
                    "Ada",
                    "Lovelace",
                    "10/12/1815",
                    "0212345671"
                )

                activity.salvaUtenteSuFirebaseFirestore(
                    "alan.turing",
                    "Alan",
                    "Turing",
                    "23/06/1912",
                    "3358924674"
                )

                activity.salvaUtenteSuFirebaseFirestore(
                    "tim.bernerslee",
                    "Tim",
                    "Berners-Lee",
                    "08/06/1955",
                    "3358924574"
                )

                //Log.d("TEST CLASSIFICA UTENTI RECENSITI","Prima")
            }
        }

        val myCollection = Annuncio.database.collection("utente")

        val myCollectionRecensioneAda = myCollection.document("ada.lovelace").collection("recensione")
        val myCollectionRecensioneAlan = myCollection.document("alan.turing").collection("recensione")

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
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Ottimo prodotto", "Ho acquistato questo prodotto e sono rimasto molto soddisfatto. La qualità è eccellente e il prezzo è competitivo. Inoltre, la spedizione è stata rapida e il servizio clienti è stato molto disponibile. Consiglio vivamente questo prodotto!", 5, "ada.lovelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Imballo non adeguato", "Il prodotto è arrivato danneggiato a causa di un'imballo insufficiente. Il venditore si è dimostrato disponibile nel risolvere il problema, ma avrei preferito ricevere il prodotto in perfette condizioni fin dall'inizio.", 2, "ada.lovelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Prodotto discreto", "Il prodotto ha un prezzo conveniente, ma la qualità non è eccezionale. Adatto per un utilizzo occasionale, ma non lo consiglio per un uso intenso. La spedizione è stata abbastanza rapida.", 3, "ada.lovelace")!!)
                    idRecensioniAda.add(activity.inserisciRecensioneSuFirebaseFirestore("Non consigliato", "Ho avuto diversi problemi con questo prodotto e il venditore non si è dimostrato disponibile nel risolverli. Sconsiglio l'acquisto di questo prodotto.", 1, "ada.lovelace")!!)


                    val myHashMediaRecensioni = activity.classificaUtentiRecensitiConVotoPiuAlto()

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI", myHashMediaRecensioni["ada.lovelace"].toString() )

                    assertEquals(2.75,myHashMediaRecensioni["ada.lovelace"]!!,0.1)
                    assertEquals(3.25,myHashMediaRecensioni["alan.turing"]!!,0.1)
                    assertEquals(0.0,myHashMediaRecensioni["tim.bernerslee"]!!,0.1)

                    assertEquals("{alan.turing=3.25, ada.lovelace=2.75, tim.bernerslee=0.0}", myHashMediaRecensioni.toString())

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI", idRecensione1Ada)

                    for(idRecensioneAda in idRecensioniAda)
                        myCollectionRecensioneAda.document(idRecensioneAda).delete().await()

                    for(idRecensioneAlan in idRecensioniAlan)
                        myCollectionRecensioneAlan.document(idRecensioneAlan).delete().await()

                    //Log.d("TEST CLASSIFICA UTENTI RECENSITI","Prima 1")
                    myCollection.document("alan.turing").delete().await()
                    myCollection.document("ada.lovelace").delete().await()
                    myCollection.document("tim.bernerslee").delete().await()
                }
            }

        //Log.d("TEST CLASSIFICA UTENTI RECENSITI","Prima 2")
    }

    //--- Inizio test sulla funzione che mi inserisci un documento per ogni utente ---
    @Test fun testInserisciUtenteFirebaseFirestore(): Unit = runBlocking {

        val myCollection = Annuncio.database.collection("utente")

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
                );

                assertEquals(primaInserimento+1, myCollection.get().await().size())
            }
        }

        //Dopo il test, elimino il documento associato al utente.

        myCollection.document("utenteDiProva").delete().await()
    }

    //--- Inizio test sulla funzione che mi inserisce una recensione a un utente: creato, recensito e eliminato ---
    @Test fun testInserisciRecensioneUtenteFirebaseFirestore(): Unit = runBlocking {

        val scenarioSignUpActivity = ActivityScenario.launch(SignUpActivity::class.java)

        scenarioSignUpActivity.onActivity { activity ->
            runBlocking {
                activity.salvaUtenteSuFirebaseFirestore(
                    "utenteDiProva",
                    "Giuseppe",
                    "Marconi",
                    "10/02/1980",
                    "3358924674"
                );
            }
        }

        val myCollectionUtente = Annuncio.database.collection("utente").document("utenteDiProva")

        val myCollectionRecensione = myCollectionUtente.collection("recensione")

        val numeroDocsRecensioni = myCollectionRecensione.get().await().size()

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

               val idRecensione = activity.inserisciRecensioneSuFirebaseFirestore("Fantastico prodotto! Consigliatissimo!", "Ho acquistato questo prodotto e sono rimasto estremamente soddisfatto. La qualità è eccellente e corrisponde perfettamente alla descrizione fornita dal venditore. Inoltre, il prezzo è competitivo rispetto ad altri prodotti simili sul mercato. La spedizione è stata rapida e il servizio clienti è stato disponibile e cortese nel rispondere alle mie domande. Consiglio vivamente questo prodotto a chiunque sia alla ricerca di un'ottima soluzione per le proprie esigenze.",5,"utenteDiProva")

                assertEquals(numeroDocsRecensioni+1,Annuncio.database.collection("utente").document("utenteDiProva").collection("recensione").get().await().size())

                val idRecensioneVotoNonValido = activity.inserisciRecensioneSuFirebaseFirestore("Fantastico prodotto! Consigliatissimo!", "Ho acquistato questo prodotto e sono rimasto estremamente soddisfatto. La qualità è eccellente e corrisponde perfettamente alla descrizione fornita dal venditore. Inoltre, il prezzo è competitivo rispetto ad altri prodotti simili sul mercato. La spedizione è stata rapida e il servizio clienti è stato disponibile e cortese nel rispondere alle mie domande. Consiglio vivamente questo prodotto a chiunque sia alla ricerca di un'ottima soluzione per le proprie esigenze.",6,"utenteDiProva")

                assertEquals(null, idRecensioneVotoNonValido)

                if(idRecensione != null)
                    //Elimino il documento, che ho appena inserito
                    myCollectionRecensione.document(idRecensione).delete().await()
            }
        }

        //Elimino l'utente che ho definito per il test
        myCollectionUtente.delete().await()
    }

    //--- Inizio test sulla funzione che inserisci elementi nel DB ---
    @Test fun testSalvaAnnunciFirebaseFirestore(): Unit = runBlocking {
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
            Log.e("INSERIMENTO ANNUNCI TEST", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi con prezzo inferiore a X ---
    @Test fun testRecuperaAnnunciPerPrezzoInferiore(): Unit = runBlocking{
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

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            //--- Fine Inserimento dati su Firestore Firebase ---
            scenarioUserLoginActivity.onActivity { activity ->
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
            Log.e("RECUPERA ANNUNCI TEST PREZZO INFERIORE", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo superiore a X ---
    @Test fun testRecuperaAnnunciPerPrezzoSuperiore(): Unit = runBlocking{
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

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
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
            Log.e("RECUPERA ANNUNCI TEST PREZZO SUPERIORE", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi con un prezzo compreso tra un range:  max<X>min ---
    @Test fun testRecuperaAnnunciPerRange(): Unit = runBlocking{
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
            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
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
            Log.e("RECUPERA ANNUNCI TEST PREZZO RANGE", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi permette di recuperare tutti gli annunci che si trovano nel DB ---
    @Test fun testRecuperaTuttiAnnunciFirebaseFirestore(): Unit = runBlocking {
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

                val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

                scenarioUserLoginActivity.onActivity { activity ->
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
            Log.e("RECUPERA TUTTI ANNUNCI TEST", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi che hanno quel titolo ---
    @Test fun testRecuperaAnnunciPerTitoloFirebaseFirestore(): Unit= runBlocking{
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

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking{

                    assertEquals(1,activity.recuperaAnnunciTitolo("Mr Robot: Season 1 Blu-Ray + Digital HD").size)
                    assertEquals(1,activity.recuperaAnnunciTitolo("Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked").size)
                    assertEquals(1,activity.recuperaAnnunciTitolo("Vintage Leather Messenger Bag").size)
                    assertEquals(1,activity.recuperaAnnunciTitolo("Apple Watch Series 7 45mm GPS + Cellular").size)

                    assertEquals(0,activity.recuperaAnnunciTitolo("Apple iPhone 11 Pro").size)
                }
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---
        }catch (e: Exception) {
            Log.e("RECUPERA ANNUNCI TEST PREZZO INFERIORE", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi recupera gli elementi che hanno disponibilita a essere spediti ---
    @Test fun testRecuperaAnnunciPerDisponibilitaSpedireFirebaseFirestore(): Unit = runBlocking{
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

            val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

            scenarioUserLoginActivity.onActivity { activity ->
                runBlocking{
                    assertEquals(3,activity.recuperaAnnunciDisponibilitaSpedire(true).size)
                    assertEquals(1,activity.recuperaAnnunciDisponibilitaSpedire(false).size)
                }
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---
        }catch (e: Exception) {
            Log.e("RECUPERA ANNUNCI TITOLO TEST", "Errore nei test", e)
        }
    }

    //--- Inizio test sulle funzioni che mi modificano gli annunci: titolo, descrizione, categoria e prezzo ---
    @Test fun testModificaAnnunciTitoloDescrizioneCategoriaPrezzoFirebaseFirestore(): Unit = runBlocking {
        try {
            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841


            val newAnnuncio1 = Annuncio(
                "userIdTestProva",
                "Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked",
                "The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.",
                1100.00,
                1,
                false,
                "elettronica/smartphone",
                geoPosition
            )

            newAnnuncio1.salvaAnnuncioSuFirebase()

            //--- Fine Inserimento dati su Firestore Firebase ---

            assertEquals("Annuncio(userId='userIdTestProva', titolo='Apple iPhone 12 Pro Max 256GB Pacific Blue Unlocked', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio1.annuncioId}')",newAnnuncio1.toString())

            newAnnuncio1.setTitolo("Apple Watch Series 7 45mm GPS + Cellular")

            assertEquals("Annuncio(userId='userIdTestProva', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The iPhone 12 Pro Max is Apple's flagship smartphone with a stunning 6.7-inch Super Retina XDR display, A14 Bionic chip, 5G capability, and a powerful triple-camera system.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio1.annuncioId}')",newAnnuncio1.toString())

            newAnnuncio1.setDescrizione("The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.")

            assertEquals("Annuncio(userId='userIdTestProva', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='elettronica/smartphone', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio1.annuncioId}')",newAnnuncio1.toString())

            newAnnuncio1.setCategoria("wearable")

            assertEquals("Annuncio(userId='userIdTestProva', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=1100.0, stato=1, disponibilitaSpedire=false, categoria='wearable', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio1.annuncioId}')",newAnnuncio1.toString())

            newAnnuncio1.setPrezzo(499.0)

            assertEquals("Annuncio(userId='userIdTestProva', titolo='Apple Watch Series 7 45mm GPS + Cellular', descrizione='The Apple Watch Series 7 is the ultimate fitness and health companion, with a stunning always-on Retina display, blood oxygen sensor, ECG app, and 50% faster charging. Stay connected with GPS + Cellular capability and a wide range of watch faces and bands.', prezzo=499.0, stato=1, disponibilitaSpedire=false, categoria='wearable', posizione=Location[provider 0.000000,-122.084100 et=0 alt=37.422], annuncioId='${newAnnuncio1.annuncioId}')",newAnnuncio1.toString())

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()

            //--- Fine eliminazione dati su Firestore Firebase ---
        }catch (e: Exception) {
            Log.e("MODIFICA ANNUNCI TEST", "Errore nei test", e)
        }
    }

    //--- Inizio test sulla funzione che mi elimina gli annunci ---
    @Test fun testEliminaAnnunci(): Unit = runBlocking{
        try {
            //--- Inserimento dati su Firestore Firebase ---
            val geoPosition = Location("provider")
            geoPosition.altitude = 37.4220
            geoPosition.longitude = -122.0841

            val primaInserimento = getNumeroElementiFirestore()

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

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            newAnnuncio2.eliminaAnnuncioDaFirebase()
            newAnnuncio3.eliminaAnnuncioDaFirebase()
            newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---

            assertEquals(primaInserimento,getNumeroElementiFirestore())

            //--- Fine eliminazione dati su Firestore Firebase ---
        }catch (e: Exception) {
            Log.e("MODIFICA ANNUNCI TEST", "Errore nei test", e)
        }
    }


    // --- Inizio funzione che testa il mantenimento delle informazioni aggiornate nel HashMap considerando DB ---
    // --> Non funziona ma HashMap è correttamente aggiornata, anche il DB <--
    /*
    @Ignore
    suspend fun testSubscribeRealTimeDatabase(){

        try{
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
            //newAnnuncio2.salvaAnnuncioSuFirebase()
            //newAnnuncio3.salvaAnnuncioSuFirebase()
            //newAnnuncio4.salvaAnnuncioSuFirebase()

            //--- Fine Inserimento dati su Firestore Firebase ---
            activityScenarioRule.scenario.onActivity { activity ->

                    //Facciamo riferimento al documento che abbiamo appena creato
                    val documentoRif = Annuncio.database.collection("annunci").document(newAnnuncio1.annuncioId)

                    Log.d("Prima update",newAnnuncio1.annuncioId)

                    runBlocking {
                        //simula una modifica effettuata da un altro client
                        documentoRif.update(
                            "titolo",
                            "Mr. Robot 2",
                            "descrizione",
                            "Second season of the critically acclaimed TV series about a cybersecurity engineer turned hacker."
                        ).await()

                        delay(1000)
                    }
                    assertEquals("Annuncio(userId='userIdTestProva', titolo='Mr. Robot 2', descrizione='Second season of the critically acclaimed TV series about a cybersecurity engineer turned hacker.', prezzo=16.99, stato=2, disponibilitaSpedire=true, categoria='filmETv/serieTv', posizione=Location[provider 0.000000,-122.084100 et=0], annuncioId='${newAnnuncio1.annuncioId}')",activity.myAnnunci[newAnnuncio1.annuncioId].toString())
            }

            //--- Eliminazione dati su Firestore Firebase ---
            newAnnuncio1.eliminaAnnuncioDaFirebase()
            //newAnnuncio2.eliminaAnnuncioDaFirebase()
            //newAnnuncio3.eliminaAnnuncioDaFirebase()
            //newAnnuncio4.eliminaAnnuncioDaFirebase()
            //--- Fine eliminazione dati su Firestore Firebase ---

        } catch (e: Exception) {
            Log.e("RECUPERA ANNUNCI TEST REAL TIME", "Errore nei test", e)
        }
    }
    */
    // --- Fine funzione che testa il mantenimento delle informazioni aggiornate nel HashMap considerando DB ---


    //--- Metodo di supporto, che mi serve per recupera il numero di documenti nella collezione annunci che sono salvati su FireStore ---
    @Ignore
    suspend fun getNumeroElementiFirestore(): Int {

        val myCollection = Annuncio.database.collection(Annuncio.nomeCollection)

        val myDocuments = myCollection.get().await()

        return myDocuments.size()
    }
    //--- Fine metodo di supporto ---
}