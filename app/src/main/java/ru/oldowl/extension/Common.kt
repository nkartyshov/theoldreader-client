package ru.oldowl.extension

import java.util.*

fun Date?.afterOrEquals(date: Date?): Boolean {
    if (date == null || this == null) {
        return false
    }

    return this == date || this.after(date)
}

inline fun<T> Iterable<T>.exists(predicate: (T) -> Boolean): Boolean {
    for (item in this) {
        if (predicate.invoke(item)) {
            return true
        }
    }

    return false
}