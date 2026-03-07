package com.noteaker.sample.ui.model

sealed class UIState {
    object Loading : UIState()
    data class Error(val message: String) : UIState()
    data class Success<T>(val data: T) : UIState()
}