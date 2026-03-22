package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.ArticleQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ArticleQuizRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveQuizResult(articleId: String, score: Int, total: Int, fixedXP: Int, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val resultId = "${userId}_${articleId}"
        val resultRef = db.collection("user_articles_result").document(resultId)

        // Bước 1: Kiểm tra xem user đã từng làm bài này chưa
        resultRef.get().addOnSuccessListener { document ->
            val batch = db.batch()

            // Dữ liệu kết quả (Luôn cập nhật/ghi đè để lưu điểm mới nhất)
            val resultData = hashMapOf(
                "userId" to userId,
                "articleId" to articleId,
                "score" to score,
                "totalQuestions" to total,
                "xpGained" to fixedXP,
                "timestamp" to FieldValue.serverTimestamp()
            )
            batch.set(resultRef, resultData)

            if (!document.exists()) {
                val userRef = db.collection("users").document(userId)

                // ĐỔI "xp" THÀNH "totalXP" (hoặc tên field bạn muốn tích lũy điểm)
                batch.update(userRef, "totalXP", FieldValue.increment(fixedXP.toLong()))

                Log.d("QUIZ_DEBUG", "Lần đầu hoàn thành: Cộng $fixedXP vào totalXP")
            }

            // Bước 3: Thực thi
            batch.commit().addOnCompleteListener { onComplete(it.isSuccessful) }

        }.addOnFailureListener {
            onComplete(false)
        }
    }
}