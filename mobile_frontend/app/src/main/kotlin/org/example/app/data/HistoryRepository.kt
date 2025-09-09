package org.example.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * HistoryRepository persists and retrieves sharing history.
 */
class HistoryRepository(private val context: Context) {

    // PUBLIC_INTERFACE
    fun addShareRecord(fileName: String, method: String, timestamp: Long) {
        /**
         * Append a new share record to the stored history.
         *
         * Parameters:
         * - fileName: Display name of the shared file.
         * - method: Transport method (e.g., "Bluetooth" or "Wi‑Fi").
         * - timestamp: When the share happened (epoch millis).
         */
        val arr = loadArray()
        val obj = JSONObject()
            .put("fileName", fileName)
            .put("method", method)
            .put("time", timestamp)
        arr.put(obj)
        saveArray(arr)
    }

    // PUBLIC_INTERFACE
    fun getHistory(): List<ShareHistoryItem> {
        /**
         * Load and parse all history records.
         *
         * Returns: List of ShareHistoryItem entries.
         */
        val arr = loadArray()
        val out = mutableListOf<ShareHistoryItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                ShareHistoryItem(
                    o.optString("fileName"),
                    o.optString("method"),
                    o.optLong("time")
                )
            )
        }
        return out
    }

    // PUBLIC_INTERFACE
    fun clear() {
        /**
         * Clear all stored history records.
         */
        saveArray(JSONArray())
    }

    private fun loadArray(): JSONArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val s = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        return JSONArray(s)
    }

    private fun saveArray(arr: JSONArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    data class ShareHistoryItem(
        val fileName: String,
        val method: String,
        val time: Long
    )

    companion object {
        private const val PREFS = "connectshare_prefs"
        private const val KEY_HISTORY = "history"
    }
}
