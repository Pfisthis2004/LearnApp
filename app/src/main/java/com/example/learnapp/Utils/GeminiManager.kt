package com.example.learnapp.Utils

import android.util.Log
import com.example.learnapp.BuildConfig
import com.example.learnapp.Model.Chat.AIResponse
import com.example.learnapp.Model.Chat.AISelectionResponse
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ScenarioOption
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager() {
    private val gson = GsonBuilder().setLenient().create()
    private val apiKeys = listOf(
        BuildConfig.GEMINI_API_KEY,
        BuildConfig.GEMINI_API_KEY_2,
        BuildConfig.GEMINI_API_KEY_3
    ).filter { it.isNotEmpty() }

    private var currentKeyIndex = 0

    // 2. Hàm khởi tạo Model linh hoạt theo Key hiện tại
    private fun getGenerativeModel(): GenerativeModel {
        val key = if (apiKeys.isNotEmpty()) apiKeys[currentKeyIndex] else ""
        return GenerativeModel(
            modelName = "gemini-3-flash-preview", //gemini-3.1-flash-lite-preview
            apiKey = key,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.85f
            }
        )
    }

    // 3. Cơ chế xoay vòng Key khi gặp lỗi 429 (Too Many Requests)
    private fun rotateKey(): Boolean {
        if (currentKeyIndex < apiKeys.size - 1) {
            currentKeyIndex++
            Log.d("GEMINI_AUTH", "Đã đổi sang API Key dự phòng số: ${currentKeyIndex + 1}")
            return true
        }
        Log.e("GEMINI_AUTH", "Đã thử hết tất cả các API Key nhưng đều hết hạn mức!")
        return false
    }
    /**
     * 1. Lấy kịch bản (Dùng tại SetupFragment)
     */
    suspend fun generateTwoScenarios(userIdea: String): List<ScenarioOption>? = withContext(Dispatchers.IO) {
        val prompt = """
            Dựa trên ý tưởng: "$userIdea", hãy tạo 2 kịch bản luyện nói tiếng Anh khác nhau.
            
            YÊU CẦU VỀ VĂN PHONG (Storytelling):
            - Phần "description" phải kể một câu chuyện ngắn có bối cảnh (thời gian, địa điểm, thời tiết), có biến cố bất ngờ và cảm xúc (Ví dụ: "Trời bỗng đổ mưa như trút nước", "Bất ngờ chạm mặt người lạ", "Bật cười vì đồng cảnh ngộ").
            - Độ dài description: 3-5 câu văn xuôi tiếng Việt sinh động.

            YÊU CẦU NGÔN NGỮ:
            - "roles": Mảng chứa 2 vai trò bằng TIẾNG VIỆT (Ví dụ: ["Bác sĩ", "Bệnh nhân"]).
            - "goals_for_roles": Mảng 2 chiều chứa danh sách mục tiêu (TIẾNG VIỆT) tương ứng cho từng vai trong mảng "roles".
                + goals_for_roles[0] dành cho roles[0]
                + goals_for_roles[1] dành cho roles[1]
            - "opening_header": Tự tạo một chuỗi định nghĩa vai diễn bằng Tiếng Anh.
               Sử dụng placeholder [Role0] và [Role1] (Ví dụ: "A conversation between [Role0] and [Role1] with topic: Health checkup").

            Cấu trúc JSON bắt buộc:
            {
              "options": [
                {
                  "title": "Tiêu đề hấp dẫn",
                  "description": "Câu chuyện dẫn dắt sinh động bằng tiếng Việt...",
                  "config": {
                    "title": "Tiêu đề hấp dẫn",
                    "situation": "Detailed English summary of the story for AI context",
                    "roles": ["Vai A", "Vai B"],
                    "goals_for_roles": [
                        ["Mục tiêu cho Vai A (1)", "Mục tiêu cho Vai A (2)","Mục tiêu cho Vai A (3)"],
                        ["Mục tiêu cho Vai B (1)", "Mục tiêu cho Vai B (2)","Mục tiêu cho Vai B (3)"]
                    ],
                    "opening_header": "A conversation between [Role0] and [Role1] with topic: ..."                 
                  }
                }
              ]
            }
        """.trimIndent()

        executeWithRetry {
            val response = getGenerativeModel().generateContent(prompt)
            val cleanJson = cleanJsonResponse(response.text)
            Log.d("GEMINI_RAW", "Data: $cleanJson")
            val result = gson.fromJson(cleanJson, AISelectionResponse::class.java)
            result.options
        }
    }

    /**
     * 3. Vòng lặp hội thoại & Kiểm tra mục tiêu
     */
    suspend fun chatAndCheckGoals(
        userInput: String,
        config: ChatConfig,
        history: String
    ): AIResponse? = withContext(Dispatchers.IO) {
        val personalityGuideline = if (config.personality == "Cheerful") {
            "Use a friendly, energetic, and casual tone. Act like a supportive friend."
        } else {
            "Use a professional, formal, and academic tone. Focus on correcting grammar and using sophisticated vocabulary."
        }

        val attitudeGuideline = if (config.attitude == "Supportive") {
            "Agree with the user's points and encourage them to keep speaking. Be empathetic."
        } else {
            "Challenge the user's ideas. Provide constructive counter-arguments or ask tough follow-up questions to test their logic."
        }
        // System Instruction ép AI tuân thủ luật chơi
        val systemInstruction = """
            # ROLE & CONTEXT    
            - You are: ${config.botRole}
            - User is: ${config.userRole}
            - Scenario: ${config.situation}
            
            # PERSONALITY & TONE
            - Style: $personalityGuideline
            - Strategy: $attitudeGuideline
            
            # GOAL MANAGEMENT (QUAN TRỌNG)
            - User's Goals to achieve: [${config.goals.joinToString(", ")}]
            - Current Goal Status: (Dựa trên lịch sử hội thoại)
            
            # STRATEGY: ONE STEP AT A TIME
            1. **FOCUS**: Tại mỗi lượt hội thoại, hãy chỉ tập trung dẫn dắt người dùng hoàn thành **DUY NHẤT MỘT** mục tiêu chưa hoàn thành theo thứ tự ưu tiên.
            2. **STRICT EVALUATION**: Chỉ đánh giá một mục tiêu là `true` nếu người dùng đã thực hiện hành động đó một cách rõ ràng và đầy đủ. Không được đoán ý hoặc tự hoàn thành thay người dùng.
            3. **GUIDANCE**: Nếu người dùng nói lạc đề, hãy khéo léo đặt câu hỏi hoặc gợi ý để họ quay lại mục tiêu hiện tại. 
            4. **NO RUSH**: Tuyệt đối không được xác nhận hoàn thành nhiều mục tiêu cùng lúc trong một câu trả lời.
            5. **TRANSITION**: Chỉ khi mục tiêu hiện tại đã đạt được (`true`), bạn mới được chuyển sang đặt câu hỏi dẫn dắt cho mục tiêu tiếp theo.
    
            # HUMAN-LIKE RULES (BẮT BUỘC)
                1. **PHẢN HỒI CỤ THỂ**: Tuyệt đối không lặp lại các câu vô thưởng vô phạt như "Oh, that's great" hoặc "I see" cho mọi tình huống. Hãy phản hồi trực tiếp vào nội dung người dùng vừa nói.
                2. **TỪ ĐỆM TỰ NHIÊN**: Sử dụng linh hoạt các cụm từ như "Well," "Actually," "To be honest," "You know," hoặc "I was thinking..." để giống văn nói.
                3. **DẪN DẮT**: Luôn cố gắng đưa ra một thông tin mới hoặc đặt một câu hỏi liên quan đến bối cảnh để mở rộng cuộc trò chuyện.
                4. **CẢM XÚC**: Thể hiện sự ngạc nhiên, đồng cảm hoặc nghi ngờ dựa trên vai diễn.
            
            # RULES
                1. Language: ALWAYS reply in English for the "reply" field.
                2. Translation: Provide a natural Vietnamese translation for the "vi_trans" field.
                3. Length: Natural and concise (15-35 words).
                4. Evaluation: Check the learning goals: [${config.goals.joinToString(", ")}]. 
                   Update "goal_status" (boolean array) based on the history.
                5. Flow: If userInput is "START_CONVERSATION_NOW", initiate naturally with a "hook" (câu dẫn dắt).
                6. Exit: If all goals are true, set "is_finished": true.
            """.trimIndent()

        // 3. Prompt gửi đi (ép kiểu trả về JSON)
        val fullPrompt = """
        $systemInstruction
        
        Current History:
        $history
        
        User's latest input: "$userInput"
        
        Return ONLY JSON format:
        {
          "reply": "Your message in English",
          "vi_trans": "Bản dịch tiếng Việt tương ứng",
          "goal_status": [boolean, boolean, boolean],
          "is_finished": boolean,
          "score": integer (0-100 based on user's English level in this turn)
        }
    """.trimIndent()

        executeWithRetry {
            val response = getGenerativeModel().generateContent(fullPrompt)
            val cleanJson = cleanJsonResponse(response.text)
            gson.fromJson(cleanJson, AIResponse::class.java)
        }
    }

    /**
     * Hàm phụ dọn dẹp chuỗi JSON tránh lỗi format
     */
    private fun cleanJsonResponse(rawResponse: String?): String {
        if (rawResponse == null) return ""

        // Loại bỏ rác bên ngoài dấu ngoặc nhọn (Gemini thỉnh thoảng thêm text giải thích)
        var cleaned = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .replace("`", "")
            .trim()

        // Tìm vị trí của dấu { đầu tiên và } cuối cùng để cắt đúng khối JSON
        val firstBracket = cleaned.indexOf('{')
        val lastBracket = cleaned.lastIndexOf('}')

        return if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            cleaned.substring(firstBracket, lastBracket + 1)
        } else {
            cleaned
        }
    }
//        suspend fun getSuggestion(config: ChatConfig, history: String): String? = withContext(Dispatchers.IO) {
//            val prompt = """
//            # CONTEXT
//            - Scenario: ${config.situation}
//            - User's Role: ${config.userRole}
//            - Bot's Role: ${config.botRole}
//            - **ALL LEARNING GOALS**: [${config.goals.joinToString(", ")}]
//
//            # CURRENT HISTORY
//            $history
//
//            # TASK
//            Suggest ONE natural English sentence for the user to say to achieve the NEXT uncompleted goal.
//
//            # CRITICAL RULES
//            - **LANGUAGE**: The output MUST be in English. No Vietnamese.
//            - **NO EXPLANATION**: Do not explain what to do. Just provide the line of dialogue.
//            - **EXAMPLE**: If the goal is "Ask for a coffee", return "Could I have a latte, please?".
//            - **LENGTH**: Under 20 words.
//
//            # OUTPUT
//            (Return ONLY the English sentence, no quotes)
//        """.trimIndent()
//
//        executeWithRetry {
//            val response = getGenerativeModel().generateContent(prompt)
//            val rawText = response.text ?: ""
//            var cleanText = cleanJsonResponse(rawText)
//
//            if (cleanText.trim().startsWith("{")) {
//                try {
//                    val jsonMap = gson.fromJson(cleanText, Map::class.java)
//                    cleanText = jsonMap.values.firstOrNull()?.toString() ?: cleanText
//                } catch (e: Exception) {}
//            }
//            cleanText.trim().replace("\"", "")
//        }
//    }

    suspend fun getSuggestion(config: ChatConfig, history: String, goalStatus: List<Boolean>): String? = withContext(Dispatchers.IO) {
        val nextGoalIndex = goalStatus.indexOf(false)
        val nextGoal = config.goals.getOrNull(nextGoalIndex) ?: "Finish the conversation naturally"
        val shortHistory = history.lines().takeLast(10).joinToString("\n")

        val prompt = """
            # CONTEXT
            Scenario: ${config.situation}
            User Role: ${config.userRole}
          
            # TARGET GOAL
            Only focus on suggesting to help the user accomplish this SINGLE goal "$nextGoal"
         
            # RECENT HISTORY
            $shortHistory
            
            # TASK
            Suggest ONE natural English sentence for the user to achieve the "Next Goal" AND its Vietnamese translation.
            
            # CRITICAL:
            - Format: [English sentence] | [Vietnamese translation]
            - Use the pipe character "|" to separate them.
            - DO NOT use brackets, quotes, or JSON.
            - Example: Could you help me with this? | Bạn có thể giúp tôi việc này không?
        """.trimIndent()
        // Giữ nguyên phần executeWithRetry của bạn
        executeWithRetry {
            val response = getGenerativeModel().generateContent(prompt)
            cleanJsonResponse(response.text).trim().replace("\"", "")
        }
    }

    suspend fun generateFinalAnalysis(config: ChatConfig, history: String): AIResponse? = withContext(Dispatchers.IO) {
        val finalPrompt = """
            # CONTEXT
            - Scenario: ${config.situation}
            - Learning Goals: [${config.goals.joinToString(", ")}]
            
            # FULL CONVERSATION HISTORY
            $history
            
            # TASK: FINAL EVALUATION
            Dựa trên TOÀN BỘ các câu nói của "User" trong lịch sử hội thoại trên, hãy thực hiện phân tích chuyên sâu:
            1. **Pronunciation (IPA)**: Liệt kê các ký hiệu âm tiết IPA (ví dụ: s, z, θ, ð, ɪ, i:,...) mà người dùng sử dụng chính xác (good_sounds) và các âm thường xuyên phát âm sai hoặc cần cải thiện (improve_sounds).
            2. **Grammar**: Nhận diện các loại lỗi ngữ pháp lặp lại (ví dụ: "Mạo từ", "Chia động từ số ít/số nhiều").
            3. **Final Score**: Đưa ra điểm số tổng kết trung bình cho cả quá trình (0-100).

            # OUTPUT FORMAT (JSON ONLY)
            {
              "reply": "Lời nhận xét tổng quát ngắn gọn bằng tiếng Việt về buổi học",
              "score": integer,
              "good_sounds": ["s", "i:", "t"],
              "improve_sounds": ["θ", "z", "r"],
              "grammar_errors": ["Mạo từ", "Chia động từ số ít/số nhiều"],
              "goal_status": [true, true, true],
              "is_finished": true
            }
        """.trimIndent()

        executeWithRetry {
            val response = getGenerativeModel().generateContent(finalPrompt)
            val cleanJson = cleanJsonResponse(response.text)
            gson.fromJson(cleanJson, AIResponse::class.java)
        }
    }
    private suspend fun <T> executeWithRetry(block: suspend () -> T): T? {
        var attempts = 0
        while (attempts < apiKeys.size) {
            try {
                return block()
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                // Bổ sung kiểm tra "503" hoặc "UNAVAILABLE" hoặc "demand" (từ thông báo lỗi bạn nhận được)
                val isRetryableError = errorMsg.contains("429") ||
                        errorMsg.contains("Quota") ||
                        errorMsg.contains("503") ||
                        errorMsg.contains("UNAVAILABLE") ||
                        errorMsg.contains("demand")

                if (isRetryableError) {
                    attempts++
                    Log.w("GEMINI_RETRY", "Gặp lỗi hệ thống hoặc Quota. Đang thử đổi Key... (Lần thử: $attempts)")
                    if (!rotateKey()) {
                        Log.e("GEMINI_CORE", "Đã thử hết tất cả các Key khả dụng.")
                        break
                    }
                    // Sau khi rotateKey, vòng lặp while sẽ chạy lại block() với getGenerativeModel() mới
                } else {
                    Log.e("GEMINI_CORE", "Lỗi nghiêm trọng không thể retry: $errorMsg")
                    break
                }
            }
        }
        return null
    }
}