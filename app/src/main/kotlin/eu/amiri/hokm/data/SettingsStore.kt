package eu.amiri.hokm.data

import android.content.Context
import android.content.SharedPreferences
import eu.amiri.hokm.engine.BotDifficulty

/**
 * Small persisted preferences – currently just whether the tutorial has
 * already been shown. Mirrors the iOS `SettingsStore`.
 */
class SettingsStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("hokm", Context.MODE_PRIVATE)
    )

    var hasSeenTutorial: Boolean
        get() = prefs.getBoolean(KEY_SEEN_TUTORIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEN_TUTORIAL, value).apply()

    var botDifficulty: BotDifficulty
        get() = BotDifficulty.valueOf(prefs.getString(KEY_BOT_DIFFICULTY, BotDifficulty.NORMAL.name) ?: BotDifficulty.NORMAL.name)
        set(value) = prefs.edit().putString(KEY_BOT_DIFFICULTY, value.name).apply()

    var uiScale: Float
        get() = prefs.getFloat(KEY_UI_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_UI_SCALE, value).apply()

    /** Whether the in-game spotlight tutorial has run for a mode (2P/4P). */
    fun hasSeenTableCoach(playerCount: Int): Boolean =
        prefs.getBoolean(coachKey(playerCount), false)

    fun markTableCoachSeen(playerCount: Int) {
        prefs.edit().putBoolean(coachKey(playerCount), true).apply()
    }

    private fun coachKey(playerCount: Int) =
        if (playerCount == 2) KEY_COACH_2P else KEY_COACH_4P

    private companion object {
        const val KEY_SEEN_TUTORIAL = "hokm.settings.hasSeenTutorial"
        const val KEY_BOT_DIFFICULTY = "hokm.settings.botDifficulty"
        const val KEY_UI_SCALE = "hokm.settings.uiScale"
        const val KEY_COACH_2P = "hokm.settings.seenTableCoach2P"
        const val KEY_COACH_4P = "hokm.settings.seenTableCoach4P"
    }
}
