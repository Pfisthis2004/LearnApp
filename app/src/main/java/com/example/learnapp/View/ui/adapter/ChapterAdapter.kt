package com.example.learnapp.View.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Chapter
import com.example.learnapp.Model.Lesson
import com.example.learnapp.R

class ChapterAdapter(
    private var chapters: List<Chapter>,
    private var completedLessons: List<String> = emptyList(),
    private val onClickLesson: (Lesson) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        val isFirstChapter = (position == 0)
        holder.chaptertv.text = "Chapter - ${position + 1}"
        holder.title.text = chapter.title
        holder.process.text = "${chapter.lessonCount} bài học"

        // Lấy lessons từ Firestore theo chapterId (truyền từ ViewModel/Fragment)
        val lessonAdapter = LessonAdapter(
            chapter.lessons.toMutableList(),
            completedLessons = completedLessons,
            isFirstInLevel = isFirstChapter
        ) { lesson ->
            onClickLesson(lesson)
        }
        holder.lessonrcv.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.lessonrcv.adapter = lessonAdapter
    }

    override fun getItemCount(): Int = chapters.size

    fun updateData(newChapters: List<Chapter>, newCompleted: List<String>) {
        chapters = newChapters
        completedLessons = newCompleted
        notifyDataSetChanged()
    }

    class ChapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chaptertv: TextView = view.findViewById(R.id.chaptertv)
        val title: TextView = view.findViewById(R.id.chapterTitle)
        val process: TextView = view.findViewById(R.id.lessonCount)
        val lessonrcv: RecyclerView = view.findViewById(R.id.rcvlesson)
    }
}