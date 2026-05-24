package com.example.learnapp.View

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.R
import com.example.learnapp.View.ui.adapter.ErrorDetailAdapter
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.databinding.ActivityAiresultBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Timestamp

class AIResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiresultBinding
    private val viewModel: ChatViewModel by viewModels()
    private val userviewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiresultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận dữ liệu Object nếu là xem lại từ AiFragment
        val historyItem = intent.getParcelableExtra<HistoryItem>("HISTORY_ITEM")

        if (historyItem != null) {
            // Chuyển đổi list sang ArrayList nếu cần
            setupUI(historyItem, historyItem.score, ArrayList(historyItem.goalsStatus), ArrayList(historyItem.goalsText))
            setupHighlights(historyItem.goodSounds, historyItem.improveSounds, historyItem.grammarErrors)
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
                userviewModel.markTodayAsLearned { newStreakCount ->
                    if (newStreakCount > 0) {
                        // Nếu có streak mới -> Hiện Dialog (Dialog sẽ xử lý finish() sau khi bấm nút)
                        val prefs = getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putInt("pending_streak_count", newStreakCount).commit()
                    }
                }
                val intentHome = Intent(this, MainActivity::class.java)
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intentHome)
                finish()
            }
        }
    }

    private fun setupUI(item: HistoryItem,score: Int, status: ArrayList<Boolean>, texts: ArrayList<String>) {
        // 1. Header & Tổng quan
        binding.tvCongrats.text = "Kết quả đánh giá"
        binding.tvLevelBadge.text = "Level: ${item.level}"
        binding.tvOverallFeedback.text = item.overallFeedback
        binding.btnHome.text = "HOÀN THÀNH"

        // 2. Điểm số & Progress Bar
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

        // 4. Grammar (RecyclerView)
        val grammarAdapter = ErrorDetailAdapter(item.grammarErrors)
        binding.rvGrammarErrors.adapter = grammarAdapter
        binding.rvGrammarErrors.layoutManager = LinearLayoutManager(this)

        // 5. Vocabulary (Flexbox)
        binding.flexVocabSuggestions.removeAllViews()
        item.vocabSuggestions.forEach { suggestion ->
            addChipToFlex(binding.flexVocabSuggestions, suggestion, "#42A5F5")
        }

        // 6. Pronunciation (Flexbox)
        binding.flexPronunciationFocus.removeAllViews()
        item.pronunciationFocus.forEach { area ->
            addChipToFlex(binding.flexPronunciationFocus, area, "#42A5F5")
        }

    }

    private fun handleNewResult() {
        // 1. Thu thập dữ liệu từ Intent
        val score = intent.getIntExtra("SCORE", 0)
        val title = intent.getStringExtra("LESSON_TITLE") ?: "N/A"
        val goalsText = intent.getStringArrayListExtra("GOALS_TEXT") ?: arrayListOf()
        val goalsStatus = intent.getSerializableExtra("GOALS_STATUS") as? ArrayList<Boolean> ?: arrayListOf()

        val goodSounds = intent.getStringArrayListExtra("GOOD_SOUNDS") ?: arrayListOf()
        val improveSounds = intent.getStringArrayListExtra("IMPROVE_SOUNDS") ?: arrayListOf()
        val grammarErrors = intent.getStringArrayListExtra("GRAMMAR_ERRORS") ?: arrayListOf()

        // Tạo đối tượng hoàn chỉnh (Bạn cần truyền thêm các field mới từ Intent ở ScreenChatActivity)
        val history = HistoryItem(
            lessonTitle = title,
            score = score,
            timestamp = Timestamp.now(),
            goalsText = intent.getStringArrayListExtra("GOALS_TEXT") ?: emptyList(),
            goalsStatus = intent.getSerializableExtra("GOALS_STATUS") as? ArrayList<Boolean> ?: emptyList(),
            goodSounds = intent.getStringArrayListExtra("GOOD_SOUNDS") ?: emptyList(),
            improveSounds = intent.getStringArrayListExtra("IMPROVE_SOUNDS") ?: emptyList(),
            grammarErrors = intent.getStringArrayListExtra("GRAMMAR_ERRORS") ?: emptyList(),
            // MỚI: Thêm các field này vào Intent bên ScreenChatActivity
            level = intent.getStringExtra("LEVEL") ?: "Beginner",
            overallFeedback = intent.getStringExtra("FEEDBACK") ?: "",
            vocabSuggestions = intent.getStringArrayListExtra("VOCAB_SUGGESTIONS") ?: emptyList(),
            pronunciationFocus = intent.getStringArrayListExtra("PRONUNCIATION_FOCUS") ?: emptyList()
        )
        setupUI(history, score, goalsStatus, goalsText)
        setupHighlights(goodSounds, improveSounds, grammarErrors)
        // 3. Lưu vào Firebase
        viewModel.saveHistory(history)
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
            setTextColor(Color.WHITE)
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