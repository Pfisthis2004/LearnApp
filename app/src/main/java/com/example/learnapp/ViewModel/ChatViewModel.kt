package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnapp.Utils.GeminiManager
import com.example.learnapp.Model.Chat.AIResponse
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ChatMessage
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.Model.Chat.ScenarioOption
import com.example.learnapp.Repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val _historyList = MutableLiveData<List<HistoryItem>>()
    val historyList: LiveData<List<HistoryItem>> get() = _historyList
    private val _chatMessages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val chatMessages: LiveData<MutableList<ChatMessage>> get() = _chatMessages
    private val _goalStatus = MutableLiveData<List<Boolean>>()
    val goalStatus: LiveData<List<Boolean>> get() = _goalStatus
    private val _isFinished = MutableLiveData<Boolean>(false)
    val isFinished: LiveData<Boolean> get() = _isFinished
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val _finalAnalysis = MutableLiveData<AIResponse?>()
    val finalAnalysis: LiveData<AIResponse?> get() = _finalAnalysis
    private val _suggestionText = MutableLiveData<String?>()
    val suggestionText: LiveData<String?> = _suggestionText
    private val _scenarios = MutableLiveData<List<ScenarioOption>?>()
    val scenarios: LiveData<List<ScenarioOption>?> get() = _scenarios
    private var conversationHistory = ""

    fun startConversation(config: ChatConfig) {
        _isLoading.value = true
        val header = config.openingHeader
        // 1. HIỆN TIN NHẮN USER TRƯỚC (Dùng item_mess_user)
        addMessageToUI(ChatMessage(text = header, sender = "USER"))

        viewModelScope.launch {
            // 2. Gửi Header này lên Gemini để AI phản hồi theo Setting
            val response = repository.fetchChatResponse(header, config, "")
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
            try {
                val historyContext = getLimitedHistory()
                val response = repository.fetchChatResponse(userText, config, historyContext)

                if (response != null) {
                    processAIResponse(response)
                } else {
                    // Trường hợp API trả về null (có thể do lỗi server AI)
                    _errorMessage.postValue("AI không phản hồi, vui lòng thử lại.")
                }
            } catch (e: Exception) {
                // Trường hợp mất mạng hoặc crash logic
                _errorMessage.postValue("Lỗi kết nối: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun processAIResponse(response: AIResponse) {
        val combinedText = "${response.reply} | ${response.vi_trans}"
        val aiMsg = ChatMessage(
            text = combinedText,
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
            val englishOnly = msg.text.split("|")[0].trim()
            "${msg.sender}: $englishOnly"
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
            val result = repository.fetchFinalAnalysis(config, historyContext)

            _finalAnalysis.postValue(result)
            _isLoading.postValue(false)
            _isFinished.postValue(true)
        }
    }
    fun fetchAiSuggestion(config: ChatConfig) {
        viewModelScope.launch {
            // Lấy 5-10 tin nhắn gần nhất để làm ngữ cảnh
            val limitHistory = _chatMessages.value?.takeLast(10)?.joinToString("\n") {
                "${it.sender}: ${it.text}"
            } ?: ""
            // Lấy danh sách trạng thái mục tiêu hiện tại (Ví dụ: [true, false, false])
            val currentGoalStatus = _goalStatus.value ?: emptyList()
            // Gọi Repository và truyền List<Boolean> vào
            val result = repository.fetchSuggestion(config, limitHistory, currentGoalStatus)

            _suggestionText.postValue(result ?: "Could not get suggestion.")
        }
    }

    fun createScenarios(idea: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.generateScenarios(idea) // repository gọi geminiManager.generateTwoScenarios
                _scenarios.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue("Không thể tạo kịch bản: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun deleteHistory(item: HistoryItem) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteHistoryItem(item)
            if (success) {
                // Xóa tạm thời trên UI để người dùng thấy mượt mà ngay lập tức
                val updatedList = _historyList.value?.filter { it.id != item.id }
                _historyList.postValue(updatedList ?: emptyList())
            }
            _isLoading.value = false
        }
    }
    fun saveHistory(item: HistoryItem) {
        repository.saveHistory(item)
    }
    init {
        loadHistory()
    }

    fun loadHistory() {
        repository.getHistoryList { list ->
            _historyList.postValue(list)
        }
    }
    // Hàm để xóa gợi ý sau khi đã dùng
    fun clearSuggestion() {
        _suggestionText.value = null
    }
    fun clearError() {
        _errorMessage.value = null
    }
    fun clearScenarios() { _scenarios.value = null }
}