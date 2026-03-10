package com.example.learnapp.View

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.Model.ResultState
import com.example.learnapp.R
import com.example.learnapp.Repository.QuestionRepository
import com.example.learnapp.ViewModel.QuestionViewModel
import com.example.learnapp.ViewModel.QuestionViewModelFactory
import com.example.learnapp.databinding.ActivityQuestionBinding

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val viewModel: QuestionViewModel by viewModels {
        QuestionViewModelFactory(QuestionRepository())
    }
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        val lessonId = intent.getStringExtra("id") ?: ""
        viewModel.loadQuestions(lessonId)

        // Quan sát danh sách câu hỏi
        viewModel.questions.observe(this) { list ->
            if (list.isNotEmpty()) {
                val currentQ = list[viewModel.currentIndex.value ?: 0]
                if (currentQ.type == QuestionType.SPEAKING) {
                    val intent = Intent(this, SpeakingQuestionActivity::class.java)
                    intent.putExtras(this.intent)
                    startActivity(intent)
                    finish()
                } else {
                    showQuestion(currentQ)
                }
            }
        }

        // Quan sát chỉ số câu hỏi
        viewModel.currentIndex.observe(this) { index ->
            val list = viewModel.questions.value ?: return@observe
            if (index < list.size) {
                showQuestion(list[index])
            } else {
                showFinalResult()
            }
        }

        // Quan sát kết quả từ Strategy Pattern
        viewModel.result.observe(this) { res ->
            when (res) {
                is ResultState.FillBlankResult -> {
                    val isCorrect = res.isCorrect
                    binding.tvPrompt.setTextColor(
                        ContextCompat.getColor(this,
                            if (isCorrect) R.color.colorSuccess else R.color.colorError
                        )
                    )
                }
                is ResultState.QuizResult -> {
                    val isCorrect = res.correct > 0
                    binding.tvPrompt.setTextColor(
                        ContextCompat.getColor(this,
                            if (isCorrect) R.color.colorSuccess else R.color.colorError
                        )
                    )
                }
                else -> {}
            }

            showFeedback(res) // vẫn giữ phần feedback như cũ
        }


        binding.btnNext.setOnClickListener {
            binding.bottomFeedback.visibility = View.GONE
            viewModel.nextQuestion()
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.includeResult.btnContinueLesson.setOnClickListener{finish()}
    }

    private fun showQuestion(q: Question, selected: String? = null) {
        // Reset màu prompt về mặc định
        binding.tvPrompt.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Hiển thị câu hỏi, thay thế ____ nếu có lựa chọn
        val promptText = if (selected != null) {
            q.prompt.replace("____", selected)
        } else {
            q.prompt
        }
        binding.tvPrompt.text = promptText

        // Gán nội dung cho các nút lựa chọn
        binding.btnOption1.text = q.options.getOrNull(0) ?: ""
        binding.btnOption2.text = q.options.getOrNull(1) ?: ""

        // Xử lý khi người dùng chọn đáp án
        binding.btnOption1.setOnClickListener {
            val chosen = q.options[0]
            val updatedPrompt = q.prompt.replace("____", chosen)
            binding.tvPrompt.text = updatedPrompt
            viewModel.checkAnswer(chosen)
        }

        binding.btnOption2.setOnClickListener {
            val chosen = q.options[1]
            val updatedPrompt = q.prompt.replace("____", chosen)
            binding.tvPrompt.text = updatedPrompt
            viewModel.checkAnswer(chosen)
        }

        // Phát video nếu có
        if (q.videoUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(q.videoUrl))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }
    private fun showFeedback(res: ResultState?) {
        binding.bottomFeedback.visibility = View.VISIBLE
        when (res) {
            is ResultState.QuizResult -> {
                val isCorrect = res.correct > 0
                binding.tvResult.text = if (isCorrect) "Chính xác!" else "Sai rồi"
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(this,
                        if (isCorrect) R.color.colorSuccess else R.color.colorError
                    )
                )
                binding.tvExplanation.text = viewModel.questions.value?.get(viewModel.currentIndex.value ?: 0)?.explanation

            }
            is ResultState.FillBlankResult -> {
                binding.tvResult.text = if (res.isCorrect) "Đúng rồi!" else "Sai rồi"
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(this,
                        if (res.isCorrect) R.color.colorSuccess else R.color.colorError
                    )
                )
                binding.tvExplanation.text = viewModel.questions.value?.get(viewModel.currentIndex.value ?: 0)?.explanation
            }
            else -> {}
        }
    }

    private fun showFinalResult() {
        binding.main.visibility = View.GONE
        binding.includeResult.root.visibility = View.VISIBLE
        binding.includeResult.vocabularylist.visibility = View.VISIBLE

        val correct = viewModel.correctCount
        val total = viewModel.questions.value?.size ?: 0
        val scorePercent = if (total > 0) (correct * 100) / total else 0

        binding.includeResult.tvScore.text = "Điểm của bạn: $scorePercent%"
        binding.includeResult.tvStars.text = "Phần thưởng: +5 ⭐"

        val vocabList = viewModel.questions.value?.joinToString("\n") { q ->
            "${q.correctAnswer}: ${q.explanation}"
        } ?: ""
        binding.includeResult.tvVocabulary.text = vocabList

    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
