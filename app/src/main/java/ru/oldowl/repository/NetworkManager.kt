package ru.oldowl.repository

import android.net.ConnectivityManager

class NetworkManager(
        private val connectivityManager: ConnectivityManager
) {
    val isNetworkUnavailable: Boolean
        get() = connectivityManager.activeNetworkInfo?.let {
            !it.isConnected
        } ?: true
}