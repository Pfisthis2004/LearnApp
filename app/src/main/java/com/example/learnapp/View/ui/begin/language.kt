package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.View.LoginActivity
import com.example.learnapp.View.MainActivity
import com.example.learnapp.databinding.ActivityLanguageBinding
import com.google.firebase.auth.FirebaseAuth

class language : AppCompatActivity() {
    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val prefs = getSharedPreferences("AppPrefs_${currentUser?.uid}", MODE_PRIVATE)
        val firstLoginCompleted = prefs.getBoolean("firstLoginCompleted", false)
        if (currentUser != null) {
            if (firstLoginCompleted) {
                // Người dùng cũ, đã chọn level
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Người dùng mới hoặc chưa hoàn thành giới thiệu
                startActivity(Intent(this, welcome::class.java))
            }
            finish()
        }
//        if (currentUser != null) {
//            if (firstLoginCompleted) {
//                startActivity(Intent(this, MainActivity::class.java))
//            } else {
//                startActivity(Intent(this, welcome::class.java))
//            }
//            finish()
//        } else {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }

        binding.btnnext.setOnClickListener {
            val intent = Intent(this, welcome::class.java)
            startActivity(intent)
        }
        binding.btnback.setOnClickListener {
            finish()
        }
    }
}