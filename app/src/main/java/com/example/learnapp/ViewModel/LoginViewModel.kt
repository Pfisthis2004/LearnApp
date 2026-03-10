package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = User(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: email.substringBefore("@"),
                            photoURL = user.photoUrl?.toString() ?: "",
                            createdAt = System.currentTimeMillis(),
                            totalXP = 0,
                            streak = 0,
                            lastLoginAt = System.currentTimeMillis(),
                            isPremium = false // mặc định false
                        )
                        saveUser(userData)
                    }
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = "Sai tài khoản hoặc mật khẩu"
                }
            }
    }

    fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = User(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "",
                            photoURL = user.photoUrl?.toString() ?: "",
                            createdAt = System.currentTimeMillis(),
                            totalXP = 0,
                            streak = 0,
                            lastLoginAt = System.currentTimeMillis(),
                            isPremium = false // mặc định false
                        )
                        saveUser(userData)
                    }
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = "Xác thực Firebase thất bại"
                }
            }
    }

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _errorMessage.value = if (task.isSuccessful)
                    "Đã gửi email đặt lại mật khẩu"
                else
                    "Không thể gửi email. Kiểm tra lại địa chỉ."
            }
    }

    private fun saveUser(user: User) {
        val docRef = firestore.collection("users").document(user.uid)

        // Dùng merge để không ghi đè dữ liệu cũ (ví dụ isPremium)
        docRef.set(user, SetOptions.merge())
            .addOnFailureListener { e ->
                _errorMessage.value = "Lỗi Firestore: ${e.message}"
            }
    }
}
