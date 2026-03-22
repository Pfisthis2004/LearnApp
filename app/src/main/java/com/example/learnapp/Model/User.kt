package com.example.learnapp.Model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val createdAt: Long = 0L,
    val isPremium: Boolean=false,
    val totalXP: Int = 0,
    val streak: Int = 0,
    val lastLoginAt: Long = 0L,
    val completedLevels: List<String> = emptyList(),
    val completedLessons : List<String> = emptyList(),
    val completedChapters: List<String> = emptyList(),
    val certificates: Int = 0,
    val completedDays: List<String> = emptyList()
)