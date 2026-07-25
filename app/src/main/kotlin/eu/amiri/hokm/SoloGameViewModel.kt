package eu.amiri.hokm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.amiri.hokm.data.BotNames
import eu.amiri.hokm.data.SavedGame
import eu.amiri.hokm.data.SavedGameStore
import eu.amiri.hokm.data.SettingsStore
import eu.amiri.hokm.data.StatsStore
import eu.amiri.hokm.engine.*
import eu.amiri.hokm.ui.De
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs a local game against bots and exposes the human's [GameSnapshot] as
 * Compose state. The human always sits [Seat.SOUTH]; the other seats are bots.
 *
 * Mirrors the iOS `SoloTransport` + `GameSession`: it also keeps the running
 * game persisted (so it can be resumed) and records the statistics.
 */
class SoloGameViewModel(app: Application) : AndroidViewModel(app) {
    private val humanSeat = Seat.SOUTH
    private val statsStore = StatsStore(app)
    private val saveStore = SavedGameStore(app)
    private val settings = SettingsStore(app)

    private var game: HokmGame? = null
    var botDifficulty by mutableStateOf(settings.botDifficulty)
        private set
    private var botJob: Job? = null

    // Per-game counters for the statistics, mirroring iOS `GameSession`.
    private var handsWonThisGame = 0
    private var sweepsThisGame = 0
    private val recordedHands = mutableSetOf<Int>()
    private var gameRecorded = false

    var snapshot by mutableStateOf<GameSnapshot?>(value = null)
        private set
    var started by mutableStateOf(value = false)
        private set
    var paused by mutableStateOf(value = false)
        private set
    var stats by mutableStateOf(value = statsStore.stats)
        private set
    var canResume by mutableStateOf(value = saveStore.hasSavedGame)
        private set

    /**
     * Display name per seat, including our own – the same shape the iOS
     * `SoloTransport` sends in its welcome message. The HUD reads it so the
     * hakem pill can say "Bot Darya" instead of a bare "Gegner".
     */
    var names by mutableStateOf<Map<Seat, String>>(emptyMap())
        private set

    fun name(of: Seat): String = names[of] ?: De.OPPONENTS

    /** Fresh random bot names for every seat that is not ours, as on iOS. */
    private fun assignNames(seats: List<Seat>) {
        val botSeats = seats.filter { it != humanSeat }
        names = buildMap {
            put(humanSeat, De.YOU)
            putAll(botSeats.zip(BotNames.random(botSeats.size)))
        }
    }

    fun hasSeenTableCoach(playerCount: Int) = settings.hasSeenTableCoach(playerCount)

    fun markTableCoachSeen(playerCount: Int) = settings.markTableCoachSeen(playerCount)

    /** True until the tutorial has been seen once – it then opens on first launch. */
    var needsTutorial by mutableStateOf(!settings.hasSeenTutorial)
        private set

    fun tutorialSeen() {
        settings.hasSeenTutorial = true
        needsTutorial = false
    }

    fun updateBotDifficulty(value: BotDifficulty) {
        settings.botDifficulty = value
        botDifficulty = value
    }

    fun newGame(players: Int) {
        botJob?.cancel()
        handsWonThisGame = 0
        sweepsThisGame = 0
        recordedHands.clear()
        gameRecorded = false

        val seats = if (players == 2) listOf(Seat.SOUTH, Seat.WEST) else Seat.entries.toList()
        assignNames(seats)
        val draw = AceDraw.draw(seats)
        game = HokmGame(firstHakem = draw.hakem, rules = HokmRules(playerCount = players))
        started = true
        paused = false
        publish()
        runBots()
    }

    /** Continues the game that was saved when the player last left the table. */
    fun resumeSavedGame() {
        val saved = saveStore.load() ?: run {
            canResume = false
            return
        }
        botJob?.cancel()
        botDifficulty = saved.difficulty
        handsWonThisGame = saved.handsWonThisGame
        sweepsThisGame = saved.sweepsThisGame
        recordedHands.clear()
        recordedHands.addAll(saved.recordedHands)
        gameRecorded = false

        val restored = HokmGame(saved.state)
        game = restored
        // Keep the opponents the game was left with. Saves written before the
        // names existed (or a partial map) fall back to fresh ones, the way
        // iOS re-rolls them on every start.
        val savedNames = saved.botNames.mapNotNull { (seat, name) ->
            runCatching { Seat.valueOf(seat) }.getOrNull()?.let { it to name }
        }.toMap()
        val activeSeats = restored.activeSeats
        if (activeSeats.all { it == humanSeat || it in savedNames }) {
            names = savedNames + (humanSeat to De.YOU)
        } else {
            assignNames(activeSeats)
        }

        started = true
        paused = false
        publish()
        runBots()
    }

    fun discardSavedGame() {
        saveStore.clear()
        canResume = false
    }

    /** Leaves the table; the game itself stays saved and can be resumed. */
    fun leaveTable() {
        botJob?.cancel()
        persist()
        started = false
        paused = false
        snapshot = null
        game = null
    }

    fun pause() {
        botJob?.cancel()
        paused = true
        persist()
    }

    fun resume() {
        paused = false
        runBots()
    }

    fun resetStats() {
        statsStore.reset()
        stats = statsStore.stats
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
        if (paused) return
        botJob = viewModelScope.launch {
            while (true) {
                val g = game ?: break
                if (g.phase == GamePhase.Discarding) {
                    val seat = g.pendingDiscards.firstOrNull { it != humanSeat } ?: break
                    val action = HokmBot.nextAction(g.snapshot(seat), botDifficulty) ?: break
                    delay(BOT_DELAY_MS.milliseconds)
                    g.apply(action, from = seat)
                    publish()
                    continue
                }
                val seat = g.turn ?: break
                if (seat == humanSeat) break
                val action = HokmBot.nextAction(g.snapshot(seat), botDifficulty) ?: break
                // In the two-player draw phase the bot pauses longer so the
                // player can actually read which card was just thrown away.
                delay(if (g.phase == GamePhase.Drawing) DRAW_DELAY_MS.milliseconds else BOT_DELAY_MS.milliseconds)
                g.apply(action, from = seat)
                publish()
            }
        }
    }

    private fun publish() {
        val snap = game?.snapshot(humanSeat)
        snapshot = snap
        snap?.let { trackStatistics(it) }
        persist()
    }

    /** Saves the current state – or clears the slot once the game is over. */
    private fun persist() {
        val g = game ?: return
        if (g.phase is GamePhase.GameOver) {
            saveStore.clear()
            canResume = false
        } else {
            saveStore.save(
                SavedGame(
                    state = g.state(),
                    difficulty = botDifficulty,
                    handsWonThisGame = handsWonThisGame,
                    sweepsThisGame = sweepsThisGame,
                    recordedHands = recordedHands.toList(),
                    // Only the bots; our own name is a UI string, not state.
                    botNames = names.filterKeys { it != humanSeat }.mapKeys { it.key.name },
                ),
            )
            canResume = true
        }
    }

    private fun trackStatistics(snap: GameSnapshot) {
        when (val phase = snap.phase) {
            is GamePhase.HandOver -> recordHand(snap.handNumber, phase.winner, snap)
            is GamePhase.GameOver -> {
                // The game-winning hand skips `handOver`, so count it here.
                recordHand(snap.handNumber, phase.winner, snap)
                if (!gameRecorded) {
                    gameRecorded = true
                    statsStore.recordGame(
                        won = phase.winner == snap.myTeam,
                        handsWon = handsWonThisGame,
                        sweeps = sweepsThisGame,
                    )
                    stats = statsStore.stats
                }
            }
            else -> Unit
        }
    }

    private fun recordHand(number: Int, winner: Team, snap: GameSnapshot) {
        if (!recordedHands.add(number)) return
        if (winner != snap.myTeam) return
        handsWonThisGame++
        if ((snap.trickCounts[winner.opponent] ?: 0) == 0) sweepsThisGame++
    }

    private companion object {
        /** Bot thinking time, matching the iOS `SoloTransport`. */
        const val BOT_DELAY_MS = 700L
        const val DRAW_DELAY_MS = 2500L
    }
}
