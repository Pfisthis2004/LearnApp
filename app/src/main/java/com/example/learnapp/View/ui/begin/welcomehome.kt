package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.View.LoginActivity
import com.example.learnapp.View.SignUpActivity
import com.example.learnapp.databinding.ActivityWelcomehomeBinding

class welcomehome : AppCompatActivity() {
    lateinit var binding: ActivityWelcomehomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWelcomehomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btndangnhap.setOnClickListener {
            val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
            prefs.edit().putBoolean("isFirstTime", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btndangky.setOnClickListener {
            val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
            prefs.edit().putBoolean("isFirstTime", false).apply()
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}