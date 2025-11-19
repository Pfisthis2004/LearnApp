package com.example.learnapp.View

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.R
import com.example.learnapp.databinding.ActivityChooseLevelBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class choose_level : AppCompatActivity() {
    lateinit var binding: ActivityChooseLevelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityChooseLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnchoose.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_choose_level,null)
            bottomSheetDialog.setContentView(view)
            val levelListView = view.findViewById<ListView>(R.id.levelListView)
            val levels = listOf("A1 - Beginning", "A2 - Elementary", "B1 - Intermediate", "B2 - Upper Intermediate", "C1 - Advanced")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, levels)
            levelListView.adapter = adapter

            levelListView.setOnItemClickListener { _, _, position, _ ->
                val selectedLevel = levels[position]

                // Lưu level vào SharedPreferences
                val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
                prefs.edit().putString("selectedLevel", selectedLevel).apply()

                // Mở màn hình welcome
                val intent = Intent(this, start_studying::class.java)
                startActivity(intent)
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }
    }
}