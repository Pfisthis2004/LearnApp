package com.example.learnapp.View

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.R
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.databinding.ActivityAiresultBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Timestamp

class AIResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiresultBinding
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiresultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận dữ liệu Object nếu là xem lại từ AiFragment
        val historyItem = intent.getParcelableExtra<HistoryItem>("HISTORY_ITEM")

        if (historyItem != null) {
            setupUI(historyItem)
        } else {
            // 2. Nếu không có Object, tức là vừa học xong -> Xử lý dữ liệu mới
            handleNewResult()
        }

        binding.btnHome.setOnClickListener {
            val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)

            if (isPreview) {
                // Nếu đang xem lại thì chỉ cần đóng màn hình này để về lại Fragment
                finish()
            } else {
                // Nếu vừa học xong:
                val intent = Intent(this, MainActivity::class.java)

                // CỰC KỲ QUAN TRỌNG:
                // Flag này sẽ "quét sạch" các Activity trung gian và quay về MainActivity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                startActivity(intent)
                // Không cần gọi finish() ở đây vì CLEAR_TOP đã tự đóng các thằng trên nó rồi
            }
        }
    }

    private fun setupUI(item: HistoryItem) {
        binding.tvCongrats.text = "Kết quả bài học"
        binding.btnHome.text = "QUAY LẠI"

        // Đổ dữ liệu vào UI
        displayResults(item.score, ArrayList(item.goalsStatus), ArrayList(item.goalsText))
        setupHighlights(item.goodSounds, item.improveSounds, item.grammarErrors)
    }

    private fun handleNewResult() {
        // Nhận dữ liệu từ Intent (gửi từ SreenChatActivity)
        val score = intent.getIntExtra("SCORE", 0)
        val title = intent.getStringExtra("LESSON_TITLE") ?: "N/A"
        val goalsText = intent.getStringArrayListExtra("GOALS_TEXT") ?: arrayListOf()
        val goalsStatus = intent.getSerializableExtra("GOALS_STATUS") as? ArrayList<Boolean> ?: arrayListOf()

        val goodSounds = intent.getStringArrayListExtra("GOOD_SOUNDS") ?: arrayListOf()
        val improveSounds = intent.getStringArrayListExtra("IMPROVE_SOUNDS") ?: arrayListOf()
        val grammarErrors = intent.getStringArrayListExtra("GRAMMAR_ERRORS") ?: arrayListOf()

        // Hiển thị UI ngay lập tức
        displayResults(score, goalsStatus, goalsText)
        setupHighlights(goodSounds, improveSounds, grammarErrors)

        // Lưu vào Firebase thông qua Repository
        val history = HistoryItem(
            lessonTitle = title,
            score = score,
            timestamp = Timestamp.now(),
            goalsText = goalsText,
            goalsStatus = goalsStatus,
            goodSounds = goodSounds,
            improveSounds = improveSounds,
            grammarErrors = grammarErrors
        )
        viewModel.saveHistory(history)
    }

    private fun displayResults(score: Int, status: ArrayList<Boolean>, texts: ArrayList<String>) {
        // Hiệu ứng số điểm nhảy
        ValueAnimator.ofInt(0, score).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener { binding.tvFinalScore.text = it.animatedValue.toString() }
            start()
        }

        // ProgressBar chạy
        val progress = if (texts.isNotEmpty()) (status.count { it } * 100) / texts.size else 0
        ValueAnimator.ofInt(0, progress).apply {
            duration = 1500
            addUpdateListener { binding.resultProgressBar.progress = it.animatedValue as Int }
            start()
        }

        // Hiển thị danh sách mục tiêu ✅/❌
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
            setBackgroundResource(R.drawable.bg_chip_item)
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 20, 20) }
        }
        flexLayout.addView(textView)
    }
}