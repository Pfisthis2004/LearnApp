package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LessonRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Trong LessonRepository.kt
    suspend fun getCompletedLessons(): List<String> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        return try {
            // Truy vấn trực tiếp từ collection kết quả bài tập
            val snapshot = firestore.collection("user_lesson_results")
                .whereEqualTo("userId", uid)
                .whereEqualTo("completed", true)
                .get()
                .await()

            // Lấy danh sách lessonId từ các document tìm được
            snapshot.documents.mapNotNull { it.getString("lessonId") }
        } catch (e: Exception) {
            Log.e("LessonRepository", "Error loading completed lessons: ${e.message}")
            emptyList()
        }
    }
    // Lấy lesson theo chapterId
    suspend fun fetchChaptersWithLessons(levelId: String): List<Chapter> {
        return try {
            // 1. Lấy Chapters theo Level
            val chapterSnapshot = firestore.collection("chapters")
                .whereEqualTo("levelId", levelId)
                .orderBy("order")
                .get()
                .await()

            val chapters = chapterSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Chapter::class.java)?.apply { id = doc.id }
            }

            // 2. Lấy Lessons cho từng Chapter
            for (chapter in chapters) {
                val lessonSnapshot = firestore.collection("lessons")
                    .whereEqualTo("chapterId", chapter.id)
                    .orderBy("order")
                    .get()
                    .await()

                chapter.lessons = lessonSnapshot.documents.mapNotNull { doc ->
                    val lessonObj = doc.toObject(Lesson::class.java)
                    if (lessonObj != null) {
                        lessonObj.id = doc.id
                        lessonObj.chapterId = doc.getString("chapterId") ?: ""
                        lessonObj.levelId = doc.getString("levelId") ?: ""

                        // Log kiểm tra từng bài học sau khi map xong
                        Log.d("DEBUG_FETCH", "Fetched Lesson: ${lessonObj.id}, Chapter: ${lessonObj.chapterId}, Level: ${lessonObj.levelId}")

                        lessonObj
                    } else {
                        Log.e("DEBUG_FETCH", "Không thể map document ${doc.id} sang Lesson object")
                        null
                    }
                }
            }
            chapters
        } catch (e: Exception) {
            Log.e("LessonRepository", "Error: ${e.message}")
            emptyList()
        }
    }
}