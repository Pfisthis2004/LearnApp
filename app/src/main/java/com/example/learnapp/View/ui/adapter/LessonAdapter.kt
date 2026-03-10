package com.example.learnapp.View.ui.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.R
import com.example.learnapp.View.QuestionActivity
import com.example.learnapp.View.SpeakingQuestionActivity

class LessonAdapter(
    private var lessons: MutableList<Lesson>,
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]

        Glide.with(holder.itemView.context)
            .load(lesson.icon)
            .placeholder(R.drawable.outline_disabled_by_default_24)
            .error(R.drawable.outline_disabled_by_default_24)
            .into(holder.baihoc)
        holder.tdbaihoc.text = lesson.title

        val status = lesson.isLocked ?: true

        if (status) {
            holder.lockIcon.visibility = View.VISIBLE
            holder.trangthai.text = "Đang khóa"
            holder.itemView.alpha = 0.5f
            holder.itemView.setOnClickListener(null)
        } else {
            holder.lockIcon.visibility = View.GONE
            holder.trangthai.text = "Có thể học"
            holder.itemView.alpha = 1.0f

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, QuestionActivity::class.java)

                intent.putExtra("chapterId", lesson.chapterId)
                intent.putExtra("id", lesson.id)
                context.startActivity(intent)

                onLessonClick(lesson)
            }

        }
    }

    override fun getItemCount(): Int {
        return lessons.size
    }


    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val baihoc: ImageView = view.findViewById(R.id.lessonIcon)
        val tdbaihoc: TextView = view.findViewById(R.id.lessonTitle)
        val trangthai: TextView = view.findViewById(R.id.lessonStatus)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }
}