package com.example.learnapp.View

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.learnapp.Model.Status
import com.example.learnapp.R
import com.example.learnapp.ViewModel.AuthViewModel
import com.example.learnapp.databinding.ActivitySettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        setupSwitches() // Thêm hàm xử lý Switch
        observeViewModel()
    }

    private fun setupListeners() {
        binding.layoutEditProfile.root.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.layoutChangePassword.root.setOnClickListener {
            // Chuyển sang Activity đổi mật khẩu (nếu có)
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener { showLogoutConfirmDialog() }
    }

    private fun setupSwitches() {
        val prefs = getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)

        // 1. Xử lý Switch Thông báo
        val isNotiEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.switchNotification.isChecked = isNotiEnabled
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            if (isChecked) {
                // bật thông báo: đăng ký topic all
                FirebaseMessaging.getInstance().subscribeToTopic("all")
                Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show()
            } else {
                // tắt thông báo: hủy đăng ký topic all
                FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                Toast.makeText(this, "Đã tắt thông báo", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Xử lý Switch Dark Mode
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }



    private fun setupUI() {
        // Nhóm Tài khoản
        binding.layoutEditProfile.apply {
            itemTitle.text = "Chỉnh sửa hồ sơ"
            itemIcon.setImageResource(R.drawable.user_avt)
        }
        binding.layoutChangePassword.apply {
            itemTitle.text = "Đổi mật khẩu"
            itemIcon.setImageResource(R.drawable.lock_icon) // Đảm bảo bạn có icon này
        }

        // Nhóm Hỗ trợ
        binding.layoutHelp.apply {
            itemTitle.text = "Trung tâm trợ giúp"
            itemIcon.setImageResource(R.drawable.icon_shield)
        }
        binding.layoutAboutApp.apply {
            itemTitle.text = "Về ứng dụng"
            itemIcon.setImageResource(R.drawable.icon_info)
        }
    }

    private fun observeViewModel() {
        viewModel.passwordResetStatus.observe(this) { resource ->
            when (resource) {
                is Status.Loading -> { /* Hiện ProgressBar nếu cần */ }
                is Status.Success -> {
                    Toast.makeText(this, "Thao tác thành công!", Toast.LENGTH_SHORT).show()
                }
                is Status.Error -> {
                    Toast.makeText(this, "Lỗi: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn thoát?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                viewModel.logout()
                // Xóa trạng thái thông báo cũ để user sau không bị trùng
                getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                navigateToLogin()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}