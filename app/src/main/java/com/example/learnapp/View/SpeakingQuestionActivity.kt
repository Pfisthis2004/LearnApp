package com.example.learnapp.View

import android.content.Intent
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
            if (questions.isNotEmpty() && index < questions.size) {
                showQuestion(questions[index])
            } else {
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
                    startSpeechRecognition()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    binding.instructionText.text = "Đang xử lý..."
                    stopSpeechRecognition()
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

    /** ====================== HIỆN CÂU HỎI ====================== **/
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
                    if (state == Player.STATE_ENDED) {
                        binding.recordButton.visibility = View.VISIBLE
                        binding.reloadButton.visibility = View.VISIBLE
                        binding.instructionText.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    /** ====================== FINAL RESULT ====================== **/
    private fun showFinalResult() {

        hideAllInputUI()

        // Ẩn toàn bộ phần học
        binding.main.visibility = View.VISIBLE

        // Hiện kết quả
        binding.includeResult.root.visibility = View.VISIBLE
        binding.includeResult.tvStars.text = "Sao: +5 ⭐"

        val chapterId = intent.getStringExtra("chapterId") ?: return
        val lessonId = intent.getStringExtra("id") ?: return
        viewModel.unlockNextLesson(chapterId, lessonId)
    }

    /** ====================== ẨN input khi mới vào / load câu ====================== **/
    private fun hideAllInputUI() {
        binding.recordButton.visibility = View.GONE
        binding.reloadButton.visibility = View.GONE
        binding.instructionText.visibility = View.GONE
        binding.bottomFeedback.visibility = View.GONE
        binding.includeResult.root.visibility = View.GONE
    }

    /** ====================== SPEECH RECOGNITION ====================== **/
    private fun startSpeechRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 0)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 0)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 0)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                viewModel.checkAnswer(text ?: "")
            }

            override fun onPartialResults(partial: Bundle?) {
                // Nếu muốn hiện text tạm thời cho user
                val partialText = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                binding.instructionText.text = partialText ?: "Đang nghe..."
            }

            override fun onError(error: Int) {
                viewModel.checkAnswer("")
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

    private fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
    }

    /** ====================== LIFECYCLE ====================== **/
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