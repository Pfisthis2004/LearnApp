package com.example.learnapp.Model


data class Lesson(
    val id: String = "",
    val chapterId: String = "",
    val title: String = "",
    val icon: String= "",
    val questionType: String = "",
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)
