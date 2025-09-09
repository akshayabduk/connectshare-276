package org.example.app.utils

import android.content.Context
import org.example.app.data.HistoryRepository
import java.text.DateFormat
import java.util.Date
import kotlin.math.min

/**
 * SuggestionEngine creates simple AI-like suggestions using past share history and settings.
 */
object SuggestionEngine {

    // PUBLIC_INTERFACE
    fun getSuggestions(context: Context): List<String> {
        /**
         * Generate up to 5 suggestions based on recent history and user preferences.
         *
         * Parameters:
         * - context: Context for accessing storage.
         *
         * Returns: A list of suggestion strings.
         */
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_AI, true)
        if (!enabled) return emptyList()

        val repo = HistoryRepository(context)
        val history = repo.getHistory().sortedByDescending { it.time }

        if (history.isEmpty()) {
            return listOf("Try sharing photos with friends via Bluetooth",
                "Invite a nearby device and start chatting",
                "Share multiple files over Wi‑Fi")
        }

        val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val pick = min(5, history.size)
        val out = mutableListOf<String>()
        for (i in 0 until pick) {
            val h = history[i]
            out.add("Share ${h.fileName} again via ${h.method} (${df.format(Date(h.time))})")
        }
        return out
    }

    private const val PREFS = "connectshare_prefs"
    private const val KEY_AI = "enable_ai"
}
