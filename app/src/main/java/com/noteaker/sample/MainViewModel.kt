package com.noteaker.sample

import androidx.lifecycle.ViewModel
import com.noteaker.sample.model.Note
import com.noteaker.sample.navigation.TopBarItem
import com.noteaker.sample.repository.MyNoteRepository
import com.noteaker.sample.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val noteRepository: MyNoteRepository) : ViewModel() {

    fun addClick() {

    }

    fun onTopBarItemClick(item: TopBarItem) {

    }
}