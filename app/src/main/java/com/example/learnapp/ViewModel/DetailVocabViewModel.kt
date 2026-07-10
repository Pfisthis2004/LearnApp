package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Repository.VocabularyRepository
import com.example.learnapp.Model.SpeechVocabResult // Import từ package Model mới tách

class DetailVocabViewModel : ViewModel() {

    private val repository = VocabularyRepository()

    private val _evaluationResult = MutableLiveData<SpeechVocabResult>()
    val evaluationResult: LiveData<SpeechVocabResult> = _evaluationResult

    private val _localIpaText = MutableLiveData<String>()
    val localIpaText: LiveData<String> = _localIpaText

    fun loadInitialIpa(targetWord: String) {
        val ipa = repository.getLocalIpa(targetWord)
        _localIpaText.value = "/$ipa/"
    }

    fun processSpeechInput(targetWord: String, spokenText: String) {
        val result = repository.analyzePronunciationAccuracy(targetWord, spokenText)
        _evaluationResult.value = result
    }
}