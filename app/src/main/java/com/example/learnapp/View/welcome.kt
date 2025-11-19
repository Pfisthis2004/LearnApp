package com.example.learnapp.View

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.databinding.ActivityWelcomeBinding

class welcome : AppCompatActivity() {
    lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardflag.setOnClickListener {
            val intent = Intent(this, reasonstudying::class.java)
            startActivity(intent)
        }
        binding.cardEnglish.setOnClickListener {
            val intent = Intent(this, reasonstudying::class.java)
            startActivity(intent)
        }
    }
}