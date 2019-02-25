package ru.oldowl

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonHelper {
    private val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    fun <T> adapter(clazz: Class<T>): JsonAdapter<T> {
        return moshi.adapter(clazz)
    }
}