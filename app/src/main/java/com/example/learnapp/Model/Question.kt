package com.example.learnapp.Model

import com.google.firebase.firestore.PropertyName

data class Question(
    var id: String,
    var lessonId: String,
    var articleId: String = "",
    var type: QuestionType? =null,
    var prompt: String = "",
    var options: List<String> = listOf(),
    var correctAnswer: String = "",
    var explanation: String = "",
    var translation: String = "",

    @get:PropertyName("vocab")
    @set:PropertyName("vocab")
    var vocab: String = "",

    var videoUrl: String="",
    var expectedText: String = ""
)
