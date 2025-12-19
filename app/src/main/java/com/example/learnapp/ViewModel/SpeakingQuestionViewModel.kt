package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.SpeakingQuestion
import com.example.learnapp.Repository.SpeakingQuestionRepository

class SpeakingQuestionViewModel:ViewModel() {
    private val repository = SpeakingQuestionRepository()
    private val _questions = MutableLiveData<List<SpeakingQuestion>>()
    val questions: LiveData<List<SpeakingQuestion>> = _questions
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _result = MutableLiveData<Pair<Boolean, String>?>()
    val result: LiveData<Pair<Boolean, String>?> = _result

    fun loadQuestions(chapterId: String, lessonId: String) {
        repository.getQuestions(chapterId, lessonId) { list ->
            _questions.value = list
            _currentIndex.value = 0
        }
    }

    fun unlockNextLesson(chapterId: String, lessonId: String) {
        val nextLessonId = getNextLessonId(lessonId)
        // Cập nhật trạng thái bài học trong repository
        repository.updateLessonStatus(chapterId, lessonId, nextLessonId)
    }
    fun nextQuestion() {
        _currentIndex.value = (_currentIndex.value ?: 0) + 1
    }

    fun checkAnswer(recognizedText: String) {
        val index = _currentIndex.value ?: 0
        val question = _questions.value?.get(index)
        question?.let {
            // Chuẩn hóa câu chuẩn: bỏ dấu câu, chữ hoa/thường
            val normalizedExpectedWords = it.expectedText
                .lowercase()
                .replace("[^a-z0-9' ]".toRegex(), "")
                .trim()
                .split("\\s+".toRegex())

            // Chuẩn hóa câu người dùng: bỏ dấu câu, chữ hoa/thường
            val candidates = recognizedText.split("|")
            var isCorrect = false

            for (candidate in candidates) {
                val normalizedUserWords = candidate
                    .lowercase()
                    .replace("[^a-z0-9' ]".toRegex(), "")
                    .trim()
                    .split("\\s+".toRegex())
                // Cho phép sai số nhỏ: so sánh độ dài danh sách từ
                val distance = levenshtein(
                    normalizedUserWords.joinToString(" "),
                    normalizedExpectedWords.joinToString(" ")
                )
                val threshold = (normalizedExpectedWords.size / 5).coerceAtLeast(2)
                // So khớp theo từ
                if (normalizedUserWords == normalizedExpectedWords) {
                    isCorrect = true
                    break
                }

                if (distance <= threshold) {
                    isCorrect = true
                    break
                }
            }

            _result.value = Pair(isCorrect, it.expectedText)
        }
    }



    // Hàm tính khoảng cách Levenshtein (sai khác ký tự)
    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // xóa
                    dp[i][j - 1] + 1,      // thêm
                    dp[i - 1][j - 1] + cost // thay thế
                )
            }
        }
        return dp[a.length][b.length]
    }

    private fun getNextLessonId(currentLessonId: String): String {
        val number = currentLessonId.filter { it.isDigit() }.toIntOrNull() ?: 1
        return "lesson${number + 1}"
    }

}