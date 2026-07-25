package eu.amiri.hokm.data

/** Persian and Kurdish first names for the solo bots, as on iOS. */
object BotNames {
    private val firstNames = listOf(
        // Persian
        "Reza", "Leila", "Nima", "Sara", "Darya", "Kian", "Roya", "Omid",
        "Parisa", "Arash", "Shirin", "Babak", "Nazanin", "Farid", "Kaveh",
        "Mitra", "Bijan", "Golnaz", "Sohrab", "Ava", "Ramin", "Setareh",
        // Kurdish
        "Jîna", "Rojîn", "Berîvan", "Dilan", "Baran", "Şîlan", "Hêvî", "Zana",
        "Rûken", "Newroz", "Zîn", "Kawa", "Diyar", "Şoresh", "Sîpan", "Aland",
    )

    /** [count] distinct random bot names ("Bot <Name>"). */
    fun random(count: Int): List<String> =
        firstNames.shuffled().take(count).map { "Bot $it" }
}
