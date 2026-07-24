package eu.amiri.hokm.data

import android.content.Context
import android.content.SharedPreferences
import eu.amiri.hokm.engine.BotDifficulty
import eu.amiri.hokm.engine.GameState
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A suspended solo game: the full engine state plus what the session needs to
 * pick up exactly where it left off (bot strength, counters for the running
 * game's statistics).
 */
@Serializable
data class SavedGame(
    val state: GameState,
    val difficulty: BotDifficulty = BotDifficulty.NORMAL,
    val handsWonThisGame: Int = 0,
    val sweepsThisGame: Int = 0,
    /** Hand numbers already counted, so a resume never double-counts them. */
    val recordedHands: List<Int> = emptyList(),
)

/**
 * Persists the running solo game so it can be resumed after leaving the table
 * or even restarting the app. Mirrors the iOS `SavedGameStore`.
 */
class SavedGameStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("hokm", Context.MODE_PRIVATE)
    )

    var hasSavedGame: Boolean = prefs.contains(KEY)
        private set

    fun load(): SavedGame? {
        val raw = prefs.getString(KEY, null) ?: return null
        return runCatching { Json.decodeFromString<SavedGame>(raw) }.getOrElse {
            // Incompatible or corrupt save (e.g. after an app update): discard.
            clear()
            null
        }
    }

    fun save(game: SavedGame) {
        prefs.edit().putString(KEY, Json.encodeToString(game)).apply()
        hasSavedGame = true
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
        hasSavedGame = false
    }

    private companion object {
        const val KEY = "hokm.savedSoloGame.v1"
    }
}
