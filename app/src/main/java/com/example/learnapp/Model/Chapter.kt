package com.example.learnapp.Model

import com.google.firebase.Timestamp

data class Chapter(
    var id: String="",
    val title: String = "",
    val levelId: String = "",
    val order: Int = 0,
    val lessonCount: Int= 0,
    val createdAt: Timestamp? = null,
    var lessons: List<Lesson> = emptyList()

)
