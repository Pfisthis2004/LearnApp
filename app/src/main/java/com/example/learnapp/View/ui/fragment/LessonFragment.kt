package com.example.learnapp.View.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.example.learnapp.R
import com.example.learnapp.View.ui.adapter.ChapterAdapter
import com.example.learnapp.ViewModel.LessonViewModel
import com.example.learnapp.databinding.FragmentLessonBinding
import com.google.firebase.database.FirebaseDatabase

class LessonFragment : Fragment() {
    private lateinit var adapter: ChapterAdapter
    private val  viewModel: LessonViewModel by viewModels()
    private lateinit var binding: FragmentLessonBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLessonBinding.inflate(inflater, container, false)
        val view = binding.root

        val level = arguments?.getString("selectedLevel")
            ?: requireContext().getSharedPreferences("LearnAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("selectedLevel", "Chưa chọn level")
        view.findViewById<TextView>(R.id.leveltv).text = level

        adapter = ChapterAdapter(emptyList())
        binding.rcvchapter.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvchapter.adapter = adapter

        binding.loadingImage.visibility = View.VISIBLE
        Glide.with(this).asGif().load(R.raw.loading).into(binding.loadingImage)

        viewModel.chapters.observe(viewLifecycleOwner) { chapters ->
            adapter.updateData(chapters)
            binding.loadingImage.visibility = View.GONE
        }

        viewModel.loadChapters()
        return binding.root
    }
}