package com.example.learnapp.Model

data class Chapter(
    val title: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val lessons: Map<String, Lesson> = emptyMap()
)
