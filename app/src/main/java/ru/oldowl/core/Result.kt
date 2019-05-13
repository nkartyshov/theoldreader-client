package ru.oldowl.core

class Result<out T>(val value: T? = null,
                    val exception: Throwable? = null) {

    val isSuccess get() = value != null

    val isFailure get() = exception != null

    fun getOrNull(): T? = when {
        isFailure -> null
        else -> value
    }

    fun exceptionOrNull(): Throwable? = when {
        isFailure -> value as Throwable
        else -> null
    }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)

        fun <T> failure(exception: Throwable): Result<T> = Result(exception = exception)
    }
}

inline fun <T> Result<T>.onSuccess(block: (values: T?) -> Unit): Result<T> {
    if (isSuccess) block(value)
    return this
}

inline fun <T> Result<T>.onFailure(block: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let(block)
    return this
}
