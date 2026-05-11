package com.example.learnapp.View

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.learnapp.Model.Status
import com.example.learnapp.R
import com.example.learnapp.ViewModel.AuthViewModel
import com.example.learnapp.databinding.ActivityChangePassBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ChangePass : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivityChangePassBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        setupPasswordToggle(
            listOf(binding.layoutCurrentPassword, binding.layoutNewPassword, binding.layoutConfirmPassword),
            listOf(binding.etCurrentPassword, binding.etNewPassword, binding.etConfirmPassword)
        )
        binding.btnSubmit.setOnClickListener {
            val current = binding.etCurrentPassword.text.toString().trim()
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()
            viewModel.updatePassword(current, newPass, confirm)
        }

        // Quan sát LiveData từ ViewModel
        viewModel.passwordResetStatus.observe(this) { result ->
            when (result) {
                is Status.Success -> {
                    binding.progressBar.visibility = View.GONE
                    AlertDialog.Builder(this)
                        .setTitle("Thành công")
                        .setMessage("Đổi mật khẩu thành công!")
                        .setPositiveButton("OK", null)
                        .show()
                    finish()
                }
                is Status.Error -> {
                    binding.progressBar.visibility = View.GONE
                    AlertDialog.Builder(this)
                        .setTitle("Lỗi")
                        .setMessage(result.message) // thông báo lỗi từ ViewModel
                        .setPositiveButton("OK") { _, _ ->
                            finish()
                        }
                        .show()
                }
                is Status.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun setupPasswordToggle(
        layouts: List<TextInputLayout>,
        edits: List<TextInputEditText>
    ) {
        layouts.forEachIndexed { index, layout ->
            layout.setEndIconOnClickListener {
                val editText = edits[index]
                val isVisible = editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                // Toggle cho ô hiện tại
                if (isVisible) {
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    layout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.eyeclosed)
                } else {
                    editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    layout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.eye)
                }
                editText.setSelection(editText.text?.length ?: 0)

            }
        }
    }
}