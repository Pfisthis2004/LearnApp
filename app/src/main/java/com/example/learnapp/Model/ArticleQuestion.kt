package com.example.learnapp.Model

import java.io.Serializable

data class ArticleQuestion(
    var id: String = "",
    val articleId: String = "",
    val prompt: String = "",         // Câu hỏi
    val options: List<String> = emptyList(), // Danh sách lựa chọn
    val correctAnswer: String = "",  // Đáp án đúng dạng chữ
): Serializable
