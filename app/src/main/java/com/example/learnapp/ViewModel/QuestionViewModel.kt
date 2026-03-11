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

    private val _result = MutableLiveData<ResultState?>()
    val result: LiveData<ResultState?> get() = _result

    private var _correctCount = 0
    val correctCount: Int get() = _correctCount

    private var lessonData: Lesson? = null
    private var lastLessonId: String? = null
    private var isProcessingFinish = false
    fun loadQuestions(lessonId: String) {
        // KIỂM TRA: Nếu đã load đúng Lesson này rồi và danh sách câu hỏi không rỗng thì KHÔNG load nữa
        if (lessonId == lastLessonId && _questions.value != null && _questions.value!!.isNotEmpty()) {
            Log.d(TAG, "Questions for $lessonId already loaded. Skipping.")
            return
        }

        Log.d(TAG, "Loading questions for lessonId=$lessonId")
        repository.getQuestions(lessonId) { list ->
            Log.d(TAG, "Loaded ${list.size} questions")
            _questions.value = list
            _currentIndex.value = 0
            _correctCount = 0
            lastLessonId = lessonId // Cập nhật ID bài học vừa load thành công
            Log.d(TAG, "Reset currentIndex=0, correctCount=0")
        }
    }

    fun nextQuestion() {
        val newIndex = (_currentIndex.value ?: 0) + 1
        Log.d(TAG, "Moving to next question: index=$newIndex")
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

        Log.d(TAG, "Checking answer for question index=$index, userInput=$userInput")

        question?.let {
            val handler = getHandlerForType(it.type)
            val res: ResultState = handler.checkAnswer(userInput, it)
            _result.value = res

            Log.d(TAG, "Result for question ${it.id}: $res")

            when (res) {
                is ResultState.QuizResult -> {
                    if (res.correct > 0) {
                        _correctCount++
                        Log.d(TAG, "QuizResult correct. Total correctCount=$_correctCount")
                    }
                }
                is ResultState.SpeakingResult -> {
                    if (res.isCorrect) {
                        _correctCount++
                        Log.d(TAG, "SpeakingResult correct. Total correctCount=$_correctCount")
                    }
                }
                is ResultState.FillBlankResult -> {
                    if (res.isCorrect) {
                        _correctCount++
                        Log.d(TAG, "FillBlankResult correct. Total correctCount=$_correctCount")
                    }
                }
            }
        } ?: Log.w(TAG, "No question found at index=$index")
    }

    private fun getHandlerForType(type: QuestionType?): QuestionHandler {
        Log.d(TAG, "Getting handler for type=$type")
        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> QuizHandler()
            QuestionType.SPEAKING -> SpeakingHandler()
            QuestionType.FILL_IN_THE_BLANK -> FillBlankHandler()
            null -> {
                Log.e(TAG, "Unknown QuestionType=null")
                throw IllegalArgumentException("Unknown QuestionType")
            }
        }
    }
}
