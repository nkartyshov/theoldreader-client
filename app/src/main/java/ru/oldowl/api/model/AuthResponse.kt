package ru.oldowl.api.model

import com.squareup.moshi.Json

data class AuthResponse(
        @Json(name = "SID") var sid: String,
        @Json(name = "LSID") var lsid: String,
        @Json(name = "Auth") var auth: String)
