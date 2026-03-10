package com.example.learnapp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.learnapp.Repository.BaseRepository

class QuestionViewModelFactory(private val repository: BaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionViewModel(repository) as T
        }
        // Sau này nếu có thêm ViewModel khác dùng chung Repository, bạn có thể thêm else if ở đây
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}