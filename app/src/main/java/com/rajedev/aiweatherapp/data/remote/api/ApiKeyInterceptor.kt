package com.rajedev.aiweatherapp.data.remote.api

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain.request().url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .build()
        return chain.proceed(chain.request().newBuilder().url(newUrl).build())
    }
}
