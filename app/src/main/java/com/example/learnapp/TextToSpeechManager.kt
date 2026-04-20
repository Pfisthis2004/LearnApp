package com.example.learnapp

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Thiết lập ngôn ngữ là tiếng Anh Mỹ
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ngôn ngữ không hỗ trợ")
            } else {
                isReady = true
                // Có thể chỉnh tốc độ nói (0.8 - 1.0 là vừa phải)
                tts?.setSpeechRate(0.9f)
            }
        } else {
            Log.e("TTS", "Khởi tạo thất bại")
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        }
    }
    fun stop() {
        tts?.stop() // Dừng phát ngay lập tức
    }
    fun shutDown() {
        tts?.stop()
        tts?.shutdown()
    }
}