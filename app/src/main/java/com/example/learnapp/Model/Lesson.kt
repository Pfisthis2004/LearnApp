package com.example.learnapp.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName


data class Lesson(
    var id: String = "",
    var chapterId: String = "",
    var levelId: String="",
    val title: String = "",
    val icon: String= "",
    val xpReward: Int =0,
    val order:Int = 0,
    val questionCount: Int = 0,
    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false,
    @get:Exclude // Cực kỳ quan trọng: Không lưu trường này lên Firestore
    @set:Exclude
    var isCompleted: Boolean = false,
    val createdAt: Timestamp? = null
)
