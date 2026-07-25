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

    private companion object {
        const val KEY_SEEN_TUTORIAL = "hokm.settings.hasSeenTutorial"
        const val KEY_BOT_DIFFICULTY = "hokm.settings.botDifficulty"
    }
}
