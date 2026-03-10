package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Level
import com.google.firebase.firestore.FirebaseFirestore

class LevelRepostitory {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "LevelRepository"
    }

    fun fetchLevels(callback: (List<Level>) -> Unit) {

        Log.d(TAG, "Bắt đầu lấy danh sách Level")

        firestore.collection("levels")
            .orderBy("order")
            .get()
            .addOnSuccessListener { snapshot ->

                Log.d(TAG, "Firestore trả về ${snapshot.size()} level")

                val levels = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Level::class.java)?.copy(id = doc.id)
                }

                Log.d(TAG, "Sau khi chuyển sang model: ${levels.size} level")

                callback(levels)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi khi lấy Level từ Firestore", e)
            }
    }
}