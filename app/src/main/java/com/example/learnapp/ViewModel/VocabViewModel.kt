package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.Repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VocabViewModel : ViewModel() {
    private val repository = VocabularyRepository() // Sử dụng Repository
    private val _vocabList = MutableLiveData<List<Vocabulary>>()
    val vocabList: LiveData<List<Vocabulary>> = _vocabList
    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun fetchVocabularies(userId: String) {
        viewModelScope.launch {
            // Sử dụng Flow từ Repository để lắng nghe dữ liệu
            repository.getVocabularies(userId).collect { list ->
                _vocabList.value = list
            }
        }
    }

    // Thêm hàm này vào VocabViewModel.kt
    fun updateAllFavorites(userId: String, changes: Map<String, Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateMultipleFavorites(userId, changes)
                Log.d("vocabVM", "Đã cập nhật hàng loạt thành công!")
            } catch (e: Exception) {
                Log.e("vocabVM", "Lỗi cập nhật hàng loạt: ${e.message}")
            }
        }
    }

    // Trong VocabViewModel.kt
    fun saveVocabFromLesson(userId: String, listFromLesson: List<Vocabulary>) {
        Log.d("vocabVM", "Yêu cầu lưu ${listFromLesson.size} từ từ bài học.")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Lấy danh sách từ vựng cũ từ Firestore
                val existingVocabs = repository.getAllVocabListOnce(userId)

                // 2. Chuyển thành Set các chuỗi (đã viết thường và xóa khoảng trắng) để so sánh nhanh
                val existingWords = existingVocabs.map { it.vocab.trim().lowercase() }.toSet()

                // 3. Lọc: Chỉ giữ lại những từ CHƯA xuất hiện trong Set trên
                val filteredList = listFromLesson.filter {
                    it.vocab.trim().lowercase() !in existingWords
                }

                if (filteredList.isNotEmpty()) {
                    repository.saveVocabularies(userId, filteredList)
                    Log.d("vocabVM", "Thành công: Đã lưu thêm ${filteredList.size} từ mới.")
                } else {
                    Log.d("vocabVM", "Bỏ qua: Tất cả từ vựng này đã có trong bộ sưu tập.")
                }

                _saveStatus.postValue(true)
            } catch (e: Exception) {
                Log.e("vocabVM", "Lỗi khi lọc và lưu: ${e.message}")
                _saveStatus.postValue(false)
            }
        }
    }
}
