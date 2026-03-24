package com.example.learnapp.Model.Chat

data class ChatMessage(
    val text: String,
    val sender: String, // "AI" hoặc "USER"
    val translation: String? = null,
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
