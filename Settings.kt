package com.camera.ta3bia

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("recharge-settings", Context.MODE_PRIVATE)

    var dest: String
        get() = prefs.getString("dest", "555") ?: "555"
        set(v) = prefs.edit().putString("dest", v).apply()

    var endCode: String
        get() = prefs.getString("endc", "") ?: ""
        set(v) = prefs.edit().putString("endc", v).apply()

    /** "ar" or "fr" */
    var lang: String
        get() = prefs.getString("lang", Lang.AR.code) ?: Lang.AR.code
        set(v) = prefs.edit().putString("lang", v).apply()

    /** Index of the chosen interface: 0 = Zellige, 1 = Glass, 2 = Pomegranate */
    var theme: Int
        get() = prefs.getInt("theme", 1).coerceIn(0, ThemeId.values().size - 1)
        set(v) = prefs.edit().putInt("theme", v).apply()
}
