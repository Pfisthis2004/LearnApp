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
        Log.d("SpeakingViewModel", "loadQuestions: chapterId=$chapterId, lessonId=$lessonId")
        repository.getQuestions(chapterId, lessonId) { list ->
            Log.d("SpeakingViewModel", "Loaded ${list.size} questions for $chapterId/$lessonId")
            list.forEachIndexed { i, q ->
                Log.d("SpeakingViewModel", "Question[$i]: prompt=${q.prompt}, videoUrl=${q.videoUrl}")
            }
            _questions.value = list
            _currentIndex.value = 0
        }
    }


    fun checkAnswer(recognizedText: String) {
        val index = _currentIndex.value ?: 0
        val question = _questions.value?.get(index)
        question?.let {
            val isCorrect = recognizedText.trim().equals(it.expectedText, ignoreCase = true)
            _result.value = Pair(isCorrect, it.expectedText)
        }
    }
    private fun getNextLessonId(currentLessonId: String): String {
        val number = currentLessonId.filter { it.isDigit() }.toIntOrNull() ?: 1
        return "lesson${number + 1}"
    }
    fun unlockNextLesson(chapterId: String, lessonId: String) {
        val nextLessonId = getNextLessonId(lessonId)
        // Cập nhật trạng thái bài học trong repository
        repository.updateLessonStatus(chapterId, lessonId, nextLessonId)
    }
    fun nextQuestion() {
        _currentIndex.value = (_currentIndex.value ?: 0) + 1
    }

}