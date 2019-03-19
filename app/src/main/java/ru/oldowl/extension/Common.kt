package ru.oldowl.extension

import java.util.*

fun Date?.afterOrEquals(date: Date?): Boolean {
    if (date == null || this == null) {
        return false
    }

    return this == date || this.after(date)
}