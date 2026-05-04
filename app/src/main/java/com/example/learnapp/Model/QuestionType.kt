package com.example.learnapp.Model

enum class QuestionType(val value: String) {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    SPEAKING("SPEAKING"),
    FILL_IN_THE_BLANK("FILL_IN_THE_BLANK"),
    ORDERING("ORDERING");

    companion object {
        fun fromValue(value: String): QuestionType? {
            return values().find { it.value == value }
        }
    }
}
