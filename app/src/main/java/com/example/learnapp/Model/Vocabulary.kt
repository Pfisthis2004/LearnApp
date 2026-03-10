package com.example.learnapp.Model

data class Vocabulary (
    val id: String = "",
    val lessonId: String = "",
    val word: String = "",
    val ipa: String = "",
    val translation: String = "",
    val example: String = "",
    val createdAt: Long = 0L
)