package eu.amiri.hokm.engine

import kotlinx.serialization.Serializable

/** The four French suits used in Hokm. */
@Serializable
enum class Suit(val symbol: String) {
    SPADES("♠"),
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣");

    val isRed: Boolean get() = this == HEARTS || this == DIAMONDS
}

/** Card ranks; ace is high in Hokm. The numeric [value] is 2..14. */
@Serializable
enum class Rank(val value: Int) {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14);

    val symbol: String
        get() = when (this) {
            ACE -> "A"; KING -> "K"; QUEEN -> "Q"; JACK -> "J"
            else -> value.toString()
        }
}

/** A single playing card. */
@Serializable
data class Card(val rank: Rank, val suit: Suit) {
    val id: String get() = "${rank.value}-${suit.name.lowercase()}"
    override fun toString(): String = "${rank.symbol}${suit.symbol}"

    /** Whether this card beats [other] given the trump and the suit that was led. */
    fun beats(other: Card, trump: Suit, ledSuit: Suit): Boolean =
        beats(other, TrumpChoice.OfSuit(trump), ledSuit)

    /**
     * Variant-aware comparison: classic trump, "high" (no trump, normal
     * ranking) or "low" (no trump, the two is the strongest card).
     */
    fun beats(other: Card, mode: TrumpChoice, ledSuit: Suit): Boolean = when (mode) {
        is TrumpChoice.OfSuit -> when {
            suit == other.suit -> rank.value > other.rank.value
            suit == mode.suit -> true
            other.suit == mode.suit -> false
            suit == ledSuit -> true
            else -> false
        }
        TrumpChoice.High ->
            if (suit == other.suit) rank.value > other.rank.value
            else suit == ledSuit && other.suit != ledSuit
        TrumpChoice.Low ->
            if (suit == other.suit) rank.value < other.rank.value
            else suit == ledSuit && other.suit != ledSuit
    }
}

/** Helpers for building and ordering the deck. */
object Deck {
    /** All 52 cards. */
    val full: List<Card>
        get() = Suit.entries.flatMap { suit -> Rank.entries.map { Card(it, suit) } }
}

/**
 * Sorting used to present a hand nicely in the UI: the trump suit comes first,
 * and red/black suit blocks alternate whenever possible so the hand stays
 * easy to read.
 */
object CardSorting {
    val defaultSuitOrder = listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS)

    fun displayOrder(cards: List<Card>, trump: Suit? = null): List<Card> {
        val groups: Map<Suit, List<Card>> = cards.groupBy { it.suit }
            .mapValues { (_, v) -> v.sortedByDescending { it.rank.value } }

        val remaining = defaultSuitOrder.filter { groups[it] != null }.toMutableList()
        val orderedSuits = mutableListOf<Suit>()

        if (trump != null) {
            val i = remaining.indexOf(trump)
            if (i >= 0) orderedSuits.add(remaining.removeAt(i))
        }
        while (remaining.isNotEmpty()) {
            val previousIsRed = orderedSuits.lastOrNull()?.isRed
            val index = remaining.indexOfFirst { previousIsRed == null || it.isRed != previousIsRed }
                .let { if (it < 0) 0 else it }
            orderedSuits.add(remaining.removeAt(index))
        }
        return orderedSuits.flatMap { groups[it] ?: emptyList() }
    }
}
