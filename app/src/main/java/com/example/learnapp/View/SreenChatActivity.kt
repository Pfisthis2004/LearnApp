package com.example.learnapp.View

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Utils.SpeechToTextManager
import com.example.learnapp.Utils.TextToSpeechManager
import com.example.learnapp.View.ui.adapter.ChatAdapter
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.databinding.ActivitySreenChatBinding

class SreenChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySreenChatBinding
    private lateinit var sttManager: SpeechToTextManager
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: ChatViewModel by viewModels()
    private var currentConfig: ChatConfig? = null
    private var isNavigatingToResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySreenChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        ttsManager = TextToSpeechManager(this)

        // 1. Lấy dữ liệu và setup RecyclerView ngay lập tức
        currentConfig = intent.getParcelableExtra("CONFIG_KEY")
        currentConfig?.let { config ->
            binding.tvTitle.text = config.title
            setupRecyclerView(config)
            setupSTT(config)
            setupHelpButton()

            // 2. Bắt đầu hội thoại nếu là lần đầu vào màn hình
            if (viewModel.chatMessages.value.isNullOrEmpty()) {
                viewModel.startConversation(config)
            }
        }

        observeChatLogic()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView(config: ChatConfig) {
        chatAdapter = ChatAdapter(config){ textToSpeak ->
            val englishText = textToSpeak.split("|")[0].trim()
            ttsManager.speak(englishText)
        }
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@SreenChatActivity)
            // Tắt hiệu ứng nháy khi cập nhật Header
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSTT(config: ChatConfig) {
        sttManager = SpeechToTextManager(
            this,
            onResult = { text -> viewModel.sendUserMessage(text, config) },
            onError = { error -> Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_SHORT).show() }
        )

        binding.recordButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
                    sttManager.startListening()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    sttManager.stopListening()

                    binding.suggestBlock.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.suggestBlock.visibility = View.GONE
                            binding.suggestBlock.alpha = 1f // Reset alpha cho lần hiện sau
                            viewModel.clearSuggestion()
                        }.start()

                    true
                }
                else -> false
            }
        }
    }
    private fun setupHelpButton() {
        binding.helpButton.setOnClickListener {
            currentConfig?.let { config ->
                // 1. Hiệu ứng hiện khối gợi ý mượt mà
                binding.suggestBlock.visibility = View.VISIBLE
                binding.suggestBlock.alpha = 0f
                binding.suggestBlock.animate().alpha(1f).setDuration(200).start()

                // 2. Chữ chạy hoặc thông báo trạng thái sinh động
                binding.promptsuggest.text = "Đợi một tý..."

                // Vô hiệu hóa nút để tránh bấm liên tục
                binding.helpButton.isEnabled = false

                viewModel.fetchAiSuggestion(config)
            }
        }
    }
    private fun observeChatLogic() {
        // 1. Quản lý danh sách tin nhắn và cuộn
        viewModel.chatMessages.observe(this) { messages ->
            if (messages != null) {
                chatAdapter.submitList(messages)
                binding.chatRecyclerView.post {
                    if (chatAdapter.itemCount > 0) {
                        binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
                // Phát âm câu cuối nếu là AI
                messages.lastOrNull()?.let {
                    if (it.sender == "AI") {
                        // Tách lấy phần tiếng Anh để đọc, bỏ qua phần sau dấu "|"
                        val englishOnly = it.text.split("|")[0].trim()
                        ttsManager.speak(englishOnly)
                    }
                }
            }
        }

        // 2. Quản lý trạng thái mục tiêu (GỘP LẠI)
        viewModel.goalStatus.observe(this) { status ->
            if (status == null) return@observe

            chatAdapter.updateGoalStatus(status)

            // Chỉ gọi phân tích khi: có mục tiêu, tất cả xong, và chưa đang chuyển màn
            if (status.isNotEmpty() && status.all { it } && !isNavigatingToResult) {
                // Vô hiệu hóa nút bấm để tránh gửi tin nhắn khi đang tổng kết
                binding.recordButton.isEnabled = false
                binding.instructionText.text = "Đang tổng kết bài học..."

                currentConfig?.let { viewModel.finishAndAnalyze(it) }
            }
        }

        // 3. Đợi kết quả phân tích cuối cùng để chuyển màn
        viewModel.finalAnalysis.observe(this) { analysis ->
            if (analysis != null && !isNavigatingToResult) {
                isNavigatingToResult = true

                binding.root.postDelayed({
                    val intent = Intent(this, AIResultActivity::class.java).apply {
                        putExtra("SCORE", analysis.score)
                        putStringArrayListExtra("GOOD_SOUNDS", ArrayList(analysis.good_sounds))
                        putStringArrayListExtra("IMPROVE_SOUNDS", ArrayList(analysis.improve_sounds))
                        putStringArrayListExtra("GRAMMAR_ERRORS", ArrayList(analysis.grammar_errors))

                        putExtra("LESSON_TITLE", binding.tvTitle.text.toString())
                        putExtra("GOALS_STATUS", ArrayList(viewModel.goalStatus.value ?: emptyList<Boolean>()))
                        putExtra("GOALS_TEXT", ArrayList(currentConfig?.goals ?: emptyList<String>()))
                    }
                    startActivity(intent)
                    finish()
                }, 1500) // Delay nhẹ để người dùng kịp thấy mục tiêu cuối cùng tích xanh
            }
        }

        // 4. Các observer phụ khác
        viewModel.suggestionText.observe(this) { rawHint ->
            if (!rawHint.isNullOrEmpty()) {
                val cleanHint = rawHint.replace("[", "").replace("]", "").trim()
                if (cleanHint.contains("|")) {
                    val parts = cleanHint.split("|")
                    val english = parts[0].trim()
                    val vietnamese = parts.getOrNull(1)?.trim() ?: ""

                    binding.promptsuggest.text = english
                    binding.translationSuggest.text = vietnamese // TextView mới thêm vào suggestBlock
                } else {
                    // Phòng hờ AI quên dấu |
                    binding.promptsuggest.text = cleanHint
                    binding.translationSuggest.text = ""
                }
                binding.helpButton.isEnabled = true
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Chỉ cập nhật nếu chưa vào trạng thái kết thúc
            if (!isNavigatingToResult) {
                binding.recordButton.isEnabled = !isLoading
                binding.helpButton.isEnabled = !isLoading
                binding.recordButton.alpha = if (isLoading) 0.5f else 1.0f
                binding.instructionText.text = if (isLoading) "AI đang nghĩ..." else "Nhấn giữ và nhắc lại"
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    override fun onDestroy() {
        ttsManager.stop()
        ttsManager.shutDown()
        sttManager.destroy()
        super.onDestroy()
    }
}