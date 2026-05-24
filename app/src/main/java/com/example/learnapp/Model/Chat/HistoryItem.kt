package com.example.learnapp.Model.Chat

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class HistoryItem(
    var id: String = "",
    val lessonTitle: String = "",
    val score: Int = 0,
    val timestamp: @RawValue Any? = null,

    val level: String = "Beginner",
    val overallFeedback: String = "",
    val grammarScore: Int = 0,
    val vocabScore: Int = 0,
    val pronunciationScore: Int = 0,
    // Dữ liệu chi tiết
    val goalsText: List<String> = emptyList(),
    val goalsStatus: List<Boolean> = emptyList(),
    val goodSounds: List<String> = emptyList(),
    val improveSounds: List<String> = emptyList(),
    val grammarErrors: List<String> = emptyList(),
    val vocabSuggestions: List<String> = emptyList(),
    val pronunciationFocus: List<String> = emptyList()
) : Parcelable