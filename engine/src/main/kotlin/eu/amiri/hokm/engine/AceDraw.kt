package eu.amiri.hokm.engine

import kotlin.random.Random

/**
 * The traditional draw for the first hakem: cards are dealt around the table
 * one by one, and whoever receives the first ace begins. The full reveal
 * sequence is kept so the UI can animate it.
 */
data class AceDraw(val reveals: List<Reveal>, val hakem: Seat) {
    data class Reveal(val seat: Seat, val card: Card)

    companion object {
        /**
         * Deals from a shuffled deck to the given seats in turn until the first
         * ace appears. Deterministic for a given [seed].
         */
        fun draw(seats: List<Seat>, seed: Long = Random.nextLong()): AceDraw {
            val rng = SeededGenerator(seed)
            val deck = Deck.full.shuffled(rng)
            val reveals = mutableListOf<Reveal>()
            var index = 0
            for (card in deck) {
                val seat = seats[index % seats.size]
                reveals.add(Reveal(seat, card))
                if (card.rank == Rank.ACE) return AceDraw(reveals, seat)
                index++
            }
            return AceDraw(reveals, seats[0])
        }
    }
}
