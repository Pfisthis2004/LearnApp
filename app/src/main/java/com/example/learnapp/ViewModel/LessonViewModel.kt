package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Repository.LessonRepository
import kotlinx.coroutines.launch

class LessonViewModel : ViewModel() {
    private val repository = LessonRepository()

    // LiveData cho chapters
    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> get() = _chapters
    private val _completedLessons = MutableLiveData<List<String>>(emptyList())
    val completedLessons: LiveData<List<String>> get() = _completedLessons
    // LiveData cho lessons
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    /**
     * Hàm duy nhất cần dùng để load toàn bộ dữ liệu theo Level
     * Kết quả trả về là list Chapters, mỗi Chapter đã nạp đầy đủ list Lessons
     */
    fun loadData(levelId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            // 2. Lấy danh sách hoàn thành từ Repository (Giả định hàm lấy từ Firestore)
            val completedList = repository.getCompletedLessons()
            _completedLessons.postValue(completedList)

            // 3. Load chapters như cũ
            val result = repository.fetchChaptersWithLessons(levelId)
            _chapters.postValue(result)

            _isLoading.postValue(false)
        }
    }
    fun loadChaptersByLevel(levelId: String) {
        loadData(levelId)
    }
}
