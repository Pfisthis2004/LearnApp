package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

class FillBlankHandler: QuestionHandler {
    override fun checkAnswer(userInput: String, question: Question): ResultState {
        val isCorrect = userInput.trim().equals(question.correctAnswer, ignoreCase = true)
        return ResultState.FillBlankResult(isCorrect, userInput)
    }
}