package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

class FillBlankHandler : QuestionHandler {
    override fun checkAnswer(userInput: String, question: Question): ResultState {
        val cleanInput = userInput.trim().lowercase()
        // Tách các đáp án đúng bằng dấu | để kiểm tra
        val correctAnswers = question.correctAnswer.split("|").map { it.trim().lowercase() }

        val isCorrect = cleanInput in correctAnswers

        // Lấy đáp án đầu tiên để hiển thị nếu người dùng làm sai
        val displayCorrectAnswer = question.correctAnswer.split("|").first().trim()

        return ResultState.FillBlankResult(
            isCorrect = isCorrect,
            userAnswer = userInput,
            correctAnswer = displayCorrectAnswer
        )
    }
}