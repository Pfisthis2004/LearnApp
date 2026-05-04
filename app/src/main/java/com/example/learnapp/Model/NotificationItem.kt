package com.example.learnapp.Model

import com.google.firebase.Timestamp

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val sentAt: Timestamp? = null,
    val sentBy: String = "",
    val target: String = ""
)
