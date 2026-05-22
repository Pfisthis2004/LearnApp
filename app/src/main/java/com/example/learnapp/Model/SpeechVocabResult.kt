package com.example.learnapp.Model

import java.io.Serializable

data class SpeechVocabResult(
    val similarityScore: Int,
    val correctCount: Int,
    val totalCount: Int,
    val wrongWordsList: List<String>,
    val wordComparisonDetails: List<WordComparison>, // Gộp chi tiết vào đây
    val targetIpaWords: List<String>
) : Serializable
data class WordComparison(
    val word: String,
    val isWordCorrect: Boolean,
    val charComparison: List<Boolean>? = null
) : Serializable
