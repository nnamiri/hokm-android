package eu.amiri.hokm.engine

import kotlinx.serialization.Serializable

/** One trick: up to four cards played in seat order starting at [leader]. */
class Trick(val leader: Seat) {
    @Serializable
    data class Play(val seat: Seat, val card: Card)

    private val _plays = mutableListOf<Play>()
    val plays: List<Play> get() = _plays

    val ledSuit: Suit? get() = _plays.firstOrNull()?.card?.suit

    val isComplete: Boolean get() = _plays.size == 4

    /** The seat that has to play next, or null when the trick is complete. */
    val nextSeat: Seat?
        get() {
            if (isComplete) return null
            var seat = leader
            repeat(_plays.size) { seat = seat.next }
            return seat
        }

    fun add(card: Card, from: Seat) {
        _plays.add(Play(from, card))
    }

    /** An independent copy, so a snapshot never aliases the live trick. */
    fun copyOf(): Trick {
        val t = Trick(leader)
        for (p in _plays) t.add(p.card, p.seat)
        return t
    }

    /** Classic-trump winner (highest trump, else highest card of the led suit). */
    fun winningPlay(trump: Suit): Play? = winningPlay(TrumpChoice.OfSuit(trump))

    /** Variant-aware winner: classic trump, or high/low without a trump suit. */
    fun winningPlay(mode: TrumpChoice): Play? {
        if (ledSuit == null) return null
        val led = ledSuit!!
        var best = _plays[0]
        for (play in _plays.drop(1)) {
            if (play.card.beats(best.card, mode, led)) best = play
        }
        return best
    }
}
