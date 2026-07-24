package eu.amiri.hokm.engine

import kotlinx.serialization.Serializable

/**
 * What the hakem declared for this hand. Besides the four trump suits the
 * hakem may always call "high" (normal ranking, no trump) or "low" (inverted
 * ranking, no trump – the two beats the ace).
 */
@Serializable
sealed interface TrumpChoice {
    @Serializable
    data class OfSuit(override val suit: Suit) : TrumpChoice

    @Serializable
    data object High : TrumpChoice

    @Serializable
    data object Low : TrumpChoice

    /** The trump suit, when one exists (classic variant only). */
    val suit: Suit? get() = null

    /** True when the two is the strongest card (inverted ranking). */
    val isInverted: Boolean get() = this == Low
}

/**
 * Configurable scoring rules (common Iranian house rules): a hand is won with
 * 7 tricks worth 1 point, a 7–0 sweep ("kut") is worth 2, sweeping the hakem's
 * team ("hakem kut") is worth 3. First team to 7 points wins.
 */
@Serializable
data class HokmRules(
    val tricksToWinHand: Int = 7,
    val pointsToWinGame: Int = 7,
    val normalHandPoints: Int = 1,
    val sweepPoints: Int = 2,
    val hakemSweepPoints: Int = 3,
    /** 2 (head-to-head with draw phase) or 4 (classic partners). */
    val playerCount: Int = 4,
)

/**
 * Two-player draw turn outcome: both cards are shown openly to the drawer –
 * nothing is discarded or taken "blind".
 */
@Serializable
data class DrawResult(
    val seat: Seat,
    val taken: Card,
    val discarded: Card,
    /** True when the revealed card was taken (and the next one thrown away). */
    val tookRevealed: Boolean,
)

/** Reasons an action can violate the rules. */
enum class HokmErrorKind { INVALID_PHASE, NOT_HAKEM, NOT_YOUR_TURN, CARD_NOT_IN_HAND, MUST_FOLLOW_SUIT }

/** Thrown when an action violates the rules. */
class HokmException(val kind: HokmErrorKind) : Exception(kind.name)

/** The lifecycle of a game. */
@Serializable
sealed interface GamePhase {
    @Serializable
    data object ChoosingTrump : GamePhase

    /** Two-player only: both players discard 2 of their first 4 cards. */
    @Serializable
    data object Discarding : GamePhase

    /** Two-player only: alternating draw turns until the stock is empty. */
    @Serializable
    data object Drawing : GamePhase

    /** Trick play is in progress. */
    @Serializable
    data object Playing : GamePhase

    /** A hand ended; waiting to deal the next one. */
    @Serializable
    data class HandOver(val winner: Team) : GamePhase

    /** A team reached the required points. */
    @Serializable
    data class GameOver(val winner: Team) : GamePhase
}

/**
 * An action a player wants to perform. Applied to [HokmGame] which validates
 * it against the authoritative state.
 */
sealed interface PlayerAction {
    /** Variant-aware declaration (trump suit, or high/low). */
    data class ChooseMode(val choice: TrumpChoice) : PlayerAction
    data class PlayCard(val card: Card) : PlayerAction
    data object StartNextHand : PlayerAction
    /** Two-player: discard two of the first four cards. */
    data class DiscardTwo(val cards: List<Card>) : PlayerAction
    /** Two-player draw turn: take the revealed card. */
    data object TakeCard : PlayerAction
    /** Two-player draw turn: reject it and take the next one. */
    data object RejectCard : PlayerAction
}
