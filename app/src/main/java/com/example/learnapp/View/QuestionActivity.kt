package com.example.learnapp.View

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.learnapp.Model.Question
import com.example.learnapp.R
import com.example.learnapp.ViewModel.QuestionViewModel
import com.example.learnapp.databinding.ActivityQuestionBinding

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val viewModel: QuestionViewModel by viewModels()

    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()
        binding.videoView.player =player

        val chapterId = intent.getStringExtra("chapterId") ?: return
        val id = intent.getStringExtra("id") ?: return
        viewModel.loadQuestions(chapterId, id)

        viewModel.questions.observe(this) { list ->
            if (list.isEmpty()) {
                binding.tvPrompt.text = "Không có câu hỏi trong bài học này."
            } else {
                showQuestion(list[viewModel.currentIndex.value ?: 0])
            }
        }

        viewModel.currentIndex.observe(this) { index ->
            val list = viewModel.questions.value ?: return@observe
            if (index < list.size) {
                showQuestion(list[index])
            } else {
                val result = viewModel.result.value ?: Pair(0, list.size)
                showFinalResult(result.first,result.second)
            }
        }

        viewModel.finalVocabulary.observe(this) { vocabText ->
            if (vocabText.isNullOrBlank()) {
                binding.includeResult.tvVocabulary.visibility = View.GONE
            } else {
                binding.includeResult.tvVocabulary.visibility = View.VISIBLE
                binding.includeResult.tvVocabulary.text = vocabText
            }
        }

        binding.btnNext.setOnClickListener {
            binding.bottomFeedback.visibility = View.GONE
            viewModel.nextQuestion()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.includeResult.btnContinueLesson.setOnClickListener {
                finish()
        }
    }
    private fun showQuestion(q: Question) {
        binding.tvPrompt.text = q.prompt
        binding.btnOption1.text = q.options[0]
        binding.btnOption2.text = q.options[1]

        binding.btnOption1.setOnClickListener { checkAnswer(q, q.options[0]) }
        binding.btnOption2.setOnClickListener { checkAnswer(q, q.options[1]) }

        if (q.videoUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(q.videoUrl))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            player.volume = 1f
        }
    }

    private fun checkAnswer(q: Question, selected: String) {
        binding.bottomFeedback.visibility = View.VISIBLE
        val updatedPrompt = q.prompt.replace("____", selected)
        binding.tvPrompt.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
        binding.tvPrompt.text = updatedPrompt
        binding.tvExplanation.text = q.explanation

        if (selected == q.correctAnswer) {
            binding.tvResult.text = "Chính xác"
            binding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
            val totalCount = viewModel.questions.value?.size ?: 0
            viewModel.increaseCorrect(totalCount)
        } else {
            binding.tvResult.text = "Sai"
            binding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.colorError))
        }
    }

    private fun showFinalResult(correctCount: Int, totalCount: Int) {
        binding.main.visibility = View.GONE
        binding.includeResult.root.visibility = View.VISIBLE
        binding.includeResult.vocabularylist.visibility = View.VISIBLE

        val scorePercent = (correctCount * 100) / totalCount
        binding.includeResult.tvStars.text = "Sao: +5 ⭐"
        binding.includeResult.tvScore.text = "Điểm: $scorePercent%"

        val chapterId = intent.getStringExtra("chapterId") ?: return
        val lessonId = intent.getStringExtra("id") ?: return
        viewModel.fetchLessonCountAndSaveResult(chapterId, lessonId, correctCount, totalCount)


    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}