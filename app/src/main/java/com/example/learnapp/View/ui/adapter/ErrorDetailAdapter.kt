package com.example.learnapp.View.ui.adapter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ErrorDetailAdapter(private val errors: List<String>) :
    RecyclerView.Adapter<ErrorDetailAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvError: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvError.apply {
            text = "• ${errors[position]}"
            setTextColor(Color.WHITE)
            textSize = 14f
        }
    }

    override fun getItemCount() = errors.size
}