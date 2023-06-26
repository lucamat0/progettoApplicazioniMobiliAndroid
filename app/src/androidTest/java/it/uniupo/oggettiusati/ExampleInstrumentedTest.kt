package it.uniupo.oggettiusati

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.fragment.HomeFragment
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.*
import org.junit.*
import kotlin.collections.ArrayList

/**
 * Attenzione: Questa implementazione contiene dei test non completi, utilizzati nella prima parte della stesura del codice,
 * A causa di limiti di tempo non sono stati rimplementati.
 *
 * @author Amato Luca
 */
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("it.uniupo.oggettiusati", appContext.packageName)
    }
/*
    lateinit var myAnnunci : ArrayList <Annuncio>
    val database = Firebase.firestore

    //private lateinit var scenarioCartFragment: FragmentScenario<CartFragment>
    private lateinit var scenarioHomeFragment: FragmentScenario<HomeFragment>

    @Before fun testInserisciAnnunciFirebaseFirestore(){

        myAnnunci = ArrayList()

        myAnnunci.add(Annuncio(
            userId = "12345",
            titolo = "Bicicletta da montagna",
            descrizione = "Vendo bicicletta da montagna in ottime condizioni.",
            prezzo = 300.0,
            stato = 1,
            disponibilitaSpedire = true,
            categoria = "wjh7UFtzG2si73BGgRwQ",
            sottocategoria = null,
            posizione = GeoPoint(44.923114095766934, 8.61777862155143),
            timeStampInizioVendita = System.currentTimeMillis(),
            timeStampFineVendita = null,
            userIdAcquirente = null,
            annuncioId = "ABC123",
            venduto = false,
            acquirenteRecensito = false,
            proprietarioRecensito = false
        ))

        myAnnunci.add(Annuncio(
            userId = "54321",
            titolo = "Smartphone Samsung Galaxy S21",
            descrizione = "Vendo smartphone Samsung Galaxy S21, colore nero, 128 GB di memoria.",
            prezzo = 800.0,
            stato = 2,
            disponibilitaSpedire = true,
            categoria = "HTq7GqiBExVDcnpIYKeL",
            sottocategoria = "i25TUps67SAz3gzgfZrB",
            posizione = GeoPoint(44.923114095766934, 8.61777862155143),
            timeStampInizioVendita = System.currentTimeMillis(),
            timeStampFineVendita = null,
            userIdAcquirente = null,
            annuncioId = "XYZ789",
            venduto = false,
            acquirenteRecensito = false,
            proprietarioRecensito = false
        ))

        myAnnunci.add(Annuncio(
                userId = "67890",
                titolo = "MacBook Pro 15 pollici",
                descrizione = "Vendo MacBook Pro 15 pollici, modello 2019, 16 GB di RAM e 512 GB di storage.",
                prezzo = 2000.0,
                stato = 1,
                disponibilitaSpedire = false,
                categoria = "GvDJfrKyFUIMWOwZaXKU",
                sottocategoria = "YUpg3Mp7BJjkMlqbf9Ct",
                posizione = GeoPoint(44.89936597849873, 8.19964685024745),
                timeStampInizioVendita = System.currentTimeMillis(),
                timeStampFineVendita = null,
                userIdAcquirente = null,
                annuncioId = "DEF456",
                venduto = false,
                acquirenteRecensito = false,
                proprietarioRecensito = false
        ))

        myAnnunci.add(Annuncio(
            userId = "98765",
            titolo = "Tavolo da pranzo allungabile",
            descrizione = "Vendo tavolo da pranzo allungabile in legno massiccio, adatto per 6-8 persone.",
            prezzo = 500.0,
            stato = 2,
            disponibilitaSpedire = false,
            categoria =  "rcIDugWOWELVoXVSgoJC",
            sottocategoria = null,
            posizione = GeoPoint(44.89936597849873, 8.19964685024745),
            timeStampInizioVendita = System.currentTimeMillis(),
            timeStampFineVendita = null,
            userIdAcquirente = null,
            annuncioId = "GHI789",
            venduto = false,
            acquirenteRecensito = false,
            proprietarioRecensito = false
        ))

        myAnnunci.add(Annuncio(
            userId = "54321",
            titolo = "Felpa con cappuccio",
            descrizione = "Vendo felpa con cappuccio di marca famosa, taglia L, colore nero.",
            prezzo = 50.0,
            stato = 1,
            disponibilitaSpedire = true,
            categoria = "3YNfwrUc6Ur7smGp8DSt",
            sottocategoria = "M8QH1WOxyto9b5QgZlly",
            posizione = GeoPoint(44.9038741409022, 8.206342879158646),
            timeStampInizioVendita = System.currentTimeMillis(),
            timeStampFineVendita = null,
            userIdAcquirente = null,
            annuncioId = "MNO123",
            venduto = false,
            acquirenteRecensito = false,
            proprietarioRecensito = false
        ))

        myAnnunci.stream().forEach { annuncio -> runBlocking { annuncio.salvaAnnuncioSuFirebase(null) }}

    }
    @After fun testEliminaAnnunciUtentiFirebaseFirestore():Unit = runBlocking{

        myAnnunci.stream().forEach { annuncio -> runBlocking {  annuncio.eliminaAnnuncioDaFirebase() }}

    }
*/

    /*

    @Test fun testRicercaAnnuncioFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollectionUtente = database.collection(UserLoginActivity.Utente.nomeCollection);

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

                val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

                myCollection.document("ada.loelace").delete().await()
                myCollection.document("alan.turing").delete().await()
            }
        }
    }

    @Test fun testInserisciEliminaAnnuncioPreferitoFirebaseFirestore(){

        val scenarioUserLoginActivity = ActivityScenario.launch(UserLoginActivity::class.java)

        scenarioUserLoginActivity.onActivity { activity ->
            runBlocking {

                val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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

                val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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

              database.collection(UserLoginActivity.Utente.nomeCollection).document("alan.turing").collection("transazione").document(idTransazioneAlan).delete().await()
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

                val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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


        val myCollectionUtente = database.collection(UserLoginActivity.Utente.nomeCollection)

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

        val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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
                val myDocumentUtente = database.collection(UserLoginActivity.Utente.nomeCollection).document("alan.turing")

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
                    database.collection(UserLoginActivity.Utente.nomeCollection).document("alan.turing").collection("recensione")
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

    @Test fun testRecuperaAnnunciNonVendutiPerMostrarliNellaHome(): Unit = runBlocking {

        scenarioHomeFragment.onFragment { fragment ->
            runBlocking {

                //Nel caso in cui, non andassi a invocare nessun metodo che mi filtra, mi recupera tutti gli annunci presenti. (MAX 10)
                assertEquals(8,fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaAnnunciPrezzoInferiore(80)

                assertEquals(0,fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaTuttiAnnunci()

                fragment.recuperaAnnunciTitolo("Robot")

                assertEquals(0, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaTuttiAnnunci()

                assertEquals(8, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaAnnunciDisponibilitaSpedire(true)

                assertEquals(7, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaAnnunciPrezzoRange(18,1200)

                //Spedire + range.
                assertEquals(3, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                //prezzo superiore + spedire
                fragment.recuperaAnnunciPrezzoSuperiore(1200)

                assertEquals(4, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                //prezzo superiore + spedire
                fragment.recuperaAnnunciPrezzoInferiore(20)

                assertEquals(0, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)

                fragment.recuperaAnnunciPrezzoRange(10,5000)
                assertEquals(6, fragment.recuperaAnnunciPerMostrarliNellaHome()!!.size)
            }
        }
    }

*/
   // }

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

                val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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

        val myCollection = database.collection(UserLoginActivity.Utente.nomeCollection)

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
    /*
    @Ignore
    suspend fun getNumeroElementiFirestore(): Int {

        val myCollection = database.collection(Annuncio.nomeCollection)

        val myDocuments = myCollection.get().await()

        return myDocuments.size()
    }
    */
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