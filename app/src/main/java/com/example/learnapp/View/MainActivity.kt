package com.example.learnapp.View

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.learnapp.View.ui.fragment.LessonFragment
import com.example.learnapp.View.ui.fragment.ProfileFragment
import com.example.learnapp.R
import com.example.learnapp.View.ui.fragment.VocabularyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val fromWelcome = intent.getBooleanExtra("fromWelcome", false)
        if (fromWelcome) {
            val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
            val selectedLevel = prefs.getString("selectedLevel", "Chưa chọn level")

            val fragment = LessonFragment()
            val bundle = Bundle()
            bundle.putString("selectedLevel", selectedLevel)
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit()
        } else {
            replaceFragment(LessonFragment())
        }

        bottomNavigationView = findViewById(R.id.bottomNav)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_home->{
                    replaceFragment(LessonFragment())
                    true
                }
                R.id.menu_vocabulary->{
                    replaceFragment(VocabularyFragment())
                    true
                }
                R.id.menu_profile->{
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout,fragment).commit()
    }
}