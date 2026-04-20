package com.example.learnapp.View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.ResultState
import com.example.learnapp.R
import com.example.learnapp.Repository.QuestionRepository
import com.example.learnapp.ViewModel.QuestionViewModel
import com.example.learnapp.ViewModel.QuestionViewModelFactory
import com.example.learnapp.databinding.ActivitySpeakingQuestionBinding

class SpeakingQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpeakingQuestionBinding

    // Giữ nguyên ViewModel chuẩn của đoạn 1 để tránh lỗi Factory
    private val viewModel: QuestionViewModel by viewModels {
        QuestionViewModelFactory(QuestionRepository())
    }

    private var player: ExoPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpeakingQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPlayer()

        val lessonId = intent.getStringExtra("id") ?: ""
        viewModel.loadQuestions(lessonId)

        // 1. Quan sát chỉ số câu hỏi
        viewModel.currentIndex.observe(this) { index ->
            val questions = viewModel.questions.value ?: return@observe
            if (index < questions.size) {
                showQuestion(questions[index])
            } else {
                showFinalResult()
            }
        }

        // 2. Quan sát kết quả từ ViewModel
        viewModel.result.observe(this) { res ->
            res?.let { handleResult(it) }
        }

        // 3. Thiết lập các nút bấm (Sử dụng OnTouch cho Mic)
        setupClickListeners()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        // Listener: Video kết thúc mới cho phép ghi âm (từ đoạn 2)
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    binding.recordButton.visibility = View.VISIBLE
                    binding.reloadButton.visibility = View.VISIBLE
                    binding.instructionText.text = "Nhấn giữ và nhắc lại"
                    binding.instructionText.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.recordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    player?.pause() // Dừng video khi bắt đầu nói
                    if (checkPermission()) startSpeechRecognition()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    speechRecognizer?.stopListening()
                    true
                }
                else -> false
            }
        }

        binding.reloadButton.setOnClickListener {
            player?.seekTo(0)
            player?.play()
        }

        binding.btnNext.setOnClickListener {
            binding.bottomFeedback.visibility = View.GONE
            viewModel.nextQuestion()
        }
    }

    private fun showQuestion(q: Question) {
        // Reset UI về trạng thái đợi video (từ đoạn 2)
        binding.speakingPrompt.text = q.prompt
        binding.recordButton.visibility = View.GONE
        binding.reloadButton.visibility = View.GONE
        binding.instructionText.text = "Đang nghe mẫu..."
        binding.bottomFeedback.visibility = View.GONE

        if (q.videoUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(q.videoUrl))
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        }
    }

    private fun startSpeechRecognition() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3) // Lấy 3 kết quả để so khớp chính xác hơn
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Hiện chữ khi đang nói
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) { binding.instructionText.text = "Đang nghe..." }

            override fun onPartialResults(partial: Bundle?) {
                // Hiển thị chữ tạm thời (từ đoạn 2)
                val partialText = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                binding.instructionText.text = partialText ?: "Đang nghe..."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val combined = matches?.joinToString("|") ?: ""
                viewModel.checkAnswer(combined)
            }

            override fun onError(error: Int) {
                binding.instructionText.text = "Lỗi micro hoặc không nghe thấy, thử lại!"
            }

            // Các hàm khác giữ trống
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    private fun handleResult(result: ResultState) {
        if (result is ResultState.SpeakingResult) {
            binding.bottomFeedback.visibility = View.VISIBLE
            binding.recordButton.visibility = View.GONE

            val isCorrect = result.isCorrect
            binding.tvResult.text = if (isCorrect) "HOÀN HẢO!" else "Thử lại nhé!"
            binding.tvResult.setTextColor(ContextCompat.getColor(this,
                if (isCorrect) R.color.colorSuccess else R.color.colorError))

            binding.prompFeedback.text = "Đáp án chuẩn: ${result.expected}"
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return false
        }
        return true
    }

    private fun showFinalResult() {
        binding.main.visibility = View.GONE
        binding.includeResult.root.visibility = View.VISIBLE
        val correct = viewModel.correctCount
        val total = viewModel.questions.value?.size ?: 0
        val scorePercent = if (total > 0) (correct * 100) / total else 0

        val lessonId = intent.getStringExtra("id") ?: ""
        val nextLessonId = intent.getStringExtra("nextLessonId") ?: ""
        val xp = intent.getIntExtra("xpReward", 0)
        val chapterId = intent.getStringExtra("chapterId") ?: ""
        val levelId = intent.getStringExtra("levelId") ?: ""

        // Cập nhật thông tin vào ViewModel
        val currentLesson = Lesson(
            id = lessonId,
            xpReward = xp,
            chapterId = chapterId,
            levelId = levelId
        )
        viewModel.setLessonInfo(currentLesson)

        // Thực thi lưu trữ (Ghi đè kết quả + Cộng XP nếu cần)
        viewModel.finishLesson(nextLessonId)

        binding.includeResult.tvScore.text = "Điểm của bạn: $scorePercent%"
        binding.includeResult.tvStars.text = "Phần thưởng: +$xp XP"
        binding.includeResult.btnContinueLesson.setOnClickListener { finish() }

    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        speechRecognizer?.destroy()
    }
}
