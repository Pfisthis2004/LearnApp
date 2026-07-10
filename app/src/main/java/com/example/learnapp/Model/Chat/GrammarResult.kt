package com.example.learnapp.Model.Chat

data class GrammarResult(
    val original: String,
    val corrected: String,
    val errors: List<String>,
    val fixes: List<String>
)