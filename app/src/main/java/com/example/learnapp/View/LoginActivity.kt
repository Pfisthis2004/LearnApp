
package com.example.learnapp.View

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.R
import com.example.learnapp.View.ui.begin.language
import com.example.learnapp.ViewModel.LoginViewModel

import com.example.learnapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. KIỂM TRA AUTO LOGIN (Phải làm trước khi inflate layout để tránh nháy màn hình)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Nếu đã có User, đi thẳng vào App
            val intent = Intent(this, language::class.java)
            // Xóa Login khỏi Stack để không quay lại được
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Dừng các lệnh bên dưới lại
        }
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Quan sát ViewModel
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
        }

        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        // Đăng nhập bằng email/password
        binding.btncontinue.setOnClickListener {
            val email = binding.edtemail.text.toString()
            val password = binding.edtpassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loginWithEmail(email, password)

        }

        // Quên mật khẩu
        binding.btnforget.setOnClickListener {
            val email = binding.edtemail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.resetPassword(email)
        }

        // Cấu hình Google Sign-In
        setupGoogleSignIn()

        binding.btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
        binding.btnsignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
    private fun navigateToHome() {
        val intent = Intent(this, language::class.java)
        // FLAG_ACTIVITY_NEW_TASK và CLEAR_TASK sẽ xóa sạch(Stack)
        // Đảm bảo User không thể nhấn nút Back để quay lại màn hình Login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Đóng LoginActivity
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.loginWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}