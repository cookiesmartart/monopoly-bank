package com.github.cookiesmartart.monopolybank.locale

import android.content.Context

private const val PREFS_NAME = "locale_prefs"
private const val KEY_LANGUAGE_TAG = "language_tag"

/**
 * Stores the user's in-app language override as a plain synchronous SharedPreferences value
 * (not DataStore) on purpose: [android.app.Activity.attachBaseContext] runs before any coroutine
 * can complete, so the language tag must be readable synchronously at that point. `null` means
 * "follow the system language".
 */
object LocalePrefs {
    fun getLanguageTag(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LANGUAGE_TAG, null)

    fun setLanguageTag(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (tag == null) remove(KEY_LANGUAGE_TAG) else putString(KEY_LANGUAGE_TAG, tag)
        }.apply()
    }
}
