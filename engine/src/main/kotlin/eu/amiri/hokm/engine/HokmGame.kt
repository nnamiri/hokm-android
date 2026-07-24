package eu.amiri.hokm.engine

import kotlin.random.Random

/**
 * The authoritative Hokm game state machine (2 or 4 players in two teams).
 *
 * Flow of one 4-player hand:
 * 1. Every seat receives 5 cards (hakem first); the hakem declares trump.
 * 2. The remaining cards are dealt (4 + 4 per seat) so everyone holds 13.
 * 3. The hakem leads the first trick; players must follow suit when possible.
 * 4. The first team to take 7 tricks wins the hand and scores points.
 * 5. If the hakem's team lost, the hakem role passes to the next seat.
 *
 * A faithful port of the iOS `HokmGame` (kept as a mutable class here; the UI
 * reads immutable [GameSnapshot]s).
 */
class HokmGame {
    var rules: HokmRules
        private set
    var phase: GamePhase
        private set
    var hakem: Seat
        private set
    var trumpChoice: TrumpChoice? = null
        private set
    private val hands = mutableMapOf<Seat, MutableList<Card>>()
    var currentTrick: Trick
        private set
    var lastTrick: Trick? = null
        private set
    private val _playedCards = mutableListOf<Card>()
    val playedCards: List<Card> get() = _playedCards
    private val trickCounts = mutableMapOf(Team.ONE to 0, Team.TWO to 0)
    private val scores = mutableMapOf(Team.ONE to 0, Team.TWO to 0)
    var turn: Seat? = null
        private set
    var handNumber: Int = 1
        private set
    var revision: Int = 0
        private set

    private val undealt = mutableListOf<Card>()
    private val stock = mutableListOf<Card>()
    var revealedCard: Card? = null
        private set
    private val _pendingDiscards = mutableListOf<Seat>()
    val pendingDiscards: List<Seat> get() = _pendingDiscards
    var lastDrawResult: DrawResult? = null
        private set

    /** The trump suit, when one exists (null before the declaration / after high-low). */
    val trump: Suit? get() = trumpChoice?.suit

    /** The seats actually playing (2 or 4). */
    val activeSeats: List<Seat>
        get() = if (rules.playerCount == 2) listOf(Seat.SOUTH, Seat.WEST) else Seat.entries.toList()

    val stockCount: Int get() = stock.size

    fun handOf(seat: Seat): List<Card> = hands[seat] ?: emptyList()
    fun handCounts(): Map<Seat, Int> = hands.mapValues { it.value.size }
    fun trickCounts(): Map<Team, Int> = trickCounts.toMap()
    fun scores(): Map<Team, Int> = scores.toMap()

    private fun nextActive(after: Seat): Seat {
        val seats = activeSeats
        val i = seats.indexOf(after).let { if (it < 0) 0 else it }
        return seats[(i + 1) % seats.size]
    }

    constructor(
        firstHakem: Seat = Seat.SOUTH,
        rules: HokmRules = HokmRules(),
        seed: Long = Random.nextLong(),
    ) {
        this.rules = rules
        this.phase = GamePhase.ChoosingTrump
        this.hakem = firstHakem
        this.currentTrick = Trick(firstHakem)
        this.turn = firstHakem
        deal(seed)
    }

    /** Restores a game from a persisted [GameState] (see [state]). */
    constructor(state: GameState) {
        this.rules = state.rules
        this.phase = state.phase
        this.hakem = state.hakem
        this.trumpChoice = state.trumpChoice
        for ((seat, cards) in state.hands) hands[seat] = cards.toMutableList()
        this.currentTrick = state.currentTrick.toTrick()
        this.lastTrick = state.lastTrick?.toTrick()
        _playedCards.addAll(state.playedCards)
        trickCounts.putAll(state.trickCounts)
        scores.putAll(state.scores)
        this.turn = state.turn
        this.handNumber = state.handNumber
        this.revision = state.revision
        undealt.addAll(state.undealt)
        stock.addAll(state.stock)
        this.revealedCard = state.revealedCard
        _pendingDiscards.addAll(state.pendingDiscards)
        this.lastDrawResult = state.lastDrawResult
    }

    /** The full state, ready to be persisted and passed back to the constructor. */
    fun state(): GameState = GameState(
        rules = rules,
        phase = phase,
        hakem = hakem,
        trumpChoice = trumpChoice,
        hands = hands.mapValues { it.value.toList() },
        currentTrick = currentTrick.state(),
        lastTrick = lastTrick?.state(),
        playedCards = _playedCards.toList(),
        trickCounts = trickCounts.toMap(),
        scores = scores.toMap(),
        turn = turn,
        handNumber = handNumber,
        revision = revision,
        undealt = undealt.toList(),
        stock = stock.toList(),
        revealedCard = revealedCard,
        pendingDiscards = _pendingDiscards.toList(),
        lastDrawResult = lastDrawResult,
    )

    /** Starts a hand mid-play with fixed hands and trump. Used by unit tests. */
    constructor(testHands: Map<Seat, List<Card>>, hakem: Seat, trump: Suit, rules: HokmRules = HokmRules()) {
        this.rules = rules
        this.phase = GamePhase.Playing
        this.hakem = hakem
        this.trumpChoice = TrumpChoice.OfSuit(trump)
        for ((seat, cards) in testHands) hands[seat] = cards.toMutableList()
        this.currentTrick = Trick(hakem)
        this.turn = hakem
    }

    // MARK: - Dealing

    private fun deal(seed: Long) {
        val rng = SeededGenerator(seed)
        val deck = Deck.full.shuffled(rng).toMutableList()

        hands.clear()
        if (rules.playerCount == 2) {
            var seat = hakem
            repeat(2) {
                hands[seat] = deck.take(4).toMutableList()
                repeat(4) { deck.removeAt(0) }
                seat = nextActive(seat)
            }
            stock.clear(); stock.addAll(deck)
            undealt.clear()
        } else {
            var seat = hakem
            repeat(4) {
                hands[seat] = deck.take(5).toMutableList()
                repeat(5) { deck.removeAt(0) }
                seat = seat.next
            }
            undealt.clear(); undealt.addAll(deck)
            stock.clear()
        }
        revealedCard = null
        _pendingDiscards.clear()
        lastDrawResult = null

        trumpChoice = null
        phase = GamePhase.ChoosingTrump
        turn = hakem
        currentTrick = Trick(hakem)
        lastTrick = null
        _playedCards.clear()
        trickCounts[Team.ONE] = 0; trickCounts[Team.TWO] = 0
        revision++
    }

    // MARK: - Actions

    /**
     * The hakem's declaration: one of the four suits as trump, or high/low
     * (no trump, normal/inverted ranking). Then the rest of the deck is dealt.
     */
    fun chooseTrump(choice: TrumpChoice, by: Seat) {
        if (phase != GamePhase.ChoosingTrump) throw HokmException(HokmErrorKind.INVALID_PHASE)
        if (by != hakem) throw HokmException(HokmErrorKind.NOT_HAKEM)

        trumpChoice = choice

        if (rules.playerCount == 2) {
            phase = GamePhase.Discarding
            _pendingDiscards.clear(); _pendingDiscards.addAll(activeSeats)
            turn = null
        } else {
            var target = hakem
            repeat(2) {
                repeat(4) {
                    hands.getOrPut(target) { mutableListOf() }.addAll(undealt.take(4))
                    repeat(4) { undealt.removeAt(0) }
                    target = target.next
                }
            }
            phase = GamePhase.Playing
            turn = hakem
        }
        revision++
    }

    /** Two-player: discard exactly two of the first four cards. */
    fun discard(cards: List<Card>, from: Seat) {
        if (phase != GamePhase.Discarding) throw HokmException(HokmErrorKind.INVALID_PHASE)
        if (from !in _pendingDiscards) throw HokmException(HokmErrorKind.NOT_YOUR_TURN)
        val hand = hands[from]
        if (cards.size != 2 || cards.toSet().size != 2 || hand == null || !cards.all { it in hand }) {
            throw HokmException(HokmErrorKind.CARD_NOT_IN_HAND)
        }

        hands[from]?.removeAll { it in cards }
        _pendingDiscards.removeAll { it == from }
        if (_pendingDiscards.isEmpty()) {
            phase = GamePhase.Drawing
            turn = hakem
            revealedCard = stock.removeAt(0)
        }
        revision++
    }

    /**
     * Two-player draw turn: take the revealed card (the next one is burned
     * unseen) or reject it (then the next card must be taken).
     */
    fun resolveDraw(take: Boolean, by: Seat) {
        if (phase != GamePhase.Drawing) throw HokmException(HokmErrorKind.INVALID_PHASE)
        if (turn != by) throw HokmException(HokmErrorKind.NOT_YOUR_TURN)
        val revealed = revealedCard ?: throw HokmException(HokmErrorKind.INVALID_PHASE)

        if (take) {
            val thrownAway = stock.removeAt(0)
            hands.getOrPut(by) { mutableListOf() }.add(revealed)
            lastDrawResult = DrawResult(by, revealed, thrownAway, tookRevealed = true)
        } else {
            val forced = stock.removeAt(0)
            hands.getOrPut(by) { mutableListOf() }.add(forced)
            lastDrawResult = DrawResult(by, forced, revealed, tookRevealed = false)
        }
        revealedCard = null

        if (stock.isEmpty()) {
            phase = GamePhase.Playing
            turn = hakem
            currentTrick = Trick(hakem)
        } else {
            turn = nextActive(by)
            revealedCard = stock.removeAt(0)
        }
        revision++
    }

    /** Plays a card for [from], enforcing turn order and follow-suit. */
    fun play(card: Card, from: Seat) {
        if (phase != GamePhase.Playing) throw HokmException(HokmErrorKind.INVALID_PHASE)
        if (turn != from) throw HokmException(HokmErrorKind.NOT_YOUR_TURN)
        val hand = hands[from]
        if (hand == null || card !in hand) throw HokmException(HokmErrorKind.CARD_NOT_IN_HAND)
        if (card !in legalCards(from)) throw HokmException(HokmErrorKind.MUST_FOLLOW_SUIT)

        hands[from]?.removeAll { it == card }
        currentTrick.add(card, from)
        _playedCards.add(card)

        if (currentTrick.plays.size == rules.playerCount) {
            val winner = currentTrick.winningPlay(trumpChoice!!)!!.seat
            trickCounts[winner.team] = (trickCounts[winner.team] ?: 0) + 1
            lastTrick = currentTrick

            if ((trickCounts[winner.team] ?: 0) >= rules.tricksToWinHand) {
                finishHand(winner.team)
            } else {
                currentTrick = Trick(winner)
                turn = winner
            }
        } else {
            turn = nextActive(from)
        }
        revision++
    }

    /**
     * Deals the next hand after handOver. The hakem stays when their team won,
     * otherwise the role moves to the next seat (an opponent).
     */
    fun startNextHand(seed: Long = Random.nextLong()) {
        val current = phase
        if (current !is GamePhase.HandOver) throw HokmException(HokmErrorKind.INVALID_PHASE)
        if (current.winner != hakem.team) hakem = nextActive(hakem)
        handNumber++
        deal(seed)
    }

    private fun finishHand(winner: Team) {
        val loserTricks = trickCounts[winner.opponent] ?: 0
        val points = if (loserTricks == 0) {
            if (winner == hakem.team) rules.sweepPoints else rules.hakemSweepPoints
        } else {
            rules.normalHandPoints
        }
        scores[winner] = (scores[winner] ?: 0) + points
        turn = null
        phase = if ((scores[winner] ?: 0) >= rules.pointsToWinGame) {
            GamePhase.GameOver(winner)
        } else {
            GamePhase.HandOver(winner)
        }
    }

    /** Applies a [PlayerAction], throwing on an illegal move. */
    fun apply(action: PlayerAction, from: Seat, seed: Long = Random.nextLong()) {
        when (action) {
            is PlayerAction.ChooseMode -> chooseTrump(action.choice, from)
            is PlayerAction.PlayCard -> play(action.card, from)
            PlayerAction.StartNextHand -> startNextHand(seed)
            is PlayerAction.DiscardTwo -> discard(action.cards, from)
            PlayerAction.TakeCard -> resolveDraw(take = true, by = from)
            PlayerAction.RejectCard -> resolveDraw(take = false, by = from)
        }
    }

    // MARK: - Queries

    /** The cards [seat] may legally play now (empty when it is not their turn). */
    fun legalCards(seat: Seat): List<Card> {
        if (phase != GamePhase.Playing || turn != seat) return emptyList()
        val hand = hands[seat] ?: return emptyList()
        val led = currentTrick.ledSuit ?: return hand
        val following = hand.filter { it.suit == led }
        return if (following.isEmpty()) hand else following
    }
}
