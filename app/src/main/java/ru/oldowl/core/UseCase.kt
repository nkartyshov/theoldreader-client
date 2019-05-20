package ru.oldowl.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import kotlin.coroutines.CoroutineContext

abstract class UseCase<P, out R> : CoroutineScope, AnkoLogger {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    abstract suspend fun run(param: P): Result<R>

    operator fun invoke(param: P, onResult: Result<R>.() -> Unit) {
        val deferred = async { run(param) }

        launch(Dispatchers.Main) {
            val result = try {
                deferred.await()
            } catch (e: Throwable) {
                error("Error", e)
                Result.failure<R>(e)
            }

            onResult(result)
        }
    }
}