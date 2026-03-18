package com.example.learnapp.View

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.Model.ArticleQuestion
import com.example.learnapp.R
import com.example.learnapp.ViewModel.ArticleQuizViewModel
import com.example.learnapp.databinding.ActivityArticleQuizBinding

class ArticleQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleQuizBinding
    private val viewModel: ArticleQuizViewModel by viewModels()

    private var selectedOptionText: String? = null
    private var selectedTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleId = intent.getStringExtra("ARTICLE_ID") ?: ""
        viewModel.loadQuestions(articleId)

        observeData()
        setupEvents()
    }

    private fun observeData() {
        // Quan sát danh sách câu hỏi
        viewModel.questions.observe(this) { list ->
            if (list.isNotEmpty()) {
                updateUI(list[viewModel.currentIndex.value ?: 0])
            }
        }

        // Quan sát chỉ số câu hỏi hiện tại
        viewModel.currentIndex.observe(this) { index ->
            viewModel.questions.value?.let { updateUI(it[index]) }
        }

        // Quan sát trạng thái lưu kết quả để hiện màn hình Final
        viewModel.isFinishSaved.observe(this) { saved ->
            if (saved == true) {
                showFinalResultUI()
            }
        }
    }

    private fun updateUI(question: ArticleQuestion) {
        binding.txtPrompt.text = question.prompt
        val currentIdx = viewModel.currentIndex.value ?: 0
        val total = viewModel.questions.value?.size ?: 1

        binding.tvStep.text = "${currentIdx + 1}/$total"
        binding.progressBar.progress = ((currentIdx + 1) * 100) / total

        // Xóa các option cũ và tạo mới động
        binding.options.removeAllViews()
        question.options.forEach { option ->
            val textView = TextView(this, null, 0, R.style.OptionButtonStyle).apply {
                text = option
                // FIX: Thêm LayoutParams để các nút match_parent (không bị cụm)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 30) // Khoảng cách dưới mỗi nút
                }

                setOnClickListener { selectOption(this, option) }
            }
            binding.options.addView(textView)
        }

        binding.btnCheck.text = "Check"
        binding.btnCheck.isEnabled = false
        // Reset trạng thái nút Check về màu xám khi chưa chọn gì
        binding.btnCheck.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)

        selectedOptionText = null
        selectedTextView = null
    }

    private fun selectOption(view: TextView, option: String) {
        // 1. Reset UI tất cả các option về trạng thái mặc định
        for (i in 0 until binding.options.childCount) {
            val child = binding.options.getChildAt(i) as TextView
            child.setBackgroundResource(R.drawable.bg_option) // File mặc định viền xám
            child.setTextColor(Color.BLACK)
        }

        // 2. Highlight ô được chọn (Vòng tròn/Ô sáng lên)
        // FIX: Sử dụng bg_option_selected (viền tím/xanh) để thấy sự khác biệt
        view.setBackgroundResource(R.drawable.bg_option_selected)
        view.setTextColor(Color.parseColor("#673AB7"))

        selectedTextView = view
        selectedOptionText = option

        // Kích hoạt nút Check và đổi màu nút
        binding.btnCheck.isEnabled = true
        binding.btnCheck.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#673AB7"))
    }

    private fun setupEvents() {
        binding.btnBack.setOnClickListener { finish() }

        // Nút xem lại bài đọc nhanh
        binding.btnpeek.setOnClickListener {
            val content = intent.getStringExtra("ARTICLE_CONTENT") ?: "Nội dung bài đọc không khả dụng."

            val textView = TextView(this).apply {
                text = content
                textSize = 18f   // chỉnh kích thước chữ (sp)
                setPadding(32, 32, 32, 32) // padding cho đẹp
            }

            AlertDialog.Builder(this)
                .setTitle("Nội dung bài đọc")
                .setView(textView)
                .setPositiveButton("Đã hiểu", null)
                .show()
        }


        binding.btnCheck.setOnClickListener {
            val questions = viewModel.questions.value ?: return@setOnClickListener
            val currentIdx = viewModel.currentIndex.value ?: 0
            val currentQuestion = questions[currentIdx]

            if (binding.btnCheck.text == "Check") {
                // Kiểm tra đáp án
                if (selectedOptionText == currentQuestion.correctAnswer) {
                    viewModel.score++
                    selectedTextView?.setBackgroundResource(R.drawable.bg_options_true)
                    selectedTextView?.setTextColor(Color.BLACK)
                } else {
                    selectedTextView?.setBackgroundResource(R.drawable.bg_options_false)
                    selectedTextView?.setTextColor(Color.BLACK)
                    highlightCorrectAnswer(currentQuestion.correctAnswer)
                }

                // Kiểm tra xem có phải câu cuối không
                val isLast = currentIdx == questions.size - 1
                binding.btnCheck.text = if (isLast) "Finish" else "Next"

            } else if (binding.btnCheck.text == "Next") {
                viewModel.nextQuestion()
            } else {
                // Nhấn Finish
                processSubmit()
            }
        }
    }

    private fun highlightCorrectAnswer(correct: String?) {
        for (i in 0 until binding.options.childCount) {
            val child = binding.options.getChildAt(i) as TextView
            if (child.text == correct) {
                child.setBackgroundResource(R.drawable.bg_options_true)
                child.setTextColor(Color.BLACK)
            }
        }
    }

    private fun processSubmit() {
        val articleId = intent.getStringExtra("ARTICLE_ID") ?: ""
        val xpReward = intent.getIntExtra("XP_REWARD", 0)
        binding.btnCheck.isEnabled = false // Tránh nhấn nhiều lần
        viewModel.submitQuiz(articleId, xpReward)
    }

    private fun showFinalResultUI() {
        // Ẩn màn hình làm bài, hiện màn hình kết quả
        binding.layoutquiz.visibility = View.GONE
        binding.includeResult.root.visibility = View.VISIBLE

        val totalQuestions = viewModel.questions.value?.size ?: 0
        binding.includeResult.tvScore.text = "Đúng: ${viewModel.score}/$totalQuestions"

        val xpReward = intent.getIntExtra("XP_REWARD", 0)
        binding.includeResult.tvStars.text = "Phần thưởng: +$xpReward XP"

        binding.includeResult.btnContinueLesson.setOnClickListener {
            finish()
        }
    }
}