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
        if (currentUser != null) {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val firstLoginCompleted = prefs.getBoolean("firstLoginCompleted", false)

            if (firstLoginCompleted) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, welcome::class.java))
            }
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnnext.setOnClickListener {
            val intent = Intent(this, welcome::class.java)
            startActivity(intent)
        }
        binding.btnback.setOnClickListener {
            finish()
        }
    }
}