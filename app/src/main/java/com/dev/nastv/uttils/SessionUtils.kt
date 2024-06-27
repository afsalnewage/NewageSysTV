package com.dev.nastv.uttils

import android.content.Context
import android.content.SharedPreferences
import com.dev.nastv.uttils.AppConstant.DEFAULT_AUTH
import com.dev.nastv.uttils.AppConstant.DEFAULT_REFRESH
import com.dev.nastv.uttils.AppConstant.PREFERENCE_NAME
import com.dev.nastv.uttils.AppConstant.PRE_AUTH_TOKEN
import com.dev.nastv.uttils.AppConstant.PRE_REFRESH_TOKEN

class SessionUtils {

    companion object {

        lateinit var preferences: SharedPreferences

        fun init(context: Context) {
            preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }


        fun saveToken(auth: String, refresh: String) {
            preferences.edit().putString(PRE_AUTH_TOKEN, auth)
                .putString(PRE_REFRESH_TOKEN, refresh).apply()
        }

        val authToken: String?
            get() = preferences.getString(PRE_AUTH_TOKEN,DEFAULT_AUTH)
        val refreshToken: String?
            get() = preferences.getString(PRE_REFRESH_TOKEN,DEFAULT_REFRESH)

        fun saveAuthToken(token: String) {
            preferences.edit().putString(PRE_AUTH_TOKEN, token).apply()
        }

        fun saveRefreshToken(token: String) {
            preferences.edit().putString(PRE_REFRESH_TOKEN, token).apply()
        }

    }
}