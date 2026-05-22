package com.example.learnapp.Repository

import android.util.Log
import com.example.learnapp.Model.SpeechVocabResult
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.Model.WordComparison
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VocabularyRepository {
    private val db = FirebaseFirestore.getInstance()

    // Lắng nghe dữ liệu realtime từ Firestore
    fun getVocabularies(userId: String): Flow<List<Vocabulary>> = callbackFlow {
        val vocabRef = db.collection("users").document(userId).collection("vocabularies")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = vocabRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(Vocabulary::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }
    // Trong VocabularyRepository.kt
    suspend fun getAllVocabListOnce(userId: String): List<Vocabulary> {
        return try {
            db.collection("users").document(userId)
                .collection("vocabularies")
                .get()
                .await()
                .toObjects(Vocabulary::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun saveVocabularies(userId: String, list: List<Vocabulary>) {
            val batch = db.batch()
            val userVocabRef = db.collection("users").document(userId).collection("vocabularies")

            list.forEach { vocab ->
                val docRef = userVocabRef.document() // Firestore tự tạo ID
                batch.set(docRef, vocab.copy(id = docRef.id))
            }
            batch.commit().await() // Sử dụng .await() từ coroutines-play-services
    }
    suspend fun updateMultipleFavorites(userId: String, changes: Map<String, Boolean>) {
        val batch = db.batch() // Khởi tạo Batch
        val userVocabRef = db.collection("users").document(userId).collection("vocabularies")

        changes.forEach { (id, isFavorite) ->
            val docRef = userVocabRef.document(id)
            batch.update(docRef, "isFavorite", isFavorite)
        }

        // Thực thi tất cả lệnh trong Batch cùng lúc
        batch.commit().await()
    }
    fun getLocalIpa(phrase: String): String {
        val cleanPhrase = phrase.trim().lowercase().replace("[?.!]".toRegex(), "").trim()
        return when (cleanPhrase) {
            "hello" -> "həˈloʊ"
            "hi" -> "haɪ"
            "nice to meet you" -> "naɪs tə miːt juː"
            "what's your name" -> "wɒts jɔːr neɪm"
            "goodbye" -> "ˌɡʊdˈbaɪ"
            "bye" -> "baɪ"
            "take care" -> "teɪk keər"
            "see you later" -> "siː juː ˈleɪtər"
            "how are you" -> "haʊ ɑːr juː"
            "i'm fine, thanks" -> "aɪm faɪn θæŋks"
            "i'm great, thanks" -> "aɪm ɡreɪt θæŋks"
            "how's it going" -> "haʊz ɪt ˈɡoʊɪŋ"
            "and you" -> "ænd juː"
            "what about you" -> "wʌt əˈbaʊt juː"
            "not bad, thanks" -> "nɒt bæd θæŋks"
            "where are you from" -> "wer ɑːr juː frʌm"
            "i am from" -> "aɪ æm frʌm"
            "you are, you're" -> "juː ɑːr / jʊr"
            "i am, i'm" -> "aɪ æm / aɪm"
            "i'm turkish" -> "aɪm ˈtɜːrkɪʃ"
            "who is this" -> "huː ɪz ðɪs"
            "what is this" -> "wʌt ɪz ðɪs"
            "this is my colleague" -> "ðɪs ɪz maɪ ˈkɑːliːɡ"
            "i'm a manager" -> "aɪm ə ˈmænɪdʒər"
            "a team" -> "ə tiːm"
            "a colleague" -> "ə ˈkːliːɡ"
            "a client" -> "ə ˈklaɪənt"
            "he's he is" -> "hiːz / hiː ɪz"
            "she is she's" -> "ʃiː ɪz / ʃiːz"
            "it is it's" -> "ɪt ɪz / ɪts"
            else -> phrase.lowercase()
        }
    }
    fun analyzePronunciationAccuracy(targetWord: String, spokenText: String): SpeechVocabResult {
        val cleanTarget = targetWord.lowercase().replace("[?.!]".toRegex(), "")
        val cleanSpoken = spokenText.lowercase().replace("[?.!]".toRegex(), "")

        val targetWords = cleanTarget.split("\\s+".toRegex())
        val spokenWords = cleanSpoken.split("\\s+".toRegex())

        // Phân tích chi tiết từng từ
        val comparisonDetails = targetWords.mapIndexed { index, target ->
            val spoken = spokenWords.getOrNull(index) ?: ""

            if (target == spoken) {
                // Nếu từ khớp -> Đánh dấu đúng
                WordComparison(target, true)
            } else {
                // Nếu sai -> Tầng 2: So sánh ký tự để chỉ lỗi sai
                val charResults = target.mapIndexed { i, c ->
                    c == spoken.getOrNull(i)
                }
                WordComparison(target, false, charResults)
            }
        }

        return SpeechVocabResult(
            similarityScore = calculateSimilarityPercentage(cleanTarget, cleanSpoken),
            correctCount = comparisonDetails.count { it.isWordCorrect },
            totalCount = targetWords.size,
            wrongWordsList = comparisonDetails.filter { !it.isWordCorrect }.map { it.word },
            wordComparisonDetails = comparisonDetails,
            targetIpaWords = getLocalIpa(targetWord).replace("/", "").split("\\s+".toRegex())
        )
    }
    private fun calculateSimilarityPercentage(str1: String, str2: String): Int {
        val s1 = str1.trim().lowercase()
        val s2 = str2.trim().lowercase()
        if (s1 == s2) return 100
        if (s1.isEmpty() || s2.isEmpty()) return 0
        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                dp[j] = if (s1[i - 1] == s2[j - 1]) prev else minOf(dp[j] + 1, dp[j - 1] + 1, prev + 1)
                prev = temp
            }
        }
        return ((maxOf(s1.length, s2.length) - dp[s2.length]).toDouble() / maxOf(s1.length, s2.length) * 100).toInt()
    }
}