package com.example.learnapp.Model.Chat
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class ScenarioOption(
    val title: String = "",
    val description: String = "",
    val config: ChatConfig? = null // Cho phép config null để an toàn khi parse
) : Parcelable