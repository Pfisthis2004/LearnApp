package com.example.learnapp.View

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.learnapp.R
import com.example.learnapp.databinding.ActivityDetailVocabBinding

class DetailVocabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailVocabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailVocabBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


}