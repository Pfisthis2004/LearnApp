package com.example.learnapp.Repository

import com.example.learnapp.Model.Article
import com.google.firebase.firestore.FirebaseFirestore

class ArticlesRepository {
    private val db = FirebaseFirestore.getInstance()

    // ArticlesRepository.kt
    fun fetchArticlesByLevel(userId: String, level: String, onResult: (List<Article>) -> Unit) {
        // Thêm .whereEqualTo("level", level) để lọc từ đầu
        db.collection("articles")
            .whereEqualTo("level", level)
            .get()
            .addOnSuccessListener { snapshot ->
                val articles = snapshot.toObjects(Article::class.java)
                snapshot.documents.forEachIndexed { index, doc -> articles[index].id = doc.id }

                db.collection("user_articles_result")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { results ->
                        val completedIds = results.map { it.getString("articleId") }.toSet()
                        articles.forEach { it.isCompleted = completedIds.contains(it.id) }
                        onResult(articles)
                    }
            }.addOnFailureListener { onResult(emptyList()) }
    }
}