package ru.oldowl.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

abstract class UseCase<P, out R> : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    abstract suspend fun run(param: P): R

    operator fun invoke(param: P, onResult: (Result<R>) -> Unit) {
        try {
            val deferred = async { run(param) }

            launch(Dispatchers.Main) {
                val result = Result.success(deferred.await())
                onResult(result)
            }
        } catch (e: Exception) {
            Result.failure<R>(e)
        }
    }
}