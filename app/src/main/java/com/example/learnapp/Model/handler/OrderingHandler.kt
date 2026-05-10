package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

class OrderingHandler : QuestionHandler {
    override fun checkAnswer(userInput: String, question: Question): ResultState {
        // userInput: "I|am|learning|Kotlin"
        // question.correctAnswer: "I|am|learning|Kotlin
        val cleanUserAns = userInput.trim().replace("\\s+".toRegex(), " ")
        val cleanCorrectAns = question.correctAnswer.trim().replace("\\s+".toRegex(), " ")

        val isCorrect = cleanUserAns.equals(cleanCorrectAns, ignoreCase = true)

        return ResultState.OrderingResult(
            isCorrect = isCorrect,
            userAnswer = cleanUserAns, // Trả về chuỗi sạch để hiển thị
            correctAnswer = cleanCorrectAns
        )
    }

}