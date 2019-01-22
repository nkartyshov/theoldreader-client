package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager

class SettingsService(private val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
}