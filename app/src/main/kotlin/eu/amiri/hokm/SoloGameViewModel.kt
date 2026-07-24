package eu.amiri.hokm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.amiri.hokm.engine.AceDraw
import eu.amiri.hokm.engine.BotDifficulty
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.GamePhase
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.HokmBot
import eu.amiri.hokm.engine.HokmException
import eu.amiri.hokm.engine.HokmGame
import eu.amiri.hokm.engine.HokmRules
import eu.amiri.hokm.engine.PlayerAction
import eu.amiri.hokm.engine.Seat
import eu.amiri.hokm.engine.TrumpChoice
import eu.amiri.hokm.engine.snapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Runs a local game against bots and exposes the human's [GameSnapshot] as
 * Compose state. The human always sits [Seat.SOUTH]; the other seats are bots.
 * Mirrors the iOS `SoloTransport`.
 */
class SoloGameViewModel : ViewModel() {
    private val humanSeat = Seat.SOUTH
    private var game: HokmGame? = null
    private var difficulty = BotDifficulty.NORMAL
    private var botJob: Job? = null

    var snapshot by mutableStateOf<GameSnapshot?>(null)
        private set
    var started by mutableStateOf(false)
        private set

    fun newGame(players: Int, diff: BotDifficulty) {
        botJob?.cancel()
        difficulty = diff
        val seats = if (players == 2) listOf(Seat.SOUTH, Seat.WEST) else Seat.entries.toList()
        val draw = AceDraw.draw(seats)
        game = HokmGame(firstHakem = draw.hakem, rules = HokmRules(playerCount = players))
        started = true
        publish()
        runBots()
    }

    fun quit() {
        botJob?.cancel()
        started = false
        snapshot = null
        game = null
    }

    fun play(card: Card) = safeApply(PlayerAction.PlayCard(card))
    fun chooseMode(choice: TrumpChoice) = safeApply(PlayerAction.ChooseMode(choice))
    fun discard(cards: List<Card>) = safeApply(PlayerAction.DiscardTwo(cards))
    fun takeCard() = safeApply(PlayerAction.TakeCard)
    fun rejectCard() = safeApply(PlayerAction.RejectCard)
    fun nextHand() = safeApply(PlayerAction.StartNextHand)

    private fun safeApply(action: PlayerAction) {
        val g = game ?: return
        try {
            g.apply(action, from = humanSeat)
        } catch (_: HokmException) {
            // Illegal tap (e.g. double press): just re-publish the truth.
        }
        publish()
        runBots()
    }

    private fun runBots() {
        botJob?.cancel()
        botJob = viewModelScope.launch {
            while (true) {
                val g = game ?: break
                if (g.phase == GamePhase.Discarding) {
                    val seat = g.pendingDiscards.firstOrNull { it != humanSeat } ?: break
                    val action = HokmBot.nextAction(g.snapshot(seat), difficulty) ?: break
                    delay(600)
                    g.apply(action, from = seat)
                    publish()
                    continue
                }
                val seat = g.turn ?: break
                if (seat == humanSeat) break
                val action = HokmBot.nextAction(g.snapshot(seat), difficulty) ?: break
                // In the 2-player draw phase let the thrown/taken card linger.
                delay(if (g.phase == GamePhase.Drawing) 1500 else 700)
                g.apply(action, from = seat)
                publish()
            }
        }
    }

    private fun publish() {
        snapshot = game?.snapshot(humanSeat)
    }
}
