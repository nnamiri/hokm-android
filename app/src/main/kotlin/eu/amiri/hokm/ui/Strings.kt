package eu.amiri.hokm.ui

import eu.amiri.hokm.engine.BotDifficulty
import eu.amiri.hokm.engine.Suit
import eu.amiri.hokm.engine.Team
import eu.amiri.hokm.engine.TrumpChoice

/**
 * German UI strings, kept in one place so the app can grow into a real
 * localization (en/fa + RTL) later without touching every composable – the
 * iOS app uses the same "one table, lookup by key" idea.
 */
object De {
    const val TAGLINE = "Das persische Stichspiel – 2 Teams, 13 Karten, 7 Stiche"

    // Menu
    const val RESUME_GAME = "Spiel fortsetzen"
    const val RESUME_GAME_SUB = "Dein unterbrochenes Solo-Spiel wartet"
    const val NEW_SOLO_GAME = "Neues Solo-Spiel"
    const val SOLO_SUB = "Sofort losspielen"
    const val BOT_STRENGTH = "Bot-Stärke"
    const val PLAYERS_LABEL = "Spieler (Solo)"
    const val START = "Spiel starten"
    const val DISCARD_SAVED_GAME = "Gespeichertes Spiel verwerfen"

    // Table
    const val YOU = "Du"
    const val PAUSE = "Pause"
    const val YOUR_TURN = "Du bist am Zug"
    const val POINTS = "Punkte"
    const val TRICKS = "Stiche"
    const val WE = "Wir"
    const val OPPONENTS = "Gegner"
    const val PARTNER = "Partner"
    const val HAKEM = "Hakem"
    const val YOU_ARE_HAKEM = "Du bist Hakem!"
    const val PICK_TRUMP_TEXT =
        "Wähle anhand deiner ersten fünf Karten: eine Trumpffarbe – oder Hoch bzw. Niedrig (ohne Trumpf)."
    const val PICK_TRUMP_TEXT_2P =
        "Wähle anhand deiner ersten vier Karten: eine Trumpffarbe – oder Hoch bzw. Niedrig (ohne Trumpf)."
    const val HIGH = "Hoch"
    const val LOW = "Niedrig"

    // Two-player draw phase
    const val DISCARD_PROMPT = "Wirf 2 deiner 4 Karten ab"
    const val DISCARD_CONFIRM = "Abwerfen"
    const val DISCARD_WAITING = "Gegner wirft ab …"
    const val DRAW_TAKE = "Nehmen"
    const val DRAW_REJECT = "Wegwerfen"
    const val DRAW_PROMPT = "Nimm diese Karte – oder wirf sie weg und nimm dafür die nächste."
    const val STOCK_LABEL = "Stapel"
    const val DRAW_TAKEN = "Genommen"
    const val DRAW_DISCARDED = "Weggeworfen"

    // Banners
    const val HAND_WON = "🎉 Runde gewonnen!"
    const val HAND_LOST = "Runde verloren"
    const val KOT_TEXT = "Kut! 7:0 – das bringt 2 Punkte."
    const val HAKEM_KOT_TEXT = "Hakem-Kut! 7:0 gegen den Hakem – das bringt 3 Punkte."
    const val SCORE_LABEL = "Spielstand:"
    const val NEXT_ROUND = "Nächste Runde"
    const val GAME_WON = "🏆 Gewonnen!"
    const val GAME_LOST = "😔 Verloren"
    const val BACK_TO_MENU = "Zurück zum Menü"
    const val RESUME_PLAY = "Weiterspielen"
    const val MAIN_MENU = "Hauptmenü"

    // Statistics
    const val STATISTICS = "Statistik"
    const val NO_GAMES_YET = "Noch keine Spiele – Zeit für die erste Runde!"
    const val GAMES = "Spiele"
    const val WON = "Gewonnen"
    const val LOST = "Verloren"
    const val WIN_RATE = "Siegquote"
    const val STREAK = "Serie"
    const val BEST_STREAK = "Beste Serie"
    const val KOT_ROUNDS = "Kut-Runden"
    const val ROUNDS_WON = "Runden gewonnen"
    const val RESET_STATS = "Statistik zurücksetzen"
    const val BACK = "Zurück"

    fun turnOf(name: String) = "$name ist am Zug …"
    fun choosingTrumpBy(name: String) = "$name wählt die Trumpffarbe …"
    fun drawingTurn(name: String) = "$name zieht …"
    fun round(number: Int) = "Runde $number"
    fun tricksScore(winner: Int, loser: Int) = "Stiche: $winner : $loser"
    fun weVsOpp(mine: Int, theirs: Int) = "Wir $mine : $theirs Gegner"
    fun finalScore(mine: Int, theirs: Int) = "Endstand: Wir $mine : $theirs Gegner"
    fun hakemChose(name: String, what: String) = "$name hat $what bestimmt"
    fun hakemChoseTrump(name: String, what: String) = "$name hat $what als Trumpf bestimmt"
    fun playersCount(count: Int) = "$count Spieler"
}

val Suit.germanName: String
    get() = when (this) {
        Suit.SPADES -> "Pik"
        Suit.HEARTS -> "Herz"
        Suit.DIAMONDS -> "Karo"
        Suit.CLUBS -> "Kreuz"
    }

val BotDifficulty.germanName: String
    get() = when (this) {
        BotDifficulty.EASY -> "Leicht"
        BotDifficulty.NORMAL -> "Mittel"
        BotDifficulty.HARD -> "Schwer"
    }

/** Label from the point of view of [myTeam]. */
fun Team.label(myTeam: Team): String = if (this == myTeam) De.WE else De.OPPONENTS

/** Spoken form of a declaration, e.g. "♥ Herz" or "Niedrig". */
val TrumpChoice.germanText: String
    get() = when (this) {
        is TrumpChoice.OfSuit -> "${suit.symbol} ${suit.germanName}"
        TrumpChoice.High -> De.HIGH
        TrumpChoice.Low -> De.LOW
    }
