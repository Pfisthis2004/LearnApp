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
import com.example.learnapp.View.ui.fragment.LessonFragment

class ChapterAdapter(
    private var chapters: List<Chapter>,
    private val onClickLesson: (Lesson) -> Unit)
    : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter,parent,false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChapterViewHolder,
        position: Int
    ) {
        val chapter = chapters[position]
        holder.chaptertv.text = "Chapter - ${position + 1}"
        holder.title.text = chapter.title
        //tinh so bai hoan thanh
        val completed = chapter.lessons.values.count { it.isCompleted }
        val total = chapter.lessons.size
        holder.process.text = "$completed/$total"
        // sap xep bai hoc
        val lessonList = chapter.lessons.entries.sortedBy { it.key }.map { it.value}
        holder.lessonrcv.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.lessonrcv.adapter = LessonAdapter(lessonList.toMutableList()){lesson ->
            onClickLesson(lesson)
        }
    }

    override fun getItemCount(): Int= chapters.size

    fun updateData(newChapters: List<Chapter>){
        chapters = newChapters
        notifyDataSetChanged()
    }
    class ChapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val chaptertv = view.findViewById<TextView>(R.id.chaptertv)
        val title = view.findViewById<TextView>(R.id.chapterTitle)
        val process = view.findViewById<TextView>(R.id.lessonCount)
        val lessonrcv= view.findViewById<RecyclerView>(R.id.rcvlesson)

    }
}