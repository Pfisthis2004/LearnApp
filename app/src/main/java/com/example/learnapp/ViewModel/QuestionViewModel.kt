package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.Model.ResultState
import com.example.learnapp.Model.handler.FillBlankHandler
import com.example.learnapp.Model.handler.OrderingHandler
import com.example.learnapp.Model.handler.QuestionHandler
import com.example.learnapp.Model.handler.QuizHandler
import com.example.learnapp.Model.handler.SpeakingHandler
import com.example.learnapp.Repository.BaseRepository


class QuestionViewModel(
    private val repository: BaseRepository
) : ViewModel() {

    private val TAG = "QuestionViewModel"

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> get() = _questions

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> get() = _currentIndex
    private val _selectedOrderingWords = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedOrderingWords: LiveData<MutableList<String>> get() = _selectedOrderingWords
    private val _result = MutableLiveData<ResultState?>()
    val result: LiveData<ResultState?> get() = _result

    private var _correctCount = 0
    val correctCount: Int get() = _correctCount

    private var lessonData: Lesson? = null
    private var lastLessonId: String? = null
    private var nextLessonId: String = ""
    fun loadQuestions(lessonId: String) {
        if (lessonId == lastLessonId && _questions.value != null && _questions.value!!.isNotEmpty()) {
            return
        }
        repository.getQuestions(lessonId) { list ->
            _questions.value = list
            _currentIndex.value = 0
            _correctCount = 0
            lastLessonId = lessonId // Cập nhật ID bài học vừa load thành công
        }
    }
    fun addWordToOrdering(word: String) {
        val current = _selectedOrderingWords.value ?: mutableListOf()
        current.add(word)
        _selectedOrderingWords.value = current
    }

    fun removeWordFromOrdering(word: String) {
        val current = _selectedOrderingWords.value ?: mutableListOf()
        current.remove(word)
        _selectedOrderingWords.value = current
    }

    fun resetOrdering() {
        _selectedOrderingWords.value = mutableListOf()
    }
    fun nextQuestion() {
        val newIndex = (_currentIndex.value ?: 0) + 1
        _currentIndex.value = newIndex
    }
    fun setLessonInfo(lesson: Lesson) {
        this.lessonData = lesson
    }

    fun finishLesson(nextLessonId: String) {
        lessonData?.let {
            repository.updateLessonStatus(it, nextLessonId)
        }
    }
    fun checkAnswer(userInput: String) {
        val index = _currentIndex.value ?: 0
        val question = _questions.value?.getOrNull(index)
        question?.let {
            val handler = getHandlerForType(it.type)
            val res: ResultState = handler.checkAnswer(userInput, it)
            _result.value = res
            when (res) {
                is ResultState.QuizResult -> {
                    if (res.correct > 0) {
                        _correctCount++
                    }
                }
                is ResultState.SpeakingResult -> {
                    if (res.isCorrect) {
                        _correctCount++
                    }
                }
                is ResultState.FillBlankResult -> {
                    if (res.isCorrect) {
                        _correctCount++
                    }
                }
                is ResultState.OrderingResult -> {
                    if (res.isCorrect){
                        _correctCount++
                    }
                }
            }
        } ?: Log.w(TAG, "No question found at index=$index")
    }
    // Trong QuestionViewModel.kt
    fun checkOrderingAnswer(userSelectedWords: List<String>) {
        val index = _currentIndex.value ?: 0
        val question = _questions.value?.getOrNull(index) ?: return

        val handler = OrderingHandler()
        val res = handler.checkOrderingAnswer(userSelectedWords, question)

        _result.value = res

        if (res is ResultState.OrderingResult && res.isCorrect) {
            _correctCount++
        }
    }
    private fun getHandlerForType(type: QuestionType?): QuestionHandler {
        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> QuizHandler()
            QuestionType.SPEAKING -> SpeakingHandler()
            QuestionType.FILL_IN_THE_BLANK -> FillBlankHandler()
            QuestionType.ORDERING -> OrderingHandler()
            null -> {
                Log.e(TAG, "Unknown QuestionType=null")
                throw IllegalArgumentException("Unknown QuestionType")
            }
        }
    }
}
