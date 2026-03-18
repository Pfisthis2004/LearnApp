package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Article
import com.example.learnapp.Model.ArticleQuestion
import com.example.learnapp.Repository.ArticlesRepository
import com.google.firebase.auth.FirebaseAuth

class ArticleViewModel : ViewModel() {
    private val repository = ArticlesRepository()

    val articlesLiveData = MutableLiveData<List<Article>>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadArticles() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.postValue(true)

        repository.fetchArticlesWithStatus(userId) { list ->
            articlesLiveData.postValue(list)
            _isLoading.postValue(false)
        }
    }
}
