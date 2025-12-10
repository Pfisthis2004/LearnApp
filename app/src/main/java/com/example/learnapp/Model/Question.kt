package com.example.learnapp.Model

data class Question(
    val prompt: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val videoUrl: String=""
)
