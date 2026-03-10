package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

class QuizHandler: QuestionHandler {
    override fun checkAnswer(userInput: String, question: Question): ResultState {
        val isCorrect = userInput.trim().equals(question.correctAnswer, ignoreCase = true)
        val correct = if (isCorrect) 1 else 0
        return ResultState.QuizResult(correct, question.options.size)
    }
}