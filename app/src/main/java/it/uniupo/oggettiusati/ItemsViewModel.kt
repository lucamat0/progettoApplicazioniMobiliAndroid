package it.uniupo.oggettiusati

data class ItemsViewModel(val annuncioId: String?, val image: Int, val title: String, val price : Double? = 0.0, val emailOwner: String? = "default@mail.com", val nTelOwner: String = "0123456789"){

}
