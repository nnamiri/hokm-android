package eu.amiri.hokm.engine

import kotlin.random.Random

/** How strong a computer opponent plays. */
enum class BotDifficulty { EASY, NORMAL, HARD }

/**
 * A rule-based computer opponent. It only ever sees a [GameSnapshot], i.e.
 * exactly the information a human in that seat would have. All heuristics are
 * variant-aware (classic trump, or high/low). Faithful port of the iOS bot.
 */
object HokmBot {
    /** Picks the strongest suit as trump: longest suit, honors as tiebreaker. */
    fun chooseTrump(hand: List<Card>): Suit {
        var bestSuit = Suit.SPADES
        var bestScore = -1
        for (suit in Suit.entries) {
            val cards = hand.filter { it.suit == suit }
            var score = cards.size * 10
            for (card in cards) {
                score += when (card.rank) {
                    Rank.ACE -> 4; Rank.KING -> 3; Rank.QUEEN -> 2; Rank.JACK -> 1
                    else -> 0
                }
            }
            if (score > bestScore) { bestScore = score; bestSuit = suit }
        }
        return bestSuit
    }

    /**
     * Picks among all six declarations: a very strong hand calls "high", a
     * very weak one "low", otherwise the best suit becomes trump.
     */
    fun bestDeclaration(hand: List<Card>): TrumpChoice {
        if (hand.isEmpty()) return TrumpChoice.High
        val total = hand.sumOf { it.rank.value }
        val average = total.toDouble() / hand.size
        if (average >= 10.5) return TrumpChoice.High
        if (average <= 5.5) return TrumpChoice.Low
        return TrumpChoice.OfSuit(chooseTrump(hand))
    }

    /**
     * The next action for the snapshot's seat, or null when it is not that
     * seat's move (bots never trigger StartNextHand; the host UI does).
     */
    fun nextAction(snapshot: GameSnapshot, difficulty: BotDifficulty = BotDifficulty.NORMAL): PlayerAction? {
        return when (snapshot.phase) {
            GamePhase.ChoosingTrump -> {
                if (!snapshot.iAmHakem) return null
                if (difficulty == BotDifficulty.EASY) {
                    val options = mutableListOf<TrumpChoice>()
                    snapshot.hand.map { it.suit }.toSet().forEach { options.add(TrumpChoice.OfSuit(it)) }
                    options.add(TrumpChoice.High); options.add(TrumpChoice.Low)
                    PlayerAction.ChooseMode(options.randomOrNull() ?: TrumpChoice.High)
                } else {
                    PlayerAction.ChooseMode(bestDeclaration(snapshot.hand))
                }
            }
            GamePhase.Discarding -> {
                if (!snapshot.needsDiscard) return null
                val mode = snapshot.trumpChoice ?: TrumpChoice.High
                val sorted = snapshot.hand.sortedBy { declarationStrength(it, mode) }
                if (sorted.size < 2) return null
                PlayerAction.DiscardTwo(sorted.take(2))
            }
            GamePhase.Drawing -> {
                val revealed = snapshot.revealedCard
                if (!snapshot.isMyTurn || revealed == null) return null
                if (difficulty == BotDifficulty.EASY) {
                    if (Random.nextBoolean()) PlayerAction.TakeCard else PlayerAction.RejectCard
                } else {
                    val mode = snapshot.trumpChoice ?: TrumpChoice.High
                    if (declarationStrength(revealed, mode) >= 11) PlayerAction.TakeCard else PlayerAction.RejectCard
                }
            }
            GamePhase.Playing -> {
                if (!snapshot.isMyTurn) return null
                val card = chooseCard(snapshot, difficulty) ?: return null
                PlayerAction.PlayCard(card)
            }
            is GamePhase.HandOver, is GamePhase.GameOver -> null
        }
    }

    /** How valuable a card is under [mode] (trumps very strong; ranks invert in low). */
    fun declarationStrength(card: Card, mode: TrumpChoice): Int {
        val rankStrength = if (mode.isInverted) 16 - card.rank.value else card.rank.value
        return rankStrength + (if (card.suit == mode.suit) 100 else 0)
    }

    /** Heuristic card choice; see [BotDifficulty] for the levels. */
    fun chooseCard(snapshot: GameSnapshot, difficulty: BotDifficulty = BotDifficulty.NORMAL): Card? {
        val legal = snapshot.legalCards
        if (legal.isEmpty()) return null
        val mode = snapshot.trumpChoice ?: return legal.first()

        if (difficulty == BotDifficulty.EASY) return legal.randomOrNull()

        val trump = mode.suit

        fun strength(card: Card): Int {
            val rankStrength = if (mode.isInverted) 16 - card.rank.value else card.rank.value
            return rankStrength + (if (card.suit == trump) 100 else 0)
        }
        fun cheapest(cards: List<Card>): Card? = cards.minByOrNull { strength(it) }
        fun strongest(cards: List<Card>): Card? = cards.maxByOrNull { strength(it) }

        val trick = snapshot.currentTrick
        val led = trick.ledSuit
        val winning = trick.winningPlay(mode)
        if (led == null || winning == null) {
            // We are leading the trick.
            if (difficulty == BotDifficulty.HARD) {
                val bosses = legal.filter { it.suit != trump && isBoss(it, snapshot) }
                strongest(bosses)?.let { return it }
            }
            val topRank = if (mode.isInverted) Rank.TWO else Rank.ACE
            legal.firstOrNull { it.rank == topRank && it.suit != trump }?.let { return it }
            val sideSuits = legal.filter { it.suit != trump }.groupBy { it.suit }
            sideSuits.values.maxByOrNull { it.size }?.let { return cheapest(it) }
            return strongest(legal)
        }

        if (winning.seat == snapshot.seat.partner) return cheapest(legal)

        val beating = legal.filter { it.beats(winning.card, mode, led) }
        if (beating.isNotEmpty()) {
            val isLastPlayer = trick.plays.size == 3
            if (difficulty == BotDifficulty.HARD && !isLastPlayer) {
                val bossWinners = beating.filter { isBoss(it, snapshot) }
                cheapest(bossWinners)?.let { return it }
            }
            return cheapest(beating)
        }
        return cheapest(legal)
    }

    /**
     * True when no unseen card of the same suit outranks [card] in the current
     * mode (everything stronger was already played or is in our own hand).
     */
    fun isBoss(card: Card, snapshot: GameSnapshot): Boolean {
        val mode = snapshot.trumpChoice ?: return false
        val seen = (snapshot.playedCards + snapshot.hand).toSet()
        for (rank in Rank.entries) {
            val candidate = Card(rank, card.suit)
            if (candidate.beats(card, mode, card.suit) && candidate !in seen) return false
        }
        return true
    }
}
