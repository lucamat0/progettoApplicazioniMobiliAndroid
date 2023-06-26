package it.uniupo.oggettiusati.chat

/**
 * Rappresenta un messaggio
 *
 * @author Amato Luca
 * @property messaggio Contenuto del messaggio
 * @property userId Identificativo dell'utente destinatario
 */
data class Messaggio(var messaggio: String? = null, var userId: String? = null)