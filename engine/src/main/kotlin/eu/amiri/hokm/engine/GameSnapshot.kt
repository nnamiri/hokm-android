package eu.amiri.hokm.engine

/**
 * A redacted view of the game for one seat: it contains that player's own
 * cards but only card *counts* for everyone else, so a client never receives
 * information it should not have. (In solo play everything is local, but the
 * same shape keeps the door open for online multiplayer later.)
 */
data class GameSnapshot(
    val seat: Seat,
    val revision: Int,
    val handNumber: Int,
    val phase: GamePhase,
    val hakem: Seat,
    val trumpChoice: TrumpChoice?,
    val hand: List<Card>,
    val handCounts: Map<Seat, Int>,
    val currentTrick: Trick,
    val lastTrick: Trick?,
    val playedCards: List<Card>,
    val trickCounts: Map<Team, Int>,
    val scores: Map<Team, Int>,
    val turn: Seat?,
    val legalCards: List<Card>,
    val playerCount: Int,
    val stockCount: Int,
    val revealedCard: Card?,
    val pendingDiscards: List<Seat>,
    val lastDrawResult: DrawResult?,
) {
    val isMyTurn: Boolean get() = turn == seat
    val iAmHakem: Boolean get() = hakem == seat
    val myTeam: Team get() = seat.team

    /** The trump suit, when one exists (classic variant only). */
    val trump: Suit? get() = trumpChoice?.suit

    /** Two-player: whether this seat still has to discard two cards. */
    val needsDiscard: Boolean get() = seat in pendingDiscards
}

/** Builds the redacted per-player view. */
fun HokmGame.snapshot(forSeat: Seat): GameSnapshot = GameSnapshot(
    seat = forSeat,
    revision = revision,
    handNumber = handNumber,
    phase = phase,
    hakem = hakem,
    trumpChoice = trumpChoice,
    hand = CardSorting.displayOrder(handOf(forSeat), trump),
    handCounts = handCounts(),
    currentTrick = currentTrick.copyOf(),
    lastTrick = lastTrick?.copyOf(),
    playedCards = playedCards.toList(),
    trickCounts = trickCounts(),
    scores = scores(),
    turn = turn,
    legalCards = legalCards(forSeat),
    playerCount = rules.playerCount,
    stockCount = stockCount,
    revealedCard = if (turn == forSeat) revealedCard else null,
    pendingDiscards = pendingDiscards.toList(),
    lastDrawResult = if (lastDrawResult?.seat == forSeat) lastDrawResult else null,
)
