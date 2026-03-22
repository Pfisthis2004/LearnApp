package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Status
import com.example.learnapp.Repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _passwordResetStatus = MutableLiveData<Status<Unit>>()
    val passwordResetStatus: LiveData<Status<Unit>> = _passwordResetStatus

    fun logout() {
        repository.logout()
    }

    fun updatePassword(newPassword: String) {
        _passwordResetStatus.value = Status.Loading
        repository.changePassword(newPassword) {
            _passwordResetStatus.value = it
        }
    }
}