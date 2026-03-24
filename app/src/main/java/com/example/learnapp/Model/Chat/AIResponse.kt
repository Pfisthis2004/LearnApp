package com.example.learnapp.Model.Chat

data class AIResponse(
    val reply: String,
    val vi_trans: String,
    val goal_status: List<Boolean>,
    val is_finished: Boolean,
    val score: Int
)
