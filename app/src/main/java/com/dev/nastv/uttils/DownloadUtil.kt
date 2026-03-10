package com.dev.nastv.uttils

import android.content.Context
import android.content.SharedPreferences

object DownloadUtil {
    private const val PREF_NAME = "download_prefs"
    private const val KEY_DOWNLOADED_FILES = "downloaded_files"

    fun markFileAsDownloaded(context: Context, fileName: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean(fileName, true)
        editor.apply()
    }

    fun isFileDownloaded(context: Context, fileName: String): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(fileName, false)
    }
}
