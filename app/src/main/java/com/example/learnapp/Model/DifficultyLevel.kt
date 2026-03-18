package com.example.learnapp.Model
enum class DifficultyLevel(val value: String) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2");

    companion object {
        fun fromString(value: String): DifficultyLevel {
            return values().find { it.value == value } ?: A1
        }
    }
}