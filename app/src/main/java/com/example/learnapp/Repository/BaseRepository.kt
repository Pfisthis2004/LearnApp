package com.example.learnapp.Repository

import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.Question

interface BaseRepository {
    fun getQuestions( lessonId: String, callback: (List<Question>) -> Unit)
    fun updateLessonStatus(lessonId: Lesson, nextLessonId: String)
}
