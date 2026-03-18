package com.noteaker.sample.data.network

import com.noteaker.sample.data.model.ZenQuotes
import retrofit2.http.GET

interface ZenQuotesApi {
    @GET("/api/random")
    suspend fun quote(): ZenQuotes
}