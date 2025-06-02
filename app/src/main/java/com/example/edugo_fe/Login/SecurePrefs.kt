package com.example.edugo_fe.Login

import android.content.Context
import androidx.core.content.edit

object SecurePrefs {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"

    fun saveAccessToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_ACCESS_TOKEN, token)
            apply()
        }
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearAccessToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_ACCESS_TOKEN)
            apply()
        }
    }
}