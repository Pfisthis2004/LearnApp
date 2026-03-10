package com.example.learnapp.Model

import com.google.firebase.Timestamp

data class Level(
    var id: String = "",
    var title: String = "",
    var order: Int = 0,
    var createdAt: Timestamp? = null
)
