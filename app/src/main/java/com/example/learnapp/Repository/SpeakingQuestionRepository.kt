package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.SpeakingQuestion
import com.google.firebase.database.FirebaseDatabase

class SpeakingQuestionRepository {
    private val database = FirebaseDatabase.getInstance()
    private val lessonsRef = database.getReference("lessons")

    fun getQuestions(chapterId: String, id: String, callback: (List<SpeakingQuestion>) -> Unit) {
        lessonsRef.child(chapterId).child("lessons").child(id).child("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<SpeakingQuestion>()
                snapshot.children.forEach { qSnap ->
                    val q = qSnap.getValue(SpeakingQuestion::class.java)
                    q?.let { list.add(it) }
                }
                callback(list)
            }
            .addOnFailureListener { e ->
                callback(emptyList())
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