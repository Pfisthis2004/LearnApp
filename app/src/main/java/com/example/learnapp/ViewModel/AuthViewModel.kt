package com.example.learnapp.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Status
import com.example.learnapp.Repository.AuthRepository
import com.example.learnapp.View.SettingActivity

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _passwordResetStatus = MutableLiveData<Status<Unit>>()
    val passwordResetStatus: LiveData<Status<Unit>> = _passwordResetStatus

    private var lastPassword: String? = null
    fun logout(context: Context) {
        repository.logout()
    }


    fun setLastPassword(password: String) {
        lastPassword = password
    }
    fun updatePassword(currentPassword:String, newPassword: String,confirmPassword:String) {
        // 1. Kiểm tra độ dài
        if (newPassword.length < 8) {
            _passwordResetStatus.value = Status.Error("Mật khẩu phải có ít nhất 8 ký tự")
            return
        }

        // 2. Kiểm tra trùng mật khẩu cũ
        if (lastPassword != null && newPassword == lastPassword) {
            _passwordResetStatus.value = Status.Error("Không được sử dụng mật khẩu đã dùng trước đó")
            return
        }

        if (newPassword != confirmPassword) {
            _passwordResetStatus.value = Status.Error("Mật khẩu xác nhận không khớp")
            return
        }
        _passwordResetStatus.value = Status.Loading
        repository.changePassword(currentPassword,newPassword) {
            _passwordResetStatus.value = it
            if (it is Status.Success) {
                // Cập nhật lại lastPassword để lần sau kiểm tra
                lastPassword = newPassword
            }
        }
    }
}