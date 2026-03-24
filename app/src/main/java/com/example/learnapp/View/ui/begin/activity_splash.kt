package com.example.learnapp.View.ui.begin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.learnapp.R
import com.example.learnapp.View.LoginActivity
import com.example.learnapp.View.MainActivity
import com.example.learnapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class activity_splash : AppCompatActivity() {
private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this)
            .asGif()
            .load(R.raw.topictalk) // Giả sử là hiệu ứng 3 dấu chấm
            .skipMemoryCache(true) // Không lưu vào RAM sau khi dùng
            .diskCacheStrategy(DiskCacheStrategy.NONE)// Không lưu vào bộ nhớ máy
            .into(binding.gifIntro)

        Handler(Looper.getMainLooper()).postDelayed({
            showLoadingState()
        }, 3000)
    }
    private fun showLoadingState() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.layoutLogo.visibility = View.GONE
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutLoading.startAnimation(slideUp)

        // SỬ DỤNG GLIDE ĐỂ LOAD GIF
        // Lưu ý: Thay 'your_loading_gif' bằng tên file gif trong thư mục drawable của bạn
        Glide.with(this)
            .asGif()
            .load(R.raw.loadingdot)
            .skipMemoryCache(true) // Không lưu vào RAM sau khi dùng
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.loadingImage)
        // Đợi 3.5 giây để chạy hiệu ứng loading rồi chuyển màn hình
        Handler(Looper.getMainLooper()).postDelayed({
            checkLogicAndNavigate()
        }, 3500)
    }
    private fun checkLogicAndNavigate() {
        val auth = FirebaseAuth.getInstance()
        val prefs = getSharedPreferences("LearnAppPrefs", MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("isFirstTime", true)

        val intent = when {
            auth.currentUser != null -> Intent(this, MainActivity::class.java)
            isFirstTime -> Intent(this, welcomehome::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Xóa các callback để tránh rò rỉ bộ nhớ khi đóng app đột ngột
        handler.removeCallbacksAndMessages(null)
    }
}