package com.example.learnapp.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.learnapp.View.ui.fragment.*
import com.example.learnapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNav)

        // BƯỚC QUAN TRỌNG: Chỉ nạp Fragment lần đầu khi mở App
        // Nếu savedInstanceState != null (tức là xoay màn hình),
        // hệ thống sẽ tự khôi phục lại Fragment cũ, ta không được ghi đè.
        if (savedInstanceState == null) {
            setupInitialFragment()
        }

        // Thiết lập sự kiện click cho BottomNavigationView
        setupNavigation()
    }

    private fun setupInitialFragment() {
        val fromWelcome = intent.getBooleanExtra("fromWelcome", false)

        if (fromWelcome) {
            val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
            val selectedLevel = prefs.getString("selectedLevel", "Chưa chọn level")

            val fragment = LessonFragment()
            val bundle = Bundle()
            bundle.putString("selectedLevel", selectedLevel)
            fragment.arguments = bundle

            replaceFragment(fragment)
        } else {
            // Mặc định nạp màn hình Lesson khi mới vào app
            replaceFragment(LessonFragment())
        }
    }

    private fun setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.menu_home -> LessonFragment()
                R.id.menu_vocabulary -> VocabularyFragment()
                R.id.menu_ai -> AiFragment()
                R.id.menu_articles -> ArticlesFragment()
                R.id.menu_profile -> ProfileFragment()
                else -> null
            }

            fragment?.let {
                replaceFragment(it)
                true
            } ?: false
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        // Kiểm tra xem Fragment mới định nạp có trùng loại với Fragment đang hiện tại không
        // Để tránh việc nạp lại chính màn hình đó khi nhấn vào icon menu nhiều lần
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Thêm hiệu ứng chuyển cảnh cho mượt
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}