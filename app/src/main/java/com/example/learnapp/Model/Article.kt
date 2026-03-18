package com.example.learnapp.Model

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Article(
    var id: String = "",
    val title: String = "",
    val level: String = "A1",
    val thumbnail: String = "",
    val description: String = "",
    val content: String = "",
    val highlightedWords: List<String> = emptyList(),
    val quizCount: Int = 0,
    val xp: Int = 0,
    @get:Exclude var isCompleted: Boolean = false
): Serializable

