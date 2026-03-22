package com.example.learnapp.Repository

import com.example.learnapp.Model.Status
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    fun logout() {
        auth.signOut()
    }

    // Giả định đổi mật khẩu bằng email reset hoặc re-authenticate
    fun changePassword(newPassword: String, callback: (Status<Unit>) -> Unit) {
        val user = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnSuccessListener { callback(Status.Success(Unit)) }
            ?.addOnFailureListener { callback(Status.Error(it.message ?: "Lỗi không xác định")) }
    }
}