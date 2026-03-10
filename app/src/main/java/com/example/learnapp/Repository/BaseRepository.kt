package com.example.learnapp.Repository

import com.example.learnapp.Model.Question

interface BaseRepository {
    fun getQuestions( lessonId: String, callback: (List<Question>) -> Unit)
    fun updateLessonStatus( lessonId: String, nextLessonId: String)
}
