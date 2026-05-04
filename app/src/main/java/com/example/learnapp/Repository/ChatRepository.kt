package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.Utils.GeminiManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val geminiManager = GeminiManager()
    fun getHistoryList(onResult: (List<HistoryItem>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FIRESTORE", "Listen failed.", error)
                    return@addSnapshotListener
                }

                // Duyệt từng document để lấy dữ liệu và gán ID
                val list = value?.documents?.mapNotNull { document ->
                    // Chuyển dữ liệu document thành object HistoryItem
                    val item = document.toObject(HistoryItem::class.java)

                    // Gán ID của document trên Firestore vào biến id trong Model
                    item?.apply { id = document.id }
                } ?: emptyList()

                onResult(list)
            }
    }

    fun saveHistory(item: HistoryItem) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("history")
            .add(item)
    }
    suspend fun deleteHistoryItem(item: HistoryItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false
            if (item.id.isNotEmpty()) {
                // Sửa lại đường dẫn cho đúng cấu trúc phân cấp
                db.collection("users")
                    .document(userId)
                    .collection("history")
                    .document(item.id)
                    .delete()
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("HISTORY_REPO", "Lỗi khi xóa: ${e.message}")
            false
        }
    }
    suspend fun generateScenarios(idea: String) = geminiManager.generateTwoScenarios(idea)

    suspend fun fetchChatResponse(input: String, config: ChatConfig, history: String) =
        geminiManager.chatAndCheckGoals(input, config, history)

    suspend fun fetchSuggestion(config: ChatConfig, history: String,goal: List<Boolean>) =
        geminiManager.getSuggestion(config, history,goal )

    suspend fun fetchFinalAnalysis(config: ChatConfig, history: String) =
        geminiManager.generateFinalAnalysis(config, history)
}