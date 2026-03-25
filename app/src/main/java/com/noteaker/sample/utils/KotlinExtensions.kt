package com.noteaker.sample.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


inline fun <reified T : Any> String.toKotlinObject(): T {
    val gson = Gson()
    val type = object : TypeToken<T>() {}.type
    return gson.fromJson(this, type) ?: throw Exception("Error parsing JSON")
}