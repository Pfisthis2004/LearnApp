package com.example.learnapp.Model

sealed class ResultState {
    data class QuizResult(val correct: Int, val total: Int): ResultState()
    data class SpeakingResult(val isCorrect: Boolean, val expected: String): ResultState()
    data class FillBlankResult(val isCorrect: Boolean, val userAnswer: String): ResultState()
}