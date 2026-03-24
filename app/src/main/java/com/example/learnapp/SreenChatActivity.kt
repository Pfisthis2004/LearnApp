package com.example.learnapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.C
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.learnapp.Model.Chat.ChatConfig
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
            ttsManager.speak(textToSpeak)
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
        sttManager = SpeechToTextManager(this,
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
                            binding.suggestBlock.visibility = android.view.View.GONE
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
                // 1. Hiển thị block và trạng thái chờ
                binding.suggestBlock.visibility = android.view.View.VISIBLE
                binding.promptsuggest.text = "Đợi một lát..."

                // 2. Gọi AI lấy gợi ý
                viewModel.fetchAiSuggestion(config)
            }
        }
    }
    private fun observeChatLogic() {
        viewModel.chatMessages.observe(this) { messages ->
            if (messages != null) {
                chatAdapter.submitList(messages)
                binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)

                // Chỉ phát âm câu cuối nếu là AI
                messages.lastOrNull()?.let {
                    if (it.sender == "AI") ttsManager.speak(it.text)
                }
            }
        }
        viewModel.suggestionText.observe(this) { hint ->
            if (!hint.isNullOrEmpty()) {
                binding.promptsuggest.text = hint
            }
        }

        viewModel.goalStatus.observe(this) { status ->
            chatAdapter.updateGoalStatus(status)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.recordButton.isEnabled = !isLoading
            binding.recordButton.alpha = if (isLoading) 0.5f else 1.0f
            binding.instructionText.text = if (isLoading) "AI đang nghĩ..." else "Nhấn giữ và nhắc lại"
        }

        viewModel.isFinished.observe(this) { finished ->
            if (finished) {
//                val intent = Intent(this, ResultActivity::class.java).apply {
//                    val lastAI = viewModel.chatMessages.value?.lastOrNull { it.sender == "AI" }
//                    putExtra("SCORE", lastAI?.score ?: 0)
//                    putExtra("GOALS_STATUS", ArrayList(viewModel.goalStatus.value ?: emptyList<Boolean>()))
//                    putExtra("GOALS_TEXT", ArrayList(currentConfig?.goals ?: emptyList<String>()))
//                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutDown()
        sttManager.destroy()
    }
}