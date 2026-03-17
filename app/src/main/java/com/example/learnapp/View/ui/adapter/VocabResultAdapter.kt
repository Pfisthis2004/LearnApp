package com.example.learnapp.View.ui.adapter

import com.example.learnapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Vocabulary

class VocabResultAdapter(private val list: List<Vocabulary>) :
    RecyclerView.Adapter<VocabResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vocab: TextView = view.findViewById(R.id.tvWord)
        val trans: TextView = view.findViewById(R.id.tvTranslation)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vocabulary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.vocab.text = item.vocab
        holder.trans.text = item.translation

        // --- BẮT ĐẦU THÊM ANIMATION TẠI ĐÂY ---
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in_item)

        // Tạo độ trễ (delay) dựa trên vị trí của item để các item hiện ra lần lượt
        animation.startOffset = (position * 100).toLong()

        holder.itemView.startAnimation(animation)
        // --------------------------------------
    }

    override fun getItemCount() = list.size
}