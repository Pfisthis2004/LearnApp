package com.example.learnapp.Repository

import com.example.learnapp.Model.Vocabulary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VocabularyRepository {
    private val db = FirebaseFirestore.getInstance()

    // Lắng nghe dữ liệu realtime từ Firestore
    fun getVocabularies(userId: String): Flow<List<Vocabulary>> = callbackFlow {
        val vocabRef = db.collection("users").document(userId).collection("vocabularies")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = vocabRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(Vocabulary::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }
    // Trong VocabularyRepository.kt
    suspend fun getAllVocabListOnce(userId: String): List<Vocabulary> {
        return try {
            db.collection("users").document(userId)
                .collection("vocabularies")
                .get()
                .await()
                .toObjects(Vocabulary::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    // Cập nhật trạng thái yêu thích
    suspend fun toggleFavorite(userId: String, vocabId: String, isFavorite: Boolean) {
        db.collection("users").document(userId)
            .collection("vocabularies").document(vocabId)
            .update("isFavorite", isFavorite).await()
    }
    suspend fun saveVocabularies(userId: String, list: List<Vocabulary>) {
            val batch = db.batch()
            val userVocabRef = db.collection("users").document(userId).collection("vocabularies")

            list.forEach { vocab ->
                val docRef = userVocabRef.document() // Firestore tự tạo ID
                batch.set(docRef, vocab.copy(id = docRef.id))
            }
            batch.commit().await() // Sử dụng .await() từ coroutines-play-services
    }
    suspend fun updateMultipleFavorites(userId: String, changes: Map<String, Boolean>) {
        val batch = db.batch() // Khởi tạo Batch
        val userVocabRef = db.collection("users").document(userId).collection("vocabularies")

        changes.forEach { (id, isFavorite) ->
            val docRef = userVocabRef.document(id)
            batch.update(docRef, "isFavorite", isFavorite)
        }

        // Thực thi tất cả lệnh trong Batch cùng lúc
        batch.commit().await()
    }
}