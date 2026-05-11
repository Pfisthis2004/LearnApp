package com.example.learnapp.Repository

import com.example.learnapp.Model.Status
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    fun logout() {
        auth.signOut()
    }

    // Giả định đổi mật khẩu bằng email reset hoặc re-authenticate
    fun changePassword(currentPassword: String, newPassword: String, callback: (Status<Unit>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Lưu timestamp hoặc mật khẩu cũ vào Firestore để kiểm tra lần sau
                            db.collection("users").document(user.uid)
                                .update("lastPassword", newPassword, "lastPasswordChangeAt", System.currentTimeMillis())

                            callback(Status.Success(Unit))
                        } else {
                            callback(Status.Error(updateTask.exception?.message ?: "Cập nhật thất bại"))
                        }
                    }
                } else {
                    callback(Status.Error("Mật khẩu hiện tại không đúng"))
                }
            }
        } else {
            callback(Status.Error("Không tìm thấy user"))
        }
    }

}