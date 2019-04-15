package ru.oldowl.db.model

import ru.oldowl.JsonHelper

data class Account(var email: String, var password: String, var authToken: String) {

    fun toJson(): String {

        return JsonHelper
                .adapter(Account::class.java)
                .nullSafe()
                .toJson(this)
    }
}