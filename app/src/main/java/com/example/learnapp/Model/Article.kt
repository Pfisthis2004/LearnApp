package com.example.learnapp.Model

data class Article(
    val id: String = "",
    val title: String = "",
    val level: DifficultyLevel,
    val thumbnail: String = "",
    val description: String = "",
    val content: String = "",
    val highlightedWords: List<String> = emptyList(),
    val quizCount: Int = 0,
)
