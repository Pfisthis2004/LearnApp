package com.example.learnapp.Model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val createdAt: Long = 0L,
    val isPremium: Boolean,
    val totalXP: Int = 0,
    val streak: Int = 0,
    val lastLoginAt: Long = 0L,
    val completedLessons : List<String> = emptyList()
)