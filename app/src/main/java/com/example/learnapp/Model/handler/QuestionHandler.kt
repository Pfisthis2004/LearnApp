package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

interface QuestionHandler {
    fun checkAnswer(userInput: String, question: Question): ResultState

}