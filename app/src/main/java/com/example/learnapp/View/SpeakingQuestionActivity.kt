package com.example.learnapp.View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.learnapp.Model.SpeakingQuestion
import com.example.learnapp.R
import com.example.learnapp.ViewModel.SpeakingQuestionViewModel
import com.example.learnapp.databinding.ActivitySpeakingQuestionBinding

class SpeakingQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpeakingQuestionBinding
    private val viewModel: SpeakingQuestionViewModel by viewModels()
    private var player: ExoPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpeakingQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tạo player
        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        hideAllInputUI() // Ẩn mọi thứ khi mới vào activity

        val chapterId = intent.getStringExtra("chapterId") ?: return
        val id = intent.getStringExtra("id") ?: return
        viewModel.loadQuestions(chapterId, id)

        // Quan sát câu hỏi
        viewModel.currentIndex.observe(this) { index ->
            val questions = viewModel.questions.value ?: emptyList()
            if (questions.isNotEmpty() && index <= questions.size - 1) {
                // index còn nằm trong phạm vi câu hỏi
                showQuestion(questions[index])
            } else if (questions.isNotEmpty() && index == questions.size) {
                // đã làm xong hết câu hỏi
                showFinalResult()
            }
        }

        // Quan sát kết quả
        viewModel.result.observe(this) { result ->
            if (result != null) {
                binding.recordButton.visibility = View.GONE
                binding.instructionText.visibility = View.GONE
                binding.bottomFeedback.visibility = View.VISIBLE

                binding.tvResult.text = if (result.first) "HOÀN HẢO!" else "Sai rồi!"
                binding.tvResult.setTextColor(
                    if (result.first) getColor(R.color.colorSuccess) else Color.RED
                )

                binding.prompFeedback.text = "Câu chuẩn: ${result.second}"
            } else {
                binding.bottomFeedback.visibility = View.GONE
            }
        }

        // Nút ghi âm
        binding.recordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.instructionText.text = "Đang nghe..."
                    requestMicPermission() // xin quyền và bắt đầu ghi âm nếu được cấp
                    true
                }
                MotionEvent.ACTION_UP -> {
                    binding.instructionText.text = "Đang xử lý..."
                    // KHÔNG gọi stopSpeechRecognition ngay lập tức
                    // để recognizer tự gọi onEndOfSpeech và sau đó onResults
                    true
                }
                else -> false
            }
        }

        // Reload video
        binding.reloadButton.setOnClickListener {
            player?.seekTo(0)
            player?.play()
        }

        // Next question
        binding.btnNext.setOnClickListener {
            binding.bottomFeedback.visibility = View.GONE
            viewModel.nextQuestion()
        }

        binding.includeResult.btnContinueLesson.setOnClickListener {
            finish()
        }
    }

    private fun showQuestion(q: SpeakingQuestion) {

        hideAllInputUI() // Ẩn record + reload cho tới khi video chạy xong
        binding.main.visibility = View.VISIBLE
        binding.speakingPrompt.text = q.prompt
        binding.instructionText.text = "Nhấn giữ và nhắc lại"

        if (q.videoUrl.isNotEmpty()) {

            val mediaItem = MediaItem.fromUri(Uri.parse(q.videoUrl))
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
            player?.volume = 1f

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            // Video đã load xong, bắt đầu hiển thị prompt
                        }
                        Player.STATE_ENDED -> {
                            // Khi video kết thúc thì hiện nút record/reload
                            binding.speakingPrompt.text = q.prompt
                            binding.speakingPrompt.visibility = View.VISIBLE
                            binding.recordButton.visibility = View.VISIBLE
                            binding.reloadButton.visibility = View.VISIBLE
                            binding.instructionText.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

    private fun showFinalResult() {

        hideAllInputUI()

        // Ẩn toàn bộ phần học
        binding.main.visibility = View.GONE

        // Hiện kết quả
        binding.includeResult.root.visibility = View.VISIBLE
        binding.includeResult.tvScore.visibility = View.GONE
        binding.includeResult.tvStars.text = "Sao: +5 ⭐"

        val chapterId = intent.getStringExtra("chapterId") ?: return
        val lessonId = intent.getStringExtra("id") ?: return
        viewModel.unlockNextLesson(chapterId, lessonId)
    }

    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100 // requestCode tuỳ bạn đặt
            )
        } else {
            // Nếu đã có quyền thì bắt đầu ghi âm
            startSpeechRecognition()
        }
    }
    private fun hideAllInputUI() {
        binding.recordButton.visibility = View.GONE
        binding.reloadButton.visibility = View.GONE
        binding.instructionText.visibility = View.GONE
        binding.bottomFeedback.visibility = View.GONE
    }

    private fun startSpeechRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // tiếng Anh - Mỹ
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)     // lấy nhiều kết quả để so khớp
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()

                if (texts.isEmpty()) {
                    binding.instructionText.text = "Không nhận diện được, thử lại nhé!"
                    viewModel.checkAnswer("") // để tránh crash
                    return
                }

                val combined = texts.joinToString("|")
                viewModel.checkAnswer(combined)
            }

            override fun onPartialResults(partial: Bundle?) {
                val partialText = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                binding.instructionText.text = partialText ?: "Đang nghe..."
            }

            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> binding.instructionText.text = "Không nhận diện được giọng nói"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> binding.instructionText.text = "Bạn chưa nói gì!"
                    else -> binding.instructionText.text = "Lỗi ghi âm. Vui lòng thử lại."
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        speechRecognizer?.startListening(recognizerIntent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Được cấp quyền → bắt đầu ghi âm
                startSpeechRecognition()
            } else {
                // Bị từ chối
                binding.instructionText.text = "Bạn cần cấp quyền micro để sử dụng chức năng này"
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        speechRecognizer?.destroy()
    }
}