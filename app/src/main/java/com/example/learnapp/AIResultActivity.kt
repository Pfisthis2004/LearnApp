package com.example.learnapp

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.learnapp.databinding.ActivityAiresultBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AIResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiresultBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiresultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("SCORE", 0)
        val statusList = intent.getSerializableExtra("GOALS_STATUS") as? ArrayList<Boolean> ?: arrayListOf()
        val goalsText = intent.getSerializableExtra("GOALS_TEXT") as? ArrayList<String> ?: arrayListOf()
        val lessonTitle = intent.getStringExtra("LESSON_TITLE") ?: "Bài học"

        val goodSounds = intent.getStringArrayListExtra("GOOD_SOUNDS") ?: arrayListOf()
        val improveSounds = intent.getStringArrayListExtra("IMPROVE_SOUNDS") ?: arrayListOf()
        val grammarErrors = intent.getStringArrayListExtra("GRAMMAR_ERRORS") ?: arrayListOf()
        // Tính % hoàn thành
        val completedCount = statusList.count { it }
        val progressPercent = if (goalsText.size > 0) (completedCount * 100) / goalsText.size else 0
        // Hiển thị UI
        // TRONG onCreate - Sửa từ 4 tham số thành 3 tham số đúng thứ tự
        displayResults(score, statusList, goalsText)
        setupHighlights(goodSounds, improveSounds, grammarErrors)
        // Tự động lưu vào Firebase khi vào màn hình này
        saveProgressToFirebase(lessonTitle, score, progressPercent)

        binding.btnHome.setOnClickListener {
            finish() 
        }
    }

    private fun displayResults(score: Int, status: ArrayList<Boolean>, texts: ArrayList<String>) {
        // Hiệu ứng nhảy số điểm
        ValueAnimator.ofInt(0, score).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener { binding.tvFinalScore.text = it.animatedValue.toString() }
            start()
        }

        // ProgressBar chạy %
        val progress = if (texts.size > 0) (status.count { it } * 100) / texts.size else 0
        ValueAnimator.ofInt(0, progress).apply {
            duration = 1500
            addUpdateListener { binding.resultProgressBar.progress = it.animatedValue as Int }
            start()
        }

        // Danh sách mục tiêu
        val summary = StringBuilder()
        texts.forEachIndexed { i, goal ->
            val icon = if (i < status.size && status[i]) "✅" else "❌"
            summary.append("$icon $goal\n\n")
        }
        binding.tvGoalsSummary.text = summary.toString()
    }
    private fun setupHighlights(good: List<String>, improve: List<String>, grammar: List<String>) {
        binding.flexGoodSounds.removeAllViews()
        binding.flexImproveSounds.removeAllViews()
        binding.flexGrammarErrors.removeAllViews()

        // Đổ dữ liệu thật vào các ô (Màu xanh cho âm tốt, màu cam cho âm lỗi)
        good.forEach { addChipToFlex(binding.flexGoodSounds, it, "#4CAF50") }
        improve.forEach { addChipToFlex(binding.flexImproveSounds, "/$it/", "#FF9800") }
        grammar.forEach { addChipToFlex(binding.flexGrammarErrors, it, "#B39DDB") }
    }
    private fun addChipToFlex(flexLayout: FlexboxLayout, text: String, colorHex: String) {
        val textView = TextView(this).apply {
            this.text = text
            setTextColor(Color.parseColor(colorHex))
            setPadding(32, 16, 32, 16)
            textSize = 14f
            setBackgroundResource(R.drawable.bg_chip_item) // File drawable bo góc màu tím mờ

            val params = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 20, 20)
            layoutParams = params
        }
        flexLayout.addView(textView)
    }
    private fun saveProgressToFirebase(title: String, score: Int, progress: Int) {
        val userId = auth.currentUser?.uid ?: return // Nếu chưa login thì thoát

        val historyData = hashMapOf(
            "lessonTitle" to title,
            "score" to score,
            "progress" to progress,
            "timestamp" to Timestamp.now()
        )

        // Lưu vào sub-collection "history" của user đó
        db.collection("users").document(userId)
            .collection("history")
            .add(historyData)
            .addOnSuccessListener {
                android.util.Log.d("Firebase", "Lưu tiến độ thành công")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firebase", "Lỗi lưu tiến độ: ${e.message}")
            }
    }
}