package com.example.learnapp.View.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.learnapp.R
import com.example.learnapp.databinding.FragmentLessonBinding
import com.example.learnapp.databinding.FragmentVocabularyBinding

class VocabularyFragment : Fragment() {
    private lateinit var binding: FragmentVocabularyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vocabulary, container, false)

        binding.tabAll.setOnClickListener {

            binding.tabAll.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabFavorite.setBackgroundResource(0)

        }

        binding.tabFavorite.setOnClickListener {

            binding.tabFavorite.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabAll.setBackgroundResource(0)

        }
    }
}