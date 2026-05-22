package com.example.learnapp.View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.ViewModel.DetailVocabViewModel
import com.example.learnapp.databinding.ActivityDetailVocabBinding
import java.util.Locale

class DetailVocabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailVocabBinding
    private val viewModel: DetailVocabViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null
    private var targetWord: String = ""
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailVocabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận dữ liệu
        val vocab = intent.getSerializableExtra("VOCAB_DATA") as? Vocabulary
        vocab?.let {
            targetWord = it.vocab
            binding.tvcontext.text = targetWord
            binding.tvTrans.text = it.translation
            binding.tvExample.text = it.example.ifEmpty { "Không có ví dụ" }
            binding.tvLevelValue.text = it.levelId
            val chapterNumber = vocab.chapterId.substringAfterLast("ch").filter { it.isDigit() }
            binding.tvReviewsValue.text = chapterNumber.ifEmpty { "1" }
            val lessonNumber = vocab.lessonId.substringAfterLast("l").filter { it.isDigit() }
            binding.tvlessonvalue.text = lessonNumber.ifEmpty { "1" }
            viewModel.loadInitialIpa(targetWord)
        }

        // 2. Setup Mic
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        setupSpeechListener()
        setupTouchListener()

        binding.btnback.setOnClickListener { finish() }
        setupObservers()
    }

    private fun setupTouchListener() {
        binding.recordMic.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    resetUI()
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    if (checkPermission()) {
                        startListening()
                        isListening = true
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    if (isListening) {
                        speechRecognizer?.stopListening()
                        isListening = false
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
        }
        speechRecognizer?.startListening(intent)
    }

    private fun setupSpeechListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {  }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { viewModel.processSpeechInput(targetWord, it) }
            }
            override fun onError(error: Int) {
                isListening = false
                Toast.makeText(this@DetailVocabActivity, "Thử lại nhé!", Toast.LENGTH_SHORT).show()
                viewModel.loadInitialIpa(targetWord)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupObservers() {
        viewModel.localIpaText.observe(this) { ipaText ->
            binding.tvSpeak.text = ipaText
            binding.layoutResult.visibility = View.GONE
            binding.tvFeedback.text = ""
            binding.tvSpeak2.text = ""
        }

        viewModel.evaluationResult.observe(this) { result ->
            // 1. Hiển thị kết quả phát âm (nhuộm màu chữ tiếng Anh)
            val builder = SpannableStringBuilder()
            result.wordComparisonDetails.forEach { item ->
                if (item.isWordCorrect) {
                    appendColoredText(builder, item.word, Color.parseColor("#2E7D32"))
                } else {
                    item.charComparison?.forEachIndexed { index, isCharCorrect ->
                        val char = item.word.getOrNull(index)?.toString() ?: ""
                        val color = if (isCharCorrect) Color.parseColor("#2E7D32") else Color.RED
                        appendColoredText(builder, char, color)
                    }
                }
                builder.append(" ")
            }
            binding.tvSpeak2.text = builder
            // 2. Hiển thị phiên âm chuẩn ở dòng mới (tvIPA)
            val ipaText = "/${result.targetIpaWords.joinToString(" ")}/"
            binding.tvIPA.text = ipaText

            binding.layoutResult.visibility = View.VISIBLE
            binding.tvScore.text = "Độ chính xác: ${result.similarityScore}%"

            // Phản hồi chi tiết
            if (result.similarityScore >= 90) {
                binding.tvFeedback.text = "🌟 Tuyệt vời! Bạn phát âm rất chuẩn."
                binding.tvFeedback.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                binding.tvFeedback.text = "💡 Xem các chữ màu đỏ để sửa lỗi nhé!"
                binding.tvFeedback.setTextColor(Color.parseColor("#EF6C00"))
            }
        }
    }
    private fun resetUI() {
        // Ẩn kết quả cũ
        binding.layoutResult.visibility = View.GONE
        // Xóa feedback
        binding.tvFeedback.text = ""
        // Reset tvSpeak2 về rỗng
        binding.tvSpeak2.text = ""
        // Có thể reset lại tvSpeak về trạng thái gốc nếu muốn
        viewModel.loadInitialIpa(targetWord)
    }
    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
            return false
        }
        return true
    }
    private fun appendColoredText(builder: SpannableStringBuilder, text: String, color: Int) {
        val start = builder.length
        builder.append(text)
        builder.setSpan(ForegroundColorSpan(color), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}