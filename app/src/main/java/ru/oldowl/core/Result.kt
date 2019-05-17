package ru.oldowl.core

class Result<out T>(val value: T? = null,
                    val exception: Throwable? = null) {

    val isSuccess get() = value != null

    private val isFailure get() = exception != null

    fun getOrNull(): T? = when {
        isFailure -> null
        else -> value
    }

    fun exceptionOrNull(): Throwable? = when {
        isFailure -> exception as Throwable
        else -> null
    }

    inline fun onComplete(block: Result<T>.() -> Unit): Result<T> {
        block(this)
        return this
    }

    inline fun onSuccess(block: (values: T?) -> Unit): Result<T> {
        if (isSuccess) block(value)
        return this
    }

    inline fun onFailure(block: (exception: Throwable) -> Unit): Result<T> {
        exceptionOrNull()?.let(block)
        return this
    }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun empty(): Result<Unit> = success(Unit)

        fun <T> failure(exception: Throwable): Result<T> = Result(exception = exception)
        fun <T> failure(message: String): Result<T> = failure(Exception(message))
    }
}

