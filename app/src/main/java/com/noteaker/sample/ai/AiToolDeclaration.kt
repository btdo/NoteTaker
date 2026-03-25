package com.noteaker.sample.ai

import com.google.firebase.ai.type.FunctionDeclaration

interface AiToolDeclaration {
    val tool: FunctionDeclaration

    fun processRequest(input: Map<*, *>?)
}