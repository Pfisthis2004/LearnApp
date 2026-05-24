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
            modelName = "gemini-3.1-flash-lite", //gemini-3.1-flash-lite, gemini-2.5-flash-lite,gemini-3-flash-preview
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
//            Log.d("GEMINI_RAW", "Data: $cleanJson")
            val result = gson.fromJson(cleanJson, AISelectionResponse::class.java)
            result.options
        }
    }

    /**
     * 3. Vòng lặp hội thoại & Kiểm tra mục tiêu
     */
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

        val proficiencyGuideline = when (config.level) {
            "Beginner" -> """
                # CEFR LEVEL: A1-A2 (BEGINNER MODE)
                - SPEAKING STYLE: Use very simple, common English words and short, clear sentences. Ask easy, direct questions. Keep responses under 20 words.
                - BEHAVIOR: Be highly encouraging. Focus on communication rather than perfection. Repeat or simplify questions if the user seems confused.
                - ERROR POLICY: Do NOT over-correct. Accept small grammar mistakes if the meaning is clear. Correct only communication-blocking errors naturally.
                - SCORING: Reward effort and clarity. Minor mistakes do not heavily reduce the score.
            """.trimIndent()

            "Intermediate" -> """
                # CEFR LEVEL: B1-B2 (INTERMEDIATE MODE)
                - SPEAKING STYLE: Use natural, daily conversational English with moderate sentence complexity. Introduce common phrasal verbs.
                - BEHAVIOR: Ask meaningful follow-up questions. Encourage storytelling, expressing opinions, and deeper explanations.
                - ERROR POLICY: Correct noticeable grammar mistakes naturally. Help improve sentence structures and vocabulary choices.
                - SCORING: Evaluate grammar accuracy, fluency, and vocabulary balance equally.
            """.trimIndent()

            "Advanced" -> """
                # CEFR LEVEL: C1-C2 (HARD / ADVANCED MODE)
                - SPEAKING STYLE: Use sophisticated, academic, and highly nuanced English. Implement complex sentence structures, idioms, and precise collocations.
                - BEHAVIOR: DO NOT use generic, warm greetings like 'Hey there'. Act like a strict, professional interviewer or thesis committee member. Go straight to the point. Challenge the user's reasoning, push for analytical thinking, and demand architectural or technical justifications.
                - ERROR POLICY: Actively identify and strictly penalize subtle grammar mistakes, awkward phrasal structures, or un-native expressions.
                - SCORING: Critically evaluate precision, fluency, and sophistication. Reduce points heavily for repetitive or simplistic vocabulary.
            """.trimIndent()

            else -> """
                # CEFR LEVEL: DEFAULT
                - Use simple, friendly English and maintain an easy-to-understand conversation flow.
            """.trimIndent()
        }

        val systemInstruction = """
            # ROLE & CONTEXT    
            - You are: ${config.botRole}
            - User is: ${config.userRole}
            - Scenario: ${config.situation}
            
            # PERSONALITY & TONE
            - Style: $personalityGuideline
            - Strategy: $attitudeGuideline
            
            $proficiencyGuideline
            
            # IMPORTANT LEVEL ADAPTATION
            - The conversation difficulty, vocabulary complexity, correction strictness, and scoring MUST adapt dynamically based on the provided CEFR guidelines.
            - Advanced mode requires immediate technical/logical pressure. Do not waste turns on casual pleasantries.
            
            # GOAL MANAGEMENT (QUAN TRỌNG)
            - User's Goals to achieve: [${config.goals.joinToString(", ")}]
            - Current Goal Status: (Evaluated from the chat history)
            
            # STRATEGY: ONE STEP AT A TIME
            1. **FOCUS**: At each turn, focus strictly on guiding the user to accomplish the FIRST uncompleted goal from the list.
            2. **STRICT EVALUATION**: Mark a goal as true ONLY if the user explicitly and fully performs the action. Do not guess or complete it for them.
            3. **GUIDANCE**: If the user goes off-topic, gently but firmly steer them back to the current target goal.
            4. **TRANSITION**: Do not rush. Only advance to the next goal after the current one is fully satisfied.
    
            # HUMAN-LIKE RULES (BẮT BUỘC)
            1. **NO REPETITION**: Never use standard template responses like "Oh, that's great" or "I see". Reply directly to the user's specific content.
            2. **NATURAL FILLERS**: Use conversational fillers like "Well," "Actually," "To be honest," "You know," or "From a critical standpoint..." to sound authentic.
            3. **DRIVE THE TALK**: Always introduce a fresh point or a targeted question based on the role context to keep the flow moving.
            
            # OUTPUT FORMAT RULES
            1. Language: ALWAYS reply in English for the "reply" field.
            2. Translation: Provide a natural Vietnamese translation for the "vi_trans" field.
            3. Length: Stay concise and natural (15-35 words).
            4. Exit: If all goals are verified as true, set "is_finished": true.
        """.trimIndent()

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

        // --- ĐẶT LOG ĐẦU VÀO LUỒNG CHAT ---
        Log.d("GEMINI_CHAT_INPUT", "====================== CHAT TURN START ======================")
        Log.d("GEMINI_CHAT_INPUT", "Level: ${config.level} | User Input: $userInput")
        Log.d("GEMINI_CHAT_INPUT", "Full Prompt Sent To AI:\n$fullPrompt")

        executeWithRetry {
            val response = getGenerativeModel().generateContent(fullPrompt)
            val cleanJson = cleanJsonResponse(response.text)

            // --- ĐẶT LOG ĐẦU RA LUỒNG CHAT ---
            Log.d("GEMINI_CHAT_OUTPUT", "Raw JSON Received:\n$cleanJson")
            Log.d("GEMINI_CHAT_OUTPUT", "====================== CHAT TURN END ========================")

            gson.fromJson(cleanJson, AIResponse::class.java)
        }
    }

    /**
     * Hàm lấy mẫu câu trợ giúp theo Level
     */
    suspend fun getSuggestion(
        config: ChatConfig,
        history: String,
        goalStatus: List<Boolean>
    ): String? = withContext(Dispatchers.IO) {
        val nextGoalIndex = goalStatus.indexOf(false)
        val nextGoal = config.goals.getOrNull(nextGoalIndex) ?: "Finish the conversation naturally"

        val shortHistory = history.lines().takeLast(6).joinToString("\n")

        val suggestionLevelGuideline = when (config.level) {
            "Beginner" -> """
                - Use extremely simple vocabulary (A1-A2).
                - Keep the sentence short, direct, and under 12 words.
                - Use basic grammar structures (Simple Present, Simple Past, or simple requests).
            """.trimIndent()

            "Intermediate" -> """
                - Use natural, daily conversational English (B1-B2).
                - Use moderate sentence complexity and common phrasal verbs.
                - Sentence length should be between 12-20 words.
            """.trimIndent()

            "Advanced" -> """
                - Use sophisticated, academic, and professional vocabulary (C1-C2).
                - Use advanced grammar structures (inversion, relative clauses, passive voice, formal collocations).
                - Provide complex, high-scoring arguments fitting for a professional interview.
            """.trimIndent()

            else -> "- Use simple and clear English fit for basic communication."
        }

        val prompt = """
            # CONTEXT
            Scenario: ${config.situation}
            User Role: ${config.userRole}
            Current Level: ${config.level}
          
            # TARGET GOAL
            The ultimate milestone to smoothly steer towards: "$nextGoal"
         
            # LEVEL GUIDELINES (MUST FOLLOW)
            $suggestionLevelGuideline
         
            # RECENT HISTORY
            $shortHistory
            
            # YOUR CRITICAL TASK
            Look closely at the LAST message from the AI/Bot in the RECENT HISTORY above. 
            Generate ONE ideal reply for the User that DIRECTLY answers or responds to that specific last message. 
            
            CRITICAL RULES:
            1. COHERENCE FIRST: The suggested sentence MUST feel like a natural, immediate continuation of the current conversation. Do NOT abruptly jump to a new topic or look only at the target goal.
            2. INTERACTION: Answer the Bot's question first, then optionally add a small detail or ask back to gently guide the conversation toward the "TARGET GOAL".
            3. Generate ONE ideal sentence in English according to the level guidelines above, and provide its natural Vietnamese translation.
            
            # OUTPUT FORMAT (MANDATORY)
            Return ONLY a valid JSON object with exactly two fields: "en" and "vi". Do not include markdown code blocks.
            {
              "en": "The generated English hint here",
              "vi": "Bản dịch tiếng Việt tương ứng ở đây"
            }
        """.trimIndent()

        // --- ĐẶT LOG ĐẦU VÀO HÀM SUGGESTION ---
        Log.d("GEMINI_SUGGEST_INPUT", "==================== SUGGESTION START ======================")
        Log.d("GEMINI_SUGGEST_INPUT", "Targeting Goal: $nextGoal | Level: ${config.level}")
        Log.d("GEMINI_SUGGEST_INPUT", "Prompt Sent To AI:\n$prompt")

        executeWithRetry {
            val response = getGenerativeModel().generateContent(prompt)
            val cleanJson = cleanJsonResponse(response.text)

            // --- ĐẶT LOG ĐẦU RA HÀM SUGGESTION ---
            Log.d("GEMINI_SUGGEST_OUTPUT", "Raw Suggestion JSON:\n$cleanJson")

            try {
                val jsonMap = gson.fromJson(cleanJson, Map::class.java)
                val english = jsonMap["en"]?.toString() ?: ""
                val vietnamese = jsonMap["vi"]?.toString() ?: ""

                val combinedResult = if (english.isNotEmpty() && vietnamese.isNotEmpty()) {
                    "$english | $vietnamese"
                } else {
                    null
                }

                Log.d("GEMINI_SUGGEST_OUTPUT", "Parsed Result: $combinedResult")
                Log.d("GEMINI_SUGGEST_OUTPUT", "==================== SUGGESTION END ========================")

                combinedResult
            } catch (e: Exception) {
                Log.e("GEMINI_SUGGEST_ERROR", "Lỗi phân tích JSON gợi ý: ${e.message}")
                null
            }
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
    suspend fun generateFinalAnalysis(config: ChatConfig, history: String): AIResponse? = withContext(Dispatchers.IO) {
        val finalPrompt = """
            # CONTEXT
            - Scenario: ${config.situation}
            - Level: ${config.level}
            - Learning Goals: [${config.goals.joinToString(", ")}]
            
            # FULL CONVERSATION HISTORY
            $history
            
            # TASK: FINAL EVALUATION
            Dựa trên TOÀN BỘ các câu nói của "User" trong lịch sử hội thoại trên, hãy thực hiện phân tích chuyên sâu:
            1. **Pronunciation (IPA)**: Liệt kê các ký hiệu âm tiết IPA (ví dụ: s, z, θ, ð, ɪ, i:,...) mà người dùng sử dụng chính xác (good_sounds) và các âm thường xuyên phát âm sai hoặc cần cải thiện (improve_sounds).
            2. **Grammar**: Nhận diện các loại lỗi ngữ pháp lặp lại (ví dụ: "Mạo từ", "Chia động từ số ít/số nhiều").
            3. **Final Score**: Đưa ra điểm số tổng kết trung bình cho cả quá trình (0-100).
            Dựa trên toàn bộ cuộc hội thoại, hãy tạo một bản phân tích JSON theo cấu trúc sau:
            {
              "reply": "Lời nhận xét tổng quát ngắn gọn bằng tiếng Việt",
              "score": integer (0-100),
              "good_sounds": ["âm đã làm tốt"],
              "improve_sounds": ["âm cần cải thiện"],
              "grammar_errors": ["lỗi ngữ pháp chính"],
              "vocab_suggestions": ["3 từ vựng nâng cao nên dùng"],
              "pronunciation_focus": ["các điểm trọng tâm cần luyện phát âm"],
              "level": "${config.level}"
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