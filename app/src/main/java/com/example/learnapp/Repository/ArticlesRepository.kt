package com.example.learnapp.Repository

import com.example.learnapp.Model.Article
import com.google.firebase.firestore.FirebaseFirestore

class ArticlesRepository {
    private val db = FirebaseFirestore.getInstance()

    fun fetchArticlesWithStatus(userId: String, onResult: (List<Article>) -> Unit) {
        db.collection("articles").get().addOnSuccessListener { snapshot ->
            val articles = snapshot.toObjects(Article::class.java)
            // Gán ID document cho từng article
            snapshot.documents.forEachIndexed { index, doc -> articles[index].id = doc.id }

            // Lấy danh sách bài đã làm của User này
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