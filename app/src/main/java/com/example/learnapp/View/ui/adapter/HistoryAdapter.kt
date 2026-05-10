package com.example.learnapp.View.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.R
import com.google.firebase.Timestamp
import java.util.Locale

class HistoryAdapter(private val onItemClick: (HistoryItem) -> Unit,
                     private val onDeleteLongClick: (HistoryItem) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var items = listOf<HistoryItem>()

    fun submitList(newList: List<HistoryItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Giữ nguyên layout item_conversation của bạn
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversations, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        // Sử dụng đúng ID trong XML bạn gửi
        holder.tvTitle.text = item.lessonTitle
        // Bạn có thể đổi icon tùy theo điểm số nếu muốn
        holder.imgIcon.setImageResource(R.drawable.sparkles)

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onDeleteLongClick(item)
            true
        }
    }

    override fun getItemCount() = items.size

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.converTitle)
        val imgIcon: ImageView = view.findViewById(R.id.converIcon)
    }
}