package com.noteaker.sample.domain.model

import java.util.Date

data class Note(val id: Int, val title: String, val note: String, val lastUpdated: Date)