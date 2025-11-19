package com.example.learnapp.Model


data class Lesson(
    val title: String = "",
    val icon: String= "",
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)
