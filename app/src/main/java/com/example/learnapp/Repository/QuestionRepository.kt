package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.Repository.BaseRepository
import com.google.firebase.firestore.FirebaseFirestore

class QuestionRepository : BaseRepository {
    private val db = FirebaseFirestore.getInstance()

    override fun getQuestions(lessonId: String, callback: (List<Question>) -> Unit) {
        Log.d("QuestionRepository", "Fetching questions for, lessonId=$lessonId")

        db.collection("questions")
            .whereEqualTo("lessonId", lessonId)
            .get()
            .addOnSuccessListener { result ->
                Log.d("QuestionRepository", "Query success, total docs=${result.size()}")

                val questions = result.documents.mapNotNull { doc ->
                    Log.d("QuestionRepository", "Processing docId=${doc.id}")

                    val typeValue = doc.getString("type") ?: "MULTIPLE_CHOICE"
                    val type = QuestionType.fromValue(typeValue) ?: QuestionType.MULTIPLE_CHOICE

                    Question(
                        id = doc.id,
                        lessonId = doc.getString("lessonId") ?: "",
                        articleId = doc.getString("articleId") ?: "",
                        type = type,
                        prompt = doc.getString("prompt") ?: "",
                        options = doc.get("options") as? List<String> ?: emptyList(),
                        correctAnswer = doc.getString("correctAnswer") ?: "",
                        explanation = doc.getString("explanation") ?: "",
                        videoUrl = doc.getString("videoUrl") ?: "",
                        expectedText = doc.getString("expectedText") ?: ""
                    )
                }
                Log.d("QuestionRepository", "Mapped ${questions.size} questions")
                callback(questions)
            }
            .addOnFailureListener { e ->
                Log.e("QuestionRepository", "Failed to fetch questions", e)
                callback(emptyList())
            }
    }

    override fun updateLessonStatus( lessonId: String, nextLessonId: String) {
        Log.d("QuestionRepository", "Updating lesson $lessonId to completed, next=$nextLessonId")

        db.collection("lessons")
            .document(lessonId)
            .update("status", "completed")
            .addOnSuccessListener {
                Log.d("QuestionRepository", "Lesson $lessonId marked completed")
                db.collection("lessons")
                    .document(nextLessonId)
                    .update("status", "unlocked")
                    .addOnSuccessListener {
                        Log.d("QuestionRepository", "Next lesson $nextLessonId unlocked")
                    }
                    .addOnFailureListener { e ->
                        Log.e("QuestionRepository", "Failed to unlock next lesson $nextLessonId", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("QuestionRepository", "Failed to update lesson $lessonId", e)
            }
    }

}
