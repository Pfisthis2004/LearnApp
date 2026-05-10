package com.example.learnapp.View

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Status
import com.example.learnapp.R
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        setupUI()
        observeViewModel()

        // Gọi lấy dữ liệu ngay khi mở màn hình
        viewModel.fetchUserData(uid)
    }
    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Vô hiệu hóa ô Email và Premium vì thường không cho sửa trực tiếp ở đây
        binding.edtEmail.isEnabled = false
        binding.prenium.isEnabled = false

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.edtDisplayName.text.toString().trim()
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            viewModel.updateDisplayName(uid, name)
        }
    }

    private fun observeViewModel() {
        // 1. Quan sát dữ liệu User để đổ vào các ô nhập liệu
        viewModel.userData.observe(this) { user ->
            // Vì userData lúc này là kiểu User?, ta kiểm tra null trực tiếp
            user?.let {
                binding.apply {
                    // Điền tên hiện tại
                    edtDisplayName.setText(it.displayName)

                    // Điền Email (Chỉ đọc)
                    edtEmail.setText(it.email)

                    // Điền thông tin gói (ID là prenium theo XML của bạn)
                    prenium.setText(if (it.premium) "Gói: Premium" else "Gói: Miễn phí")

                    // Hiển thị ảnh đại diện mặc định của user
                    Glide.with(this@EditProfileActivity)
                        .load(it.photoURL)
                        .placeholder(R.drawable.user_avt)
                        .error(R.drawable.user_avt)
                        .into(imgEditAvatar)
                }
            }
        }

        // 2. Quan sát trạng thái Cập nhật (Vẫn dùng Status vì hàm update có báo Loading/Success/Error)
        viewModel.updateStatus.observe(this) { status ->
            when (status) {
                is Status.Loading -> {
                    binding.btnSaveProfile.isEnabled = false
                    // Bạn có thể thêm: binding.btnSaveProfile.text = "Đang lưu..."
                }
                is Status.Success -> {
                    Toast.makeText(this, "Đã lưu thay đổi thành công!", Toast.LENGTH_SHORT).show()
                    finish() // Đóng activity và quay về Setting
                }
                is Status.Error -> {
                    binding.btnSaveProfile.isEnabled = true
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}