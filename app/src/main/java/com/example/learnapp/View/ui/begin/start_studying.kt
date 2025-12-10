package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.View.MainActivity
import com.example.learnapp.databinding.ActivityStartStudyingBinding

class start_studying : AppCompatActivity() {
    lateinit var binding: ActivityStartStudyingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartStudyingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnletgo.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fromWelcome", true) // để phân biệt quay về từ welcome
            startActivity(intent)
            finish()
        }
    }
}