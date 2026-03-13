package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.Repository.BaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class QuestionRepository : BaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun getQuestions(lessonId: String, callback: (List<Question>) -> Unit) {

        db.collection("questions")
            .whereEqualTo("lessonId", lessonId)
            .get()
            .addOnSuccessListener { result ->


                val questions = result.documents.mapNotNull { doc ->

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
                callback(questions)
            }

    }

    // QuestionRepository.kt
    override fun updateLessonStatus(lesson: Lesson, nextLessonId: String) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        val resultId = "${uid}_${lesson.id}"
        val resultRef = db.collection("user_lesson_results").document(resultId)

        userRef.get().addOnSuccessListener { document ->
            val completedLessons = document.get("completedLessons") as? List<String> ?: emptyList()
            val isFirstTime = !completedLessons.contains(lesson.id)

            db.runBatch { batch ->
                val resultData = hashMapOf(
                    "userId" to uid,
                    "lessonId" to lesson.id,
                    "chapterId" to lesson.chapterId,
                    "levelId" to lesson.levelId,
                    "completed" to true,
                    "completedAt" to FieldValue.serverTimestamp()
                )

                batch.set(resultRef, resultData)

                batch.update(userRef, "completedLessons", FieldValue.arrayUnion(lesson.id))

                if (isFirstTime) {
                    batch.update(userRef, "totalXP", FieldValue.increment(lesson.xpReward.toLong()))
                }
            }
        }
    }

}
