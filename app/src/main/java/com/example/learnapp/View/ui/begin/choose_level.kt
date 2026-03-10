package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.R
import com.example.learnapp.Repository.LevelRepostitory
import com.example.learnapp.databinding.ActivityChooseLevelBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class choose_level : AppCompatActivity() {
    lateinit var binding: ActivityChooseLevelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChooseLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnchoose.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_choose_level, null)
            bottomSheetDialog.setContentView(view)
            val levelListView = view.findViewById<ListView>(R.id.levelListView)

            // Fetch levels từ Firestore
            val levelRepo = LevelRepostitory()
            levelRepo.fetchLevels { levels ->
                val levelAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    levels.map { it.title }
                )
                levelListView.adapter = levelAdapter

                levelListView.setOnItemClickListener { _, _, position, _ ->
                    val selectedLevel = levels[position]

                    // Lưu cả id và title vào SharedPreferences
                    val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("selectedLevelId", selectedLevel.id)
                        .putString("selectedLevelTitle", selectedLevel.title)
                        .apply()

                    // Mở màn hình start_studying
                    val intent = Intent(this, start_studying::class.java)
                    startActivity(intent)
                    bottomSheetDialog.dismiss()
                }
            }

            bottomSheetDialog.show()
        }
    }
}
