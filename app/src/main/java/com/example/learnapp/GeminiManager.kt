package com.example.learnapp

import com.example.learnapp.Model.Chat.AIResponse
import com.example.learnapp.Model.Chat.AISelectionResponse
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ScenarioOption
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager() {
    private val gson = Gson()
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite", //Gemini 3 Flash hoacGemini 2.5 Flash Lite
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.85f

        }
    )

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
            - "title", "description", "goals": TIẾNG VIỆT
            - "situation": TIẾNG ANH 
            - "botRole", "userRole": TIẾNG VIỆT ngắn gọn (Ví dụ: "Bác sĩ", "Học sinh")
            - "goals": PHẢI LÀ TIẾNG VIỆT ( Tối đa 3 mục tiêu).
            - "opening_header": Tự tạo một chuỗi định nghĩa vai diễn bằng Tiếng Anh.
                Cấu trúc: "A conversation between [Bot Role] (bot role) and [User Role] (user role) with topic: [Short Topic]"

            Cấu trúc JSON bắt buộc:
            {
              "options": [
                {
                  "title": "Tiêu đề hấp dẫn",
                  "description": "Câu chuyện dẫn dắt sinh động bằng tiếng Việt...",
                  "config": {
                    "title": "Tiêu đề hấp dẫn",
                    "situation": "Detailed English summary of the story for AI context",
                    "botRole": "Tên ngắn (Ví dụ: "Bác sĩ")",
                    "userRole": "Tên ngắn (Ví dụ: "Học sinh")",
                    "opening_header": "A conversation between Doctor (bot role) and Patient (user role) with topic: Check-up appointment",
                    "goals": ["Mục tiêu 1 bằng tiếng Việt", "Mục tiêu 2 bằng tiếng Việt","Mục tiêu 3 bằng tiếng Việt"]
                  }
                }
              ]
            }
        """.trimIndent()

        return@withContext try {
            val response = generativeModel.generateContent(prompt)
            val cleanJson = cleanJsonResponse(response.text)
            android.util.Log.d("GEMINI_RAW", "Data: $cleanJson")
            val result = gson.fromJson(cleanJsonResponse(response.text), AISelectionResponse::class.java)
            result.options
        } catch (e: Exception) {
            android.util.Log.e("GEMINI_ERROR", "Lỗi tạo kịch bản: ${e.message}")
            null
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
            - Learning Goals: [${config.goals.joinToString(", ")}]
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
                1. Language: ALWAYS reply in English. 
                2. Length: Natural and concise (15-35 words).
                3. Evaluation: Check the learning goals: [${config.goals.joinToString(", ")}]. 
                   Update "goal_status" (boolean array) based on the history.
                4. Flow: If userInput is "START_CONVERSATION_NOW", initiate naturally with a "hook" (câu dẫn dắt).
                5. Exit: If all goals are true, set "is_finished": true.
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
          "vi_trans": "Bản dịch tiếng Việt",
          "goal_status": [boolean, boolean, boolean],
          "is_finished": boolean,
          "score": integer (0-100 based on user's English level in this turn)
        }
    """.trimIndent()

        return@withContext try {
            val response = generativeModel.generateContent(fullPrompt)
            val cleanJson = cleanJsonResponse(response.text)
            gson.fromJson(cleanJson, AIResponse::class.java)
        } catch (e: Exception) {
            android.util.Log.e("GEMINI_CHAT_ERROR", "${e.message}")
            null
        }
    }

    /**
     * Hàm phụ dọn dẹp chuỗi JSON tránh lỗi format
     */
    private fun cleanJsonResponse(rawResponse: String?): String {
        if (rawResponse == null) return ""
        return rawResponse
            .replace("```json", "")
            .replace("```", "")
            .replace("`", "")
            .trim()
    }
    suspend fun getSuggestion(config: ChatConfig, history: String): String? = withContext(Dispatchers.IO) {
        val prompt = """
    # CONTEXT
    - Scenario: ${config.situation}
    - User's Role: ${config.userRole}
    - Bot's Role: ${config.botRole}
    - **LEARNING GOALS**: [${config.goals.joinToString(", ")}]
    
    # CURRENT HISTORY
    $history
    
    # TASK
    Suggest ONE natural English sentence for the user to say next.
    
    # CRITICAL RULE (QUAN TRỌNG)
    - The suggestion **MUST** help the user achieve one of the LEARNING GOALS above.
    - The sentence must be short, natural, and fit the current emotional tone of the chat.
    - Return ONLY the English sentence. No extra text, no quotes.
""".trimIndent()

        return@withContext try {
            val response = generativeModel.generateContent(prompt)
            val rawText = response.text ?: ""

            // 1. Làm sạch Markdown (```json ...) bằng hàm bạn đã có
            var cleanText = cleanJsonResponse(rawText)

            // 2. PHÒNG VỆ: Nếu AI lỡ tay trả về JSON { "suggestion": "abc" }
            // Ta sẽ kiểm tra nếu chuỗi bắt đầu bằng '{', thì cố gắng parse lấy trường reply/suggestion
            if (cleanText.trim().startsWith("{")) {
                try {
                    // Thử parse nhanh để lấy giá trị bên trong nếu nó là JSON
                    val jsonMap = gson.fromJson(cleanText, Map::class.java)
                    // Lấy giá trị đầu tiên bất kỳ trong map (thường là key "reply" hoặc "suggestion")
                    cleanText = jsonMap.values.firstOrNull()?.toString() ?: cleanText
                } catch (e: Exception) {
                    // Nếu parse lỗi thì cứ để nguyên để xử lý tiếp
                }
            }
            cleanText.trim().replace("\"", "") // Xóa dấu ngoặc kép nếu có
        } catch (e: Exception) {
            null
        }
    }
}