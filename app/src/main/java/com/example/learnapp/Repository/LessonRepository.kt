package com.example.learnapp.Repository

import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.google.firebase.database.FirebaseDatabase

class LessonRepository {
    private val database = FirebaseDatabase.getInstance()
    private val chaptersRef = database.getReference("lessons")


    fun fetchChapters(callback: (List<Chapter>) -> Unit) {
        chaptersRef.get().addOnSuccessListener { snapshot ->
            val chapterList = mutableListOf<Chapter>()
            snapshot.children.forEach { chapterSnap ->
                val title = chapterSnap.child("title").getValue(String::class.java) ?: ""
                val completedCount = chapterSnap.child("completedCount").getValue(Int::class.java) ?: 0
                val totalCount = chapterSnap.child("totalCount").getValue(Int::class.java) ?: 0

                val lessonsMap = mutableMapOf<String, Lesson>()
                chapterSnap.child("lessons").children.forEach { lessonSnap ->
                    val lessonTitle = lessonSnap.child("title").getValue(String::class.java) ?: ""
                    val icon = lessonSnap.child("icon").getValue(String::class.java) ?: ""
                    val isCompleted = lessonSnap.child("isCompleted").getValue(Boolean::class.java) ?: false
                    val isLocked = lessonSnap.child("isLocked").getValue(Boolean::class.java) ?: false

                    val lesson = Lesson(lessonTitle, icon, isCompleted, isLocked)
                    lessonsMap[lessonSnap.key ?: ""] = lesson
                }

                val chapter = Chapter(title, completedCount, totalCount, lessonsMap)
                chapterList.add(chapter)
            }
            callback(chapterList)
        }
    }
}