package com.example.learnapp.View

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.EditProfileActivity
import com.example.learnapp.Model.Status
import com.example.learnapp.R
import com.example.learnapp.ViewModel.AuthViewModel
import com.example.learnapp.databinding.ActivitySettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        setupListeners()
        observeViewModel()
    }
    private fun setupListeners() {
        // Sự kiện click Đổi mật khẩu
        binding.layoutChangePassword.root.setOnClickListener {
            // Chuyển sang Activity nhập mật khẩu mới
//            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        binding.layoutEditProfile.root.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        // Sự kiện click Đăng xuất
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    private fun observeViewModel() {
        // Nếu có xử lý đổi mật khẩu ngay tại đây
        viewModel.passwordResetStatus.observe(this) { resource ->
            when (resource) {
                is Status.Loading -> { /* Hiện ProgressBar */ }
                is Status.Success -> {
                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
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
    private fun setupUI() {
        // --- Nhóm Tài khoản ---
        binding.layoutEditProfile.apply {
            itemTitle.text = "Chỉnh sửa hồ sơ"
            itemIcon.setImageResource(R.drawable.user_avt) // Icon người dùng
        }
        binding.layoutChangePassword.apply {
            itemTitle.text = "Đổi mật khẩu"
        }


        // --- Nhóm Hỗ trợ ---
        binding.layoutHelp.apply {
            itemTitle.text = "Trung tâm trợ giúp"
            itemIcon.setImageResource(R.drawable.icon_shield) // Icon dấu hỏi
        }
        binding.layoutAboutApp.apply {
            itemTitle.text = "Về ứng dụng"
            itemIcon.setImageResource(R.drawable.icon_info) // Icon chữ i
        }

    }
}