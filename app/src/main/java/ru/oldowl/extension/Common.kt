package ru.oldowl.extension

import java.util.*

fun Date?.afterOrEquals(date: Date?): Boolean {
    if (date == null || this == null) {
        return false
    }

    return this == date || this.after(date)
}

val Date.epochTime
    get() = this.time / 1000

inline fun <T> Iterable<T>.exists(predicate: (T) -> Boolean): Boolean {
    for (item in this) {
        if (predicate.invoke(item)) {
            return true
        }
    }

    return false
}