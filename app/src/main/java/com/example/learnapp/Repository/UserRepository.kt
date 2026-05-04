package com.example.learnapp.Repository

import com.example.learnapp.Model.Status
import com.example.learnapp.Model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    // Lấy thông tin User theo UID
    fun getUserProfile(uid: String, callback: (User?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { callback(it.toObject(User::class.java)) }
            .addOnFailureListener { callback(null) }
    }

    // Lấy tổng số bài học để tính %
    fun getTotalLessonsCount(callback: (Int) -> Unit) {
        db.collection("lessons").get()
            .addOnSuccessListener { callback(it.size()) }
            .addOnFailureListener { callback(0) }
    }

    // Reset tuần mới (Xóa mảng completedDays)
    fun resetWeeklyProgress(uid: String) {
        db.collection("users").document(uid).update("completedDays", emptyList<String>())
    }
    fun addCompletedDay(uid: String, dateStr: String, onComplete: () -> Unit) {
        db.collection("Users").document(uid)
            .update("completedDays", FieldValue.arrayUnion(dateStr))
            .addOnSuccessListener { onComplete() }
    }
    fun getChaptersByLevel(levelId: String, callback: (List<String>) -> Unit) {
        db.collection("chapters")
            .whereEqualTo("levelId", levelId)
            .get()
            .addOnSuccessListener { snapshot ->
                val chapterIds = snapshot.documents.map { it.id }
                callback(chapterIds)
            }
    }
    fun getVocabularyCount(uid: String, callback: (Int) -> Unit) {
        db.collection("users").document(uid)
            .collection("vocabularies") // Truy cập vào subcollection
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.size()) // Trả về số lượng document
            }
            .addOnFailureListener { callback(0) }
    }
    // Cập nhật số lượng chứng chỉ
    fun awardLevelCertificate(uid: String, levelId: String) {
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCertificates = snapshot.getLong("certificates") ?: 0

            // Tăng số lượng và thêm vào danh sách đã hoàn thành
            transaction.update(userRef, "certificates", currentCertificates + 1)
            transaction.update(userRef, "completedLevels", FieldValue.arrayUnion(levelId))
        }
    }
    fun updateProfileInfo(uid: String, newName: String, callback: (Status<Unit>) -> Unit) {
        val updates = mapOf("displayName" to newName)
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { callback(Status.Success(Unit)) }
            .addOnFailureListener { callback(Status.Error(it.message ?: "Cập nhật thất bại")) }
    }
}