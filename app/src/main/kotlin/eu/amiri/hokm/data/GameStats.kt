package eu.amiri.hokm.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.roundToInt

/** Persisted counters across all games played on this device. */
@Serializable
data class GameStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val handsWon: Int = 0,
    /** Kut / Hakem-Kut achieved by the player's team. */
    val sweeps: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
) {
    val gamesLost: Int get() = max(0, gamesPlayed - gamesWon)

    val winPercent: Int
        get() = if (gamesPlayed == 0) 0 else (gamesWon.toDouble() / gamesPlayed * 100).roundToInt()
}

/**
 * Loads and persists [GameStats] in `SharedPreferences` – the Android twin of
 * the iOS `StatsStore` (which uses `UserDefaults`).
 */
class StatsStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("hokm", Context.MODE_PRIVATE)
    )

    var stats: GameStats = load()
        private set

    /** Records the outcome of one completed game (first team to the point target). */
    fun recordGame(won: Boolean, handsWon: Int, sweeps: Int) {
        val streak = if (won) stats.currentStreak + 1 else 0
        stats = stats.copy(
            gamesPlayed = stats.gamesPlayed + 1,
            gamesWon = stats.gamesWon + if (won) 1 else 0,
            handsWon = stats.handsWon + handsWon,
            sweeps = stats.sweeps + sweeps,
            currentStreak = streak,
            bestStreak = max(stats.bestStreak, streak),
        )
        save()
    }

    fun reset() {
        stats = GameStats()
        save()
    }

    private fun load(): GameStats {
        val raw = prefs.getString(KEY, null) ?: return GameStats()
        return runCatching { Json.decodeFromString<GameStats>(raw) }.getOrElse { GameStats() }
    }

    private fun save() {
        prefs.edit().putString(KEY, Json.encodeToString(stats)).apply()
    }

    private companion object {
        const val KEY = "hokm.stats.v1"
    }
}
