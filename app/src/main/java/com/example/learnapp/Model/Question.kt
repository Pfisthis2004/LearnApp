package com.example.learnapp.Model

data class Question(
    val id: String,
    val lessonId: String,
    val articleId: String = "",
    val type: QuestionType? =null,
    val prompt: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val videoUrl: String="",
    val expectedText: String = ""
)
