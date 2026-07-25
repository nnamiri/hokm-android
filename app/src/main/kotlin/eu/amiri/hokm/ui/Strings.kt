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
    const val FONT_SIZE = "Schriftgröße"
    const val PLAYERS_LABEL = "Spieler (Solo)"
    const val START = "Spiel starten"
    const val DISCARD_SAVED_GAME = "Gespeichertes Spiel verwerfen"

    // Table
    const val YOU = "Du"
    const val PAUSE = "Pause"
    const val ROUND_WORD = "Runde"
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

    // Rules
    const val RULES = "Spielregeln"
    const val RULES_GAME_TITLE = "Das Spiel"
    const val RULES_GAME_TEXT =
        "Hokm (persisch: حکم, „Befehl“) ist das beliebteste Kartenspiel des Irans. " +
            "Gespielt wird mit 52 Karten, das Ass ist die höchste. Zu viert bildet ihr zwei " +
            "Teams – die Partner sitzen einander gegenüber. Zu zweit trittst du im direkten " +
            "Duell an."
    const val RULES_HAKEM_TITLE = "Der Hakem"
    const val RULES_HAKEM_TEXT =
        "Ein Spieler ist der Hakem (Gebieter). Er bestimmt die Trumpffarbe (Hokm) anhand " +
            "seiner ersten Karten – fünf zu viert, vier zu zweit. Zu viert werden danach die " +
            "restlichen Karten verteilt, bis alle 13 halten; zu zweit folgt die Ziehphase " +
            "(siehe unten). Der Hakem spielt zum ersten Stich aus."
    const val RULES_2P_TITLE = "Hokm zu zweit"
    const val RULES_2P_TEXT =
        "Beide Spieler erhalten 4 Karten; der Hakem sagt danach den Trumpf an, dann wirft " +
            "jeder 2 Karten ab. Nun wird abwechselnd gezogen: Du siehst die oberste Karte – " +
            "nimmst du sie, wird die nächste abgeworfen; wirfst du sie weg, nimmst du dafür " +
            "die nächste. Beide Karten werden dir dabei offen gezeigt. Ist der Stapel leer " +
            "(je 13 Karten), beginnt das normale Stichspiel bis 7 Stiche."
    const val RULES_TRICK_TITLE = "Das Stichspiel"
    const val RULES_TRICK_TEXT =
        "Es muss immer die ausgespielte Farbe bedient werden. Wer nicht bedienen kann, darf " +
            "trumpfen oder abwerfen. Den Stich gewinnt der höchste Trumpf, sonst die höchste " +
            "Karte der angespielten Farbe. Der Gewinner eines Stichs spielt zum nächsten aus."
    const val RULES_SCORING_TITLE = "Wertung"
    const val RULES_SCORING_TEXT =
        "Wer zuerst 7 Stiche gewinnt, holt die Runde und 1 Punkt. Gewinnt eine Seite alle 7 " +
            "Stiche zu null („Kut“), gibt es 2 Punkte – geschieht das dem Hakem („Hakem-Kut“), " +
            "sogar 3 Punkte. Verliert der Hakem die Runde, wandert die Rolle weiter. Wer zuerst " +
            "7 Punkte erreicht, gewinnt das Spiel."
    const val RULES_HIGH_LOW_TITLE = "Ansage: Hoch oder Niedrig"
    const val RULES_HIGH_LOW_TEXT =
        "Neben den vier Trumpffarben kann der Hakem auch „Hoch“ oder „Niedrig“ ansagen. Dann " +
            "gibt es keinen Trumpf: Bei Hoch gilt die normale Reihenfolge (Ass ist am " +
            "stärksten), bei Niedrig ist sie umgedreht – die Zwei sticht alles in ihrer Farbe. " +
            "Es gewinnt immer die stärkste Karte der angespielten Farbe; abwerfen ist erlaubt, " +
            "aber eine andere Farbe kann nie gewinnen. Wertung wie gewohnt."
    const val RULES_APP_TITLE = "In dieser App"
    const val RULES_APP_TEXT =
        "Solo: Du spielst zu zweit im Duell gegen einen Bot oder zu viert mit einem " +
            "Bot-Partner gegen zwei Bots – die Stärke wählst du im Menü. Dein laufendes Spiel " +
            "wird automatisch gespeichert und wartet auf der Startseite auf dich."

    // Tutorial
    const val TUTORIAL = "Tutorial"
    const val TUTORIAL_SUB = "Hokm in 5 Schritten lernen"
    const val SKIP = "Überspringen"
    const val NEXT = "Weiter"
    const val LETS_GO = "Los geht's!"
    const val TRUMP_LABEL = "Trumpf"
    const val LED_LABEL = "angespielt"
    const val TRUMP_BEATS = "Trumpf sticht!"
    const val ROUND_WON_ROW = "Runde gewonnen"
    const val KOT_ROW = "Kut (7:0)"
    const val HAKEM_KOT_ROW = "Hakem-Kut (7:0)"
    const val POINTS_ONE = "1 Punkt"
    const val POINTS_TWO = "2 Punkte"
    const val POINTS_THREE = "3 Punkte"
    const val SOLO_WORD = "Solo"
    const val SAVED_WORD = "Gespeichert"

    const val OB1_TITLE = "Willkommen bei Hokm!"
    const val OB1_TEXT =
        "Hokm (حکم) ist das beliebteste Kartenspiel des Irans. Zu viert spielt ihr in zwei " +
            "Teams – dein Partner sitzt dir gegenüber. Zu zweit trittst du im direkten Duell an."
    const val OB2_TITLE = "Der Hakem bestimmt den Trumpf"
    const val OB2_TEXT =
        "Ein Spieler ist der Hakem (Gebieter). Er legt die Trumpffarbe anhand seiner ersten " +
            "Karten fest – fünf zu viert, vier zu zweit. Am Ende hält jeder 13 Karten, und der " +
            "Hakem spielt aus."
    const val OB3_TITLE = "Farbe bedienen!"
    const val OB3_TEXT =
        "Du musst immer die angespielte Farbe bedienen, wenn du sie hast. Nur wenn du nicht " +
            "bedienen kannst, darfst du trumpfen – oder eine andere Karte abwerfen. Der höchste " +
            "Trumpf sticht alles."
    const val OB4_TITLE = "7 Stiche gewinnen die Runde"
    const val OB4_TEXT =
        "Wer zuerst 7 Stiche holt, gewinnt die Runde (1 Punkt). Ein 7:0 heißt „Kut“ und bringt " +
            "2 Punkte – geschieht es dem Hakem, sogar 3. Wer zuerst 7 Punkte hat, gewinnt das Spiel."
    const val OB5_TITLE = "Bereit? Los geht's!"
    const val OB5_TEXT =
        "Starte solo gegen Bots – zu zweit oder zu viert, Stärke im Menü wählbar. Mit der " +
            "Pause-Taste kannst du jederzeit unterbrechen – dein Spiel wird automatisch " +
            "gespeichert und wartet auf dich."

    // In-game spotlight tutorial (first 2P/4P game)
    const val GOT_IT = "Alles klar!"
    const val COACH_HEADER =
        "Deine Anzeige oben: Gold ist immer deine Seite. In der Mitte Punkte, Stiche, " +
            "Hakem und Trumpf – rechts außen die Runde."
    const val COACH_DRAW =
        "Ihr zieht jetzt abwechselnd: Nimm die offene Karte oder wirf sie ab – bis jeder " +
            "13 Karten hat. Danach beginnt das Stichspiel."
    const val COACH_HAND =
        "Deine Hand: Bediene die angespielte Farbe – hervorgehobene Karten sind gerade spielbar."

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
