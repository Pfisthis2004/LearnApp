package com.example.learnapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.DayStudy
import com.example.learnapp.Model.Status
import com.example.learnapp.Model.User
import com.example.learnapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat


import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                checkAndResetStreak(it)
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
            Log.d("UserViewModel", "Đã reset tuần mới thành công")
            repository.resetWeeklyProgress(user.uid, now.timeInMillis) // Cập nhật timestamp ở đây
        }
    }
    // Hàm lấy danh sách 7 ngày của tuần hiện tại và kiểm tra xem ngày nào đã học
    private fun checkAndResetStreak(user: User) {
        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

        // Lấy ngày hôm qua
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

        val completedDays = user.completedDays ?: emptyList()

        // Nếu hôm nay chưa học VÀ hôm qua cũng không học -> Reset về 0
        if (!completedDays.contains(todayStr) && !completedDays.contains(yesterdayStr)) {
            if (user.streak > 0) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("streak", 0)
                    .addOnSuccessListener {
                        Log.d("STREAK_DEBUG", "Đã quá hạn 1 ngày, streak reset về 0")
                    }
            }
        }
    }
    fun getWeeklyStudyData(completedDays: List<String>?): List<DayStudy> {
        val dayList = mutableListOf<DayStudy>()
        val calendar = Calendar.getInstance()

        // Đưa calendar về ngày Thứ 2 của tuần này
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dayNames = listOf("2", "3", "4", "5", "6", "7", "Cn")

        for (i in 0..6) {
            val dateStr = dateFormat.format(calendar.time)
            // Kiểm tra xem ngày 'dateStr' (VD: 2026-04-20) có trong mảng dữ liệu Firebase không
            val isDone = completedDays?.contains(dateStr) ?: false
            Log.d("DayAdapterDebug", "dateStr=$dateStr, completedDays=$completedDays, isDone=$isDone")
            dayList.add(DayStudy(dayNames[i], isDone))
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Nhảy sang ngày tiếp theo
        }
        return dayList
    }
    fun markTodayAsLearned(onNewStreak: (Int) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val now = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

        // Tính ngày hôm qua để kiểm tra tính liên tục
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCal.time)

        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java) ?: return@addOnSuccessListener
                val completedDays = user.completedDays ?: mutableListOf()

                // 1. Nếu hôm nay chưa được ghi nhận học
                if (!completedDays.contains(today)) {
                    // Nếu có học hôm qua -> streak + 1
                    // Nếu hôm qua KHÔNG học -> reset streak về 1 (vì hôm nay mới bắt đầu lại)
                    val newStreak = if (completedDays.contains(yesterday)) {
                        user.streak + 1
                    } else {
                        1
                    }

                    val updates = hashMapOf(
                        "completedDays" to FieldValue.arrayUnion(today),
                        "lastLoginAt" to now.timeInMillis,
                        "streak" to newStreak
                    )

                    userRef.update(updates as Map<String, Any>).addOnSuccessListener {
                        Log.d("STREAK_DEBUG", "Cập nhật thành công! Streak mới: $newStreak")
                        onNewStreak(newStreak)
                        loadData() // Cập nhật lại các LiveData khác
                    }.addOnFailureListener { e ->
                        Log.e("STREAK_DEBUG", "Lỗi update: ${e.message}")
                    }
                } else {
                    // 2. Nếu hôm nay học rồi nhưng làm thêm bài nữa, chỉ trả về streak hiện tại, không tăng
                    Log.d("STREAK_DEBUG", "Hôm nay đã học rồi.")
                    onNewStreak(0) // Trả về 0 hoặc -1 để Fragment biết không hiện Dialog chúc mừng lần 2
                }
            }
        }
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
    fun loadUserDataRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Sử dụng snapshot listener để nhận dữ liệu ngay khi Firestore thay đổi
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("UserViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _userData.value = user // Tự động kích hoạt Observer ở Fragment
                    Log.d("STREAK_DEBUG", "Realtime update: Streak = ${user?.streak}")
                }
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