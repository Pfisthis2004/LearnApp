package com.example.learnapp.Model.Chat

data class AIResponse(
    val reply: String,
    val vi_trans: String,
    val goal_status: List<Boolean>,
    val is_finished: Boolean,
    val score: Int,
    val good_sounds: List<String> = emptyList(), // Các âm phát âm tốt
    val improve_sounds: List<String> = emptyList(), // Các âm cần cải thiện
    val grammar_errors: List<String> = emptyList() // Các loại lỗi ngữ pháp (Mạo từ, Chia động từ...)
)
