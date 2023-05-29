package it.uniupo.oggettiusati.chat

class Messaggio {

    var messaggio: String? = null
    var userId: String ? = null

    constructor(){}

    constructor(messaggio: String?, userId: String?){
        this.messaggio = messaggio
        this.userId = userId
    }
}