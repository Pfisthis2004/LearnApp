package com.example.learnapp.Model.handler

import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

// Trong OrderingHandler.kt
class OrderingHandler : QuestionHandler {

    // Ghi đè phương thức cũ (nếu cần thiết cho interface)
    override fun checkAnswer(userInput: String, question: Question): ResultState {
        // Để trống hoặc ném lỗi nếu không muốn dùng cách này
        return ResultState.OrderingResult(
            isCorrect = false,
            userAnswerList = emptyList(),
            correctAnswer = question.correctAnswer // Thêm tham số này vào
        )    }

    // THÊM HÀM NÀY
    fun checkOrderingAnswer(userWords: List<String>, question: Question): ResultState {
        // 1. Lấy danh sách đáp án đúng từ DB (giả sử là "He is from Vietnam")
        val correctWords = question.correctAnswer.split(" ").filter { it.isNotBlank() }

        // 2. Nếu số lượng từ người dùng chọn khớp với số từ đáp án
        // Hoặc nếu người dùng chọn từng chữ cái (như P-o-l) nhưng câu hỏi là 1 từ đơn
        val isCorrect = if (userWords.size != correctWords.size) {
            // Trường hợp đặc biệt: Người dùng chọn ký tự rời, ta gom lại
            userWords.joinToString("") == correctWords.joinToString("")
        } else {
            userWords == correctWords
        }

        return ResultState.OrderingResult(
            isCorrect = isCorrect,
            userAnswerList = if (userWords.size != correctWords.size) listOf(userWords.joinToString("")) else userWords,
            correctAnswer = question.correctAnswer
        )
    }
}