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
import com.example.learnapp.R
import com.example.learnapp.View.QuestionActivity
import com.example.learnapp.View.SpeakingQuestionActivity
import com.example.learnapp.View.ui.fragment.LessonFragment

class LessonAdapter(private var lesson: MutableList<Lesson>,private val onLessonClick: (Lesson) -> Unit): RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_item, parent,false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LessonViewHolder,
        position: Int
    ) {
        val lesson= lesson[position]
        // Load ảnh từ Firebase URL
        Glide.with(holder.itemView.context)
            .load(lesson.icon) // URL ảnh từ Firebase
            .placeholder(R.drawable.outline_disabled_by_default_24) // ảnh mặc định khi đang tải
            .error(R.drawable.outline_disabled_by_default_24)       // ảnh mặc định nếu lỗi
            .into(holder.baihoc)
        holder.tdbaihoc.text = lesson.title
        holder.trangthai.text =  if (lesson.isCompleted) "Đã hoàn thành" else "Chưa hoàn thành"
        if (lesson.isLocked) {
            holder.lockIcon.visibility = View.VISIBLE
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.5f
            holder.itemView.setOnClickListener(null)
        } else {
            holder.lockIcon.visibility = View.GONE
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = if (lesson.questionType == "speaking") {
                    Intent(context, SpeakingQuestionActivity::class.java)
                } else {
                    Intent(context, QuestionActivity::class.java)
                }
                intent.putExtra("chapterId", lesson.chapterId)
                intent.putExtra("id", lesson.id)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = lesson.size

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val baihoc = view.findViewById<ImageView>(R.id.lessonIcon)
        val tdbaihoc = view.findViewById<TextView>(R.id.lessonTitle)
        val trangthai = view.findViewById<TextView>(R.id.lessonStatus)
        val lockIcon = view.findViewById<ImageView>(R.id.lockIcon)
    }
}