package com.noteaker.sample.data.model

data class SyncResult (
    val success: Boolean,
    val pushedCount: Int,
    val pulledCount: Int,
    val conflictsCount: Int,
    val errors: List<String> = emptyList()
)