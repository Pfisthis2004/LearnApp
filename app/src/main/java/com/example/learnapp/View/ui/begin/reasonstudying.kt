package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.databinding.ActivityReasonstudyingBinding

class reasonstudying : AppCompatActivity() {
    lateinit var binding: ActivityReasonstudyingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityReasonstudyingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardFamily.setOnClickListener {
            val intent = Intent(this, choose_level::class.java)
            startActivity(intent)
        }
        binding.cardEntertainment.setOnClickListener {
            val intent = Intent(this, choose_level::class.java)
            startActivity(intent)
        }
        binding.cardStudy.setOnClickListener {
            val intent = Intent(this, choose_level::class.java)
            startActivity(intent)
        }
        binding.cardTrips.setOnClickListener {
            val intent = Intent(this, choose_level::class.java)
            startActivity(intent)
        }
        binding.cardWork.setOnClickListener {
            val intent = Intent(this, choose_level::class.java)
            startActivity(intent)
        }
    }
}