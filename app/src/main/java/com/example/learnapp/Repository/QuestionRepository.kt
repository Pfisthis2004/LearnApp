package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Question
import com.google.firebase.database.FirebaseDatabase


class QuestionRepository {
    private val database = FirebaseDatabase.getInstance()
    private val lessonsRef = database.getReference("lessons")

    fun getQuestions(chapterId: String, id: String, callback: (List<Question>) -> Unit) {
        lessonsRef.child(chapterId).child("lessons").child(id).child("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Question>()
                snapshot.children.forEach { qSnap ->
                    val q = qSnap.getValue(Question::class.java)
                    q?.let { list.add(it) }
                }
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
    fun getLessonCount(chapterId: String, callback: (Int) -> Unit) {
        lessonsRef.child(chapterId).child("totalCount")
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.getValue(Int::class.java) ?: 0
                callback(count)
            }
            .addOnFailureListener {
                callback(0)
            }
    }
    fun unlockLesson(chapterId: String, lessonId: String) {
        val lessonRef = lessonsRef.child(chapterId).child("lessons").child(lessonId)
        lessonRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                lessonRef.child("isLocked").setValue(false)
            } else {
            }
        }
    }
    fun updateLessonStatus(chapterId: String, lessonId: String, nextLessonId: String) {
        val nextlessonRef = lessonsRef.child(chapterId).child("lessons").child(nextLessonId)
        lessonsRef.child(chapterId).child("lessons").child(lessonId)
            .child("isCompleted").setValue(true)
        nextlessonRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                nextlessonRef.child("isLocked").setValue(false)
            }
        }

    }
}