package com.example.learnapp.View.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.R

class GoalsAdapter(
    private val goals: List<String>,
    private val status: List<Boolean>
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoal = view.findViewById<TextView>(R.id.tvObjective)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_objective, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val isDone = if (position < status.size) status[position] else false

        // Hiển thị Icon và Text
        val prefix = if (isDone) "✅ " else "• "
        holder.tvGoal.text = prefix + goals[position]

        // Hiệu ứng màu sắc
        if (isDone) {
            holder.tvGoal.setTextColor(Color.parseColor("#4CAF50")) // Màu xanh lá
            holder.tvGoal.alpha = 0.7f // Làm mờ nhẹ mục tiêu đã xong
        } else {
            holder.tvGoal.setTextColor(Color.BLACK)
            holder.tvGoal.alpha = 1.0f
        }
    }

    override fun getItemCount() = goals.size
}