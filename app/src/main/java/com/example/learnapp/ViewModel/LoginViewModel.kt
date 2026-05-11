package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        if (email.isBlank()) {
            _errorMessage.value = "Email không được để trống"
            return
        }

        // 2. Kiểm tra mật khẩu trống
        if (password.isBlank()) {
            _errorMessage.value = "Mật khẩu không được để trống"
            return
        }

        // 3. Kiểm tra độ dài mật khẩu
        if (password.length < 8) {
            _errorMessage.value = "Mật khẩu phải có ít nhất 8 ký tự"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { saveUser(it) }
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
                    auth.currentUser?.let { saveUser(it) }
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = "Xác thực Google thất bại"
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

    private fun saveUser(firebaseUser: FirebaseUser) {
        val docRef = firestore.collection("users").document(firebaseUser.uid)

        // Kiểm tra xem user đã tồn tại chưa để tránh ghi đè dữ liệu học tập
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // NẾU ĐÃ CÓ: Chỉ cập nhật thời gian đăng nhập cuối
                docRef.update("lastLoginAt", System.currentTimeMillis())
            } else {
                // NẾU CHƯA CÓ: Tạo mới hoàn toàn
                val userData = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User",
                    photoURL = firebaseUser.photoUrl?.toString() ?: "",
                    createdAt = System.currentTimeMillis(),
                    totalXP = 0,
                    streak = 0,
                    role = "user",
                    lastLoginAt = System.currentTimeMillis(),
                    premium = false,
                    completedLessons = emptyList()
                )
                docRef.set(userData)
            }
        }.addOnFailureListener { e ->
            _errorMessage.value = "Lỗi kết nối Firestore: ${e.message}"
        }
    }
}
