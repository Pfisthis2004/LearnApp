package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnapp.GeminiManager
import com.example.learnapp.Model.Chat.AIResponse
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ChatMessage
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val geminiManager = GeminiManager()
    private val _chatMessages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val chatMessages: LiveData<MutableList<ChatMessage>> get() = _chatMessages
    private val _goalStatus = MutableLiveData<List<Boolean>>()
    val goalStatus: LiveData<List<Boolean>> get() = _goalStatus
    private val _isFinished = MutableLiveData<Boolean>(false)
    val isFinished: LiveData<Boolean> get() = _isFinished
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _finalAnalysis = MutableLiveData<AIResponse?>()
    val finalAnalysis: LiveData<AIResponse?> get() = _finalAnalysis
    private val _suggestionText = MutableLiveData<String?>()
    val suggestionText: LiveData<String?> = _suggestionText
    private var conversationHistory = ""

    fun startConversation(config: ChatConfig) {
        _isLoading.value = true
        val header = config.openingHeader
        // 1. HIỆN TIN NHẮN USER TRƯỚC (Dùng item_mess_user)
        addMessageToUI(ChatMessage(text = header, sender = "USER"))

        viewModelScope.launch {
            // 2. Gửi Header này lên Gemini để AI phản hồi theo Setting
            val response = geminiManager.chatAndCheckGoals(header, config, "")
            _isLoading.postValue(false)

            response?.let {
//                // Lưu vào history để lần sau AI không quên vai
//                conversationHistory += "User: $header\nAI: ${it.reply}\n"
                processAIResponse(it)
            }
        }
    }

    fun sendUserMessage(userText: String, config: ChatConfig) {
        addMessageToUI(ChatMessage(text = userText, sender = "USER"))
        _isLoading.value = true
        viewModelScope.launch {
            val historyContext = getLimitedHistory()
            val response = geminiManager.chatAndCheckGoals(userText, config, historyContext)
            _isLoading.postValue(false)
            response?.let {
//                conversationHistory += "User: $userText\nAI: ${it.reply}\n"
                processAIResponse(it)
            }
        }
    }

    private fun processAIResponse(response: AIResponse) {
        val aiMsg = ChatMessage(
            text = response.reply,
            sender = "AI",
            translation = response.vi_trans,
            score = response.score
        )
        addMessageToUI(aiMsg)
        _goalStatus.postValue(response.goal_status)
        if (response.is_finished) _isFinished.postValue(true)
    }

    private fun addMessageToUI(message: ChatMessage) {
        // Tạo list mới để trigger observer của RecyclerView
        val currentList = _chatMessages.value?.toMutableList() ?: mutableListOf()
        currentList.add(message)
        _chatMessages.postValue(currentList)
    }
    private fun getLimitedHistory(): String {
        val allMessages = _chatMessages.value ?: return ""
        // Lấy 6 tin nhắn cuối cùng
        val limited = allMessages.takeLast(6)
        return limited.joinToString("\n") { msg ->
            "${msg.sender}: ${msg.text}"
        }
    }
    // Trong ChatViewModel.kt
    fun finishAndAnalyze(config: ChatConfig) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            // Gom tất cả tin nhắn User đã nói từ đầu buổi
            val historyContext = _chatMessages.value?.joinToString("\n") {
                "${it.sender}: ${it.text}"
            } ?: ""

            // Gọi hàm phân tích chuyên sâu đã có trong GeminiManager
            val result = geminiManager.generateFinalAnalysis(config, historyContext)

            _finalAnalysis.postValue(result)
            _isLoading.postValue(false)
            _isFinished.postValue(true)
        }
    }
    fun fetchAiSuggestion(config: ChatConfig) {
        viewModelScope.launch {
            // Gom lịch sử tin nhắn thành chuỗi văn bản
            val historyStr = getLimitedHistory()

            val result = geminiManager.getSuggestion(config, historyStr)
            _suggestionText.postValue(result)
        }
    }

    // Hàm để xóa gợi ý sau khi đã dùng
    fun clearSuggestion() {
        _suggestionText.value = null
    }
}