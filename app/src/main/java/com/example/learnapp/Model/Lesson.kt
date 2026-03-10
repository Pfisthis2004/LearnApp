package com.example.learnapp.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


data class Lesson(
    var id: String = "",
    val chapterId: String = "",
    val levelId: String="",
    val title: String = "",
    val icon: String= "",
    val xpReward: Int =0,
    val order:Int = 0,
    val questionCount: Int = 0,
    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false,
    val createdAt: Timestamp? = null
)
