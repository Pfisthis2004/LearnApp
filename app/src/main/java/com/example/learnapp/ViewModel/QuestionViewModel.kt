package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Question
import com.example.learnapp.Repository.QuestionRepository

class QuestionViewModel: ViewModel() {
    private val repository = QuestionRepository()
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> get() = _questions
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> get() = _currentIndex

    private val _result = MutableLiveData<Pair<Int,Int>>() // correctCount, totalCount
    val result: LiveData<Pair<Int, Int>> get() = _result
    private val _finalVocabulary = MutableLiveData<String>()
    val finalVocabulary: LiveData<String> get() = _finalVocabulary
    private var correctCount = 0
    fun loadQuestions(chapterId: String, lessonId: String) {
        repository.getQuestions(chapterId, lessonId) { list ->
            _questions.value = list
        }
    }
    private fun scorePercent(correctCount: Int, totalCount: Int): Int {
        return if (totalCount == 0) 0 else (correctCount * 100) / totalCount
    }
    fun nextQuestion() {
        _currentIndex.value = (_currentIndex.value ?: 0) + 1
    }
    fun saveResult(chapterId: String, lessonId: String, correctCount: Int, totalCount: Int,lessonCount: Int) {
        _result.value = Pair(correctCount, totalCount)
        Log.d("ResultDebug", "Đã hoàn thành bài học: $lessonId trong chapter: $chapterId | Điểm: $correctCount/$totalCount")

        if (!isLastLesson(lessonId, lessonCount)) {
            val vocabList = _questions.value?.map { q ->
                "${q.correctAnswer} - ${q.explanation}\n"
            } ?: emptyList()

            val vocabText = vocabList.joinToString("\n") + "\n\nKết quả: $correctCount / $totalCount"
            _finalVocabulary.value = vocabText
            Log.d("ResultDebug","tu vung ${vocabText}")
            Log.d("ResultDebug", "Không phải bài cuối. Mở khóa bài tiếp theo...")
            unlockNextLesson(chapterId, lessonId)
        } else {
            _finalVocabulary.value = ""

            val score = scorePercent(correctCount, totalCount)
            Log.d("ResultDebug", "Là bài cuối. Điểm đạt: $score%")
            if (score >= 80) {
                val nextChapterId = "chapter${chapterId.filter { it.isDigit() }.toInt() + 1}"
                Log.d("ResultDebug", "Điểm đủ cao. Mở khóa chapter tiếp theo: $nextChapterId")
                unlockNextChapter(nextChapterId)
            } else {
                Log.d("ResultDebug", "Điểm chưa đủ để mở khóa chapter tiếp theo.")
            }
        }
    }
    fun fetchLessonCountAndSaveResult(chapterId: String, lessonId: String, correctCount: Int, totalCount: Int) {
        repository.getLessonCount(chapterId) { lessonCount ->
            saveResult(chapterId, lessonId, correctCount, totalCount, lessonCount)
        }
    }
    fun increaseCorrect(totalCount: Int) {
        correctCount++
        _result.value = Pair(correctCount, totalCount)
    }
    fun unlockNextLesson(chapterId: String, lessonId: String) {
        val nextLessonId = getNextLessonId(lessonId)
        // Cập nhật trạng thái bài học trong repository
        repository.updateLessonStatus(chapterId, lessonId, nextLessonId)
    }
    fun unlockNextChapter(nextChapterId: String) {
        Log.d("UnlockDebug", "Đang mở khóa bài học đầu tiên của chapter mới: $nextChapterId")
        val firstLessonId = "lesson1"
        repository.unlockLesson(nextChapterId, firstLessonId)
    }
    private fun getNextLessonId(currentLessonId: String): String {
        val number = currentLessonId.filter { it.isDigit() }.toIntOrNull() ?: 1
        return "lesson${number + 1}"
    }
    fun isLastLesson(lessonId: String, totalCount: Int): Boolean {
        val number = lessonId.filter { it.isDigit() }.toIntOrNull() ?: 1
        return number == totalCount
    }
}