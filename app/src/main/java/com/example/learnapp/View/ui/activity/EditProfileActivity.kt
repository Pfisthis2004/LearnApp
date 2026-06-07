package com.example.learnapp.View.ui.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Status
import com.example.learnapp.R
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

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
        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Hiển thị ảnh vừa chọn
            binding.imgEditAvatar.setImageURI(it)

            // Nén và encode sang Base64
            val base64Image = uriToCompressedBase64(this, it)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@let
            viewModel.updatePhoto(uid, base64Image)
        }
    }

    fun uriToCompressedBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Resize ảnh về kích thước nhỏ hơn (ví dụ 200x200 px cho avatar)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)

        // Nén ảnh với chất lượng 70% để giảm dung lượng
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
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
                    val photoData = it.photoURL
                    if (!photoData.isNullOrEmpty()) {
                        try {
                            // Thử decode Base64
                            val bytes = Base64.decode(photoData, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imgEditAvatar.setImageBitmap(bitmap)
                        } catch (e: IllegalArgumentException) {
                            // Nếu không phải Base64 thì coi là URL
                            Glide.with(this@EditProfileActivity)
                                .load(photoData)
                                .placeholder(R.drawable.user_avt)
                                .error(R.drawable.user_avt)
                                .into(imgEditAvatar)
                        }
                    } else {
                        imgEditAvatar.setImageResource(R.drawable.user_avt)
                    }
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