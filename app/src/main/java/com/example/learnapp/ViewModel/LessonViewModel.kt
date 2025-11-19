package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Repository.LessonRepository

class LessonViewModel: ViewModel() {
    private val repository = LessonRepository()
    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> get() = _chapters

    fun loadChapters() {
        repository.fetchChapters { chapterList ->
            _chapters.postValue(chapterList)
        }
    }
}