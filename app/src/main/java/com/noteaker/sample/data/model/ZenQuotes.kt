package com.noteaker.sample.data.model

class ZenQuotes : ArrayList<ZenQuotes.ZenQuotesItem>(){
    data class ZenQuotesItem(
        val a: String ,
        val h: String,
        val q: String
    )
}