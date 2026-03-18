package com.noteaker.sample.data.repository

import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.data.network.ZenQuotesApi
import javax.inject.Inject

interface QuoteRepository {
       suspend fun getQuote(): ZenQuotes
}

class MyQuoteRepository @Inject constructor (
    private val api: ZenQuotesApi
) : QuoteRepository {
    override suspend fun getQuote(): ZenQuotes = api.quote()
}