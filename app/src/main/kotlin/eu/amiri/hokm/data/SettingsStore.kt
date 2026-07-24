package eu.amiri.hokm.data

import android.content.Context
import android.content.SharedPreferences

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

    private companion object {
        const val KEY_SEEN_TUTORIAL = "hokm.settings.hasSeenTutorial"
    }
}
