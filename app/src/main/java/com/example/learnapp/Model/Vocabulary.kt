package com.example.learnapp.Model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Vocabulary (
    var id: String = "",

    var lessonId: String = "",
    var chapterId: String = "",
    var levelId: String="",

    var vocab: String = "",
    var translation: String = "",
    var example: String = "",

    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false,

    var createdAt: Long = 0L
): Serializable