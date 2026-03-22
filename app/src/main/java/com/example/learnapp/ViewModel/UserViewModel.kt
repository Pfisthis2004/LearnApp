package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.DayStudy
import com.example.learnapp.Model.Status
import com.example.learnapp.Model.User
import com.example.learnapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth


import java.util.Calendar

class UserViewModel: ViewModel() {
    private val repository = UserRepository()

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    private val _updateStatus = MutableLiveData<Status<Unit>>()
    val updateStatus: LiveData<Status<Unit>> = _updateStatus
    private val _progressPercent = MutableLiveData<Int>()
    val progressPercent: LiveData<Int> = _progressPercent
    private val _vocabCount = MutableLiveData<Int>()
    val vocabCount: LiveData<Int> = _vocabCount
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun loadData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.postValue(true)
        repository.getUserProfile(uid) { user ->
            user?.let {
                // 1. Kiểm tra Reset tuần mới (Nếu là Thứ 2 và chưa reset)
                checkAndResetWeek(it)

                // 2. Cập nhật dữ liệu User
                _userData.value = it

                // 3. Tính toán % tiến độ
                repository.getTotalLessonsCount { total ->
                    val percent = if (total > 0) (it.completedLessons.size * 100) / total else 0
                    _progressPercent.value = percent
                    _isLoading.postValue(false)
                }
            }
        }
        repository.getVocabularyCount(uid) { count ->
            _vocabCount.value = count
        }
    }

    private fun checkAndResetWeek(user: User) {
        val now = Calendar.getInstance()
        val lastLogin = Calendar.getInstance().apply { timeInMillis = user.lastLoginAt }

        val isMonday = now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
        val isDifferentWeek = now.get(Calendar.WEEK_OF_YEAR) != lastLogin.get(Calendar.WEEK_OF_YEAR)

        if (isMonday && isDifferentWeek) {
            repository.resetWeeklyProgress(user.uid)
        }
    }

    fun getStudyDaysList(completedDays: List<String>): List<DayStudy> {
        val allDays = listOf("2", "3", "4", "5", "6", "7", "Cn")
        return allDays.map { DayStudy(it, completedDays.contains(it)) }
    }
    fun checkAndAwardCertificate(user: User, currentLevelId: String) {
        // 1. Kiểm tra xem Level này đã có chứng chỉ chưa
        if (user.completedLevels.contains(currentLevelId)) return

        repository.getChaptersByLevel(currentLevelId) { allChapterIdsInLevel ->
            // 2. Check xem User đã xong hết Chapter của Level đó chưa
            val isLevelFinished = user.completedChapters.containsAll(allChapterIdsInLevel)

            if (isLevelFinished && allChapterIdsInLevel.isNotEmpty()) {
                // 3. Gọi repository để vừa tăng số certificate, vừa add level đó vào mảng completedLevels
                repository.awardLevelCertificate(user.uid, currentLevelId)
            }
        }
    }
    fun fetchUserData(uid: String) {
        repository.getUserProfile(uid) { user ->
            _userData.value = user
        }
    }

    fun updateDisplayName(uid: String, newName: String) {
        if (newName.isBlank()) {
            _updateStatus.value = Status.Error("Tên không được để trống")
            return
        }
        _updateStatus.value = Status.Loading
        repository.updateProfileInfo(uid, newName) { _updateStatus.value = it }
    }
}