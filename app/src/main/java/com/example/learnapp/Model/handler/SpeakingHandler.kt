package com.example.learnapp.Model.handler
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState

class SpeakingHandler : QuestionHandler {
    override fun checkAnswer(userInput: String, question: Question): ResultState {

        val normalizedExpectedWords = question.expectedText
            .lowercase()
            .replace("[^a-z0-9' ]".toRegex(), "")
            .trim()
            .split("\\s+".toRegex())

        val candidates = userInput.split("|")
        var isCorrect = false

        for (candidate in candidates) {
            val normalizedUserWords = candidate
                .lowercase()
                .replace("[^a-z0-9' ]".toRegex(), "")
                .trim()
                .split("\\s+".toRegex())

            val distance = levenshtein(
                normalizedUserWords.joinToString(" "),
                normalizedExpectedWords.joinToString(" ")
            )
            val threshold = (normalizedExpectedWords.size / 5).coerceAtLeast(2)

            if (normalizedUserWords == normalizedExpectedWords || distance <= threshold) {
                isCorrect = true; break
            }
        }
        return ResultState.SpeakingResult(isCorrect, question.expectedText)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }
}
