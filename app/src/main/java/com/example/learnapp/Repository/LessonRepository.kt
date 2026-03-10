package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LessonRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Lấy chapter theo levelId
//    fun fetchChaptersByLevel(levelId: String, callback: (List<Chapter>) -> Unit) {
//
//        Log.d(TAG, "Lấy chapter theo levelId: $levelId")
//
//        firestore.collection("chapters")
//            .whereEqualTo("levelId", levelId)
//            .orderBy("order")
//            .get()
//            .addOnSuccessListener { snapshot ->
//
//                Log.d(TAG, "Số chapter tìm được: ${snapshot.size()}")
//
//                val chapters = snapshot.documents.mapNotNull { doc->
//                    val chapter =doc.toObject(Chapter::class.java)
//                    chapter?.copy(id = doc.id)
//                }
//
//                Log.d(TAG, "Sau khi chuyển sang object: ${chapters.size}")
//
//                callback(chapters)
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Lỗi khi lấy chapter theo level", e)
//            }
//    }

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
                    doc.toObject(Lesson::class.java)?.apply { id = doc.id }
                }
            }
            chapters
        } catch (e: Exception) {
            Log.e("LessonRepository", "Error: ${e.message}")
            emptyList()
        }
    }
}