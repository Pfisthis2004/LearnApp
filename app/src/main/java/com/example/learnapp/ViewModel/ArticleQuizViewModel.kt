package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.ArticleQuestion
import com.example.learnapp.Repository.ArticleQuizRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArticleQuizViewModel : ViewModel() {
    private val repository = ArticleQuizRepository()
    val questions = MutableLiveData<List<ArticleQuestion>>()
    val currentIndex = MutableLiveData(0)
    var score = 0

    // Đồng bộ tên biến với Activity (isFinishSaved)
    val isFinishSaved = MutableLiveData<Boolean>()

// ArticleQuizViewModel.kt
    fun loadQuestions(articleId: String) {
        FirebaseFirestore.getInstance()
            .collection("article_questions") // Sửa tên collection cho đúng thực tế DB
            .whereEqualTo("articleId", articleId) // Lọc các câu hỏi thuộc bài viết này
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d("QUIZ_DEBUG", "Không tìm thấy câu hỏi cho bài: $articleId")
                    questions.postValue(emptyList())
                } else {
                    val list = snapshot.toObjects(ArticleQuestion::class.java)
                    Log.d("QUIZ_DEBUG", "Lấy được ${list.size} câu hỏi")
                    questions.postValue(list)
                }
            }
            .addOnFailureListener {
                Log.e("QUIZ_DEBUG", "Lỗi: ${it.message}")
            }
    }

    fun nextQuestion() {
        val next = (currentIndex.value ?: 0) + 1
        if (next < (questions.value?.size ?: 0)) {
            currentIndex.value = next
        }
    }
    fun submitQuiz(articleId: String, xpReward: Int) {
        val totalQuestions = questions.value?.size ?: 0
        // Gọi repo xử lý logic kiểm tra "lần đầu"
        repository.saveQuizResult(articleId, score, totalQuestions, xpReward) {
            isFinishSaved.postValue(it)
        }
    }
}