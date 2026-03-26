package com.noteaker.sample.fakes

import com.noteaker.sample.data.model.ZenQuotes
import com.noteaker.sample.data.repository.QuoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Avoids network in tests when [com.noteaker.sample.di.RepositoryModule] is replaced. */
@Singleton
class FakeQuoteRepository @Inject constructor() : QuoteRepository {
    override suspend fun getQuote(): ZenQuotes =
        ZenQuotes().apply {
            add(ZenQuotes.ZenQuotesItem(a = "author", h = "hint", q = "Instrumented quote"))
        }
}
