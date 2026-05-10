package com.example.learnapp.Model.Chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatConfig(
    val title: String = "English Conversation",
    val description: String ="",
    val situation: String = "",
    val roles: List<String> = emptyList(),          // Ví dụ: ["Bác sĩ", "Bệnh nhân"]
    val goals_for_roles: List<List<String>> = emptyList(),
    val botRole: String = "",
    val userRole: String = "",
    val goals: List<String> = emptyList(),
    val personality: String = "Friendly", // Gán mặc định ở đây
    val attitude: String = "Supportive",  // Gán mặc định ở đây
    @SerializedName("opening_header")
    val openingHeader: String = ""
): Parcelable
