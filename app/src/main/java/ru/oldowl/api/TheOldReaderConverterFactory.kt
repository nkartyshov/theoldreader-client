package ru.oldowl.api

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.StringReader
import java.lang.reflect.Type

class TheOldReaderConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
            type: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): Converter<ResponseBody, *>? {

        return if (type == SyndFeed::class.java) {
            SyndFeedConverter()
        } else {
            val delegate = retrofit.nextResponseBodyConverter<Any?>(this, type, annotations)
            OkResponseBodyConverter(delegate)
        }
    }

    class SyndFeedConverter : Converter<ResponseBody, SyndFeed> {
        override fun convert(value: ResponseBody): SyndFeed? {
            if (value.contentLength() < 0) {
                return null
            }

            return SyndFeedInput().build(StringReader(value.string()))
        }
    }

    class OkResponseBodyConverter(
            private val delegate: Converter<ResponseBody, Any?>
    ) : Converter<ResponseBody, Any?> {

        override fun convert(value: ResponseBody): Any? = if (value.contentLength() < 2) delegate.convert(value) else null
    }
}