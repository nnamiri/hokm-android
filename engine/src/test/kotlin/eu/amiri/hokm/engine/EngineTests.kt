package eu.amiri.hokm.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EngineTests {

    @Test fun deckHas52UniqueCards() {
        val deck = Deck.full
        assertEquals(52, deck.size)
        assertEquals(52, deck.toSet().size)
    }

    @Test fun fourPlayerDealGivesFiveThenThirteen() {
        val game = HokmGame(firstHakem = Seat.SOUTH, seed = 42)
        assertEquals(GamePhase.ChoosingTrump, game.phase)
        for (seat in Seat.entries) assertEquals(5, game.handOf(seat).size)

        game.chooseTrump(TrumpChoice.OfSuit(Suit.SPADES), by = Seat.SOUTH)
        assertEquals(GamePhase.Playing, game.phase)
        for (seat in Seat.entries) assertEquals(13, game.handOf(seat).size)
    }

    @Test fun sameSeedProducesSameDeal() {
        val a = HokmGame(seed = 123)
        val b = HokmGame(seed = 123)
        for (seat in Seat.entries) assertEquals(a.handOf(seat), b.handOf(seat))
    }

    @Test fun onlyTheHakemMayChooseTrump() {
        val game = HokmGame(firstHakem = Seat.SOUTH, seed = 1)
        val e = assertFailsWith<HokmException> {
            game.chooseTrump(TrumpChoice.High, by = Seat.WEST)
        }
        assertEquals(HokmErrorKind.NOT_HAKEM, e.kind)
    }

    @Test fun mustFollowSuitIsEnforced() {
        // South must follow hearts; a spade is illegal while it holds a heart.
        val hands = mapOf(
            Seat.SOUTH to listOf(Card(Rank.TWO, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
            Seat.WEST to listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.THREE, Suit.CLUBS)),
            Seat.NORTH to listOf(Card(Rank.FOUR, Suit.HEARTS), Card(Rank.FIVE, Suit.CLUBS)),
            Seat.EAST to listOf(Card(Rank.SIX, Suit.HEARTS), Card(Rank.SEVEN, Suit.CLUBS)),
        )
        val game = HokmGame(testHands = hands, hakem = Seat.SOUTH, trump = Suit.SPADES)
        // South leads a heart.
        game.play(Card(Rank.KING, Suit.HEARTS), from = Seat.SOUTH)
        val e = assertFailsWith<HokmException> {
            // West holds a heart but tries a club.
            game.play(Card(Rank.THREE, Suit.CLUBS), from = Seat.WEST)
        }
        assertEquals(HokmErrorKind.MUST_FOLLOW_SUIT, e.kind)
    }

    @Test fun trumpBeatsHigherLedCard() {
        val trump = Card(Rank.TWO, Suit.SPADES)
        val ledAce = Card(Rank.ACE, Suit.HEARTS)
        assertTrue(trump.beats(ledAce, trump = Suit.SPADES, ledSuit = Suit.HEARTS))
        assertTrue(!ledAce.beats(trump, trump = Suit.SPADES, ledSuit = Suit.HEARTS))
    }

    @Test fun lowModeInvertsRanking() {
        val two = Card(Rank.TWO, Suit.HEARTS)
        val ace = Card(Rank.ACE, Suit.HEARTS)
        assertTrue(two.beats(ace, TrumpChoice.Low, ledSuit = Suit.HEARTS))
        assertTrue(!ace.beats(two, TrumpChoice.Low, ledSuit = Suit.HEARTS))
    }

    @Test fun twoPlayerDealsFourCardsAndAStock() {
        val rules = HokmRules(playerCount = 2)
        val game = HokmGame(firstHakem = Seat.SOUTH, rules = rules, seed = 7)
        assertEquals(4, game.handOf(Seat.SOUTH).size)
        assertEquals(4, game.handOf(Seat.WEST).size)
        // 52 - 8 dealt = 44 in the stock.
        assertEquals(44, game.stockCount)
    }

    @Test fun botsPlayAFullFourPlayerHandToCompletion() {
        val game = HokmGame(firstHakem = Seat.SOUTH, seed = 99)
        var guard = 0
        while (game.phase == GamePhase.ChoosingTrump || game.phase == GamePhase.Playing) {
            val seat = game.turn ?: break
            val action = HokmBot.nextAction(game.snapshot(seat), BotDifficulty.HARD) ?: break
            game.apply(action, from = seat)
            if (++guard > 500) break
        }
        val phase = game.phase
        assertTrue(phase is GamePhase.HandOver || phase is GamePhase.GameOver, "hand should end, was $phase")
        val winner = when (phase) {
            is GamePhase.HandOver -> phase.winner
            is GamePhase.GameOver -> phase.winner
            else -> error("unreachable")
        }
        assertTrue((game.trickCounts()[winner] ?: 0) >= 7, "winner should hold at least 7 tricks")
    }
}
