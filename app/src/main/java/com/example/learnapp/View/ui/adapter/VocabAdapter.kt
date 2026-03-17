package com.example.learnapp.View.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.R

class VocabAdapter(
    private var vocabList: List<Vocabulary>,
    private val onItemClick: (Vocabulary) -> Unit,
    private val onFavoriteClick: (Vocabulary) -> Unit,
    private val onVolumeClick: (String) -> Unit
) : RecyclerView.Adapter<VocabAdapter.VocabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        // Sử dụng layout item_vocabulary bạn đã gửi
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vocabulary, parent, false)
        return VocabViewHolder(view)
    }

    override fun onBindViewHolder(holder: VocabViewHolder, position: Int) {
        val vocab = vocabList[position]

        holder.tvWord.text = vocab.vocab
        holder.tvTranslation.text = vocab.translation

        // 2. Thiết lập trạng thái ban đầu từ data
        if (vocab.isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.heart_red)
        } else {
            holder.btnFavorite.setImageResource(R.drawable.heart) // Dùng icon tim rỗng ở đây
        }
        holder.btnFavorite.imageTintList = null

        holder.btnFavorite.setOnClickListener {
            // 1. Đổi dữ liệu cục bộ ngay
            vocab.isFavorite = !vocab.isFavorite

            // 2. Ép icon đổi màu ngay lập tức
            if (vocab.isFavorite) {
                holder.btnFavorite.setImageResource(R.drawable.heart_red)
            } else {
                holder.btnFavorite.setImageResource(R.drawable.heart)
            }

            // 3. Chạy animation nảy
            it.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction {
                it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }

            // 4. Báo về Fragment để thêm vào danh sách "Chờ lưu"
            onFavoriteClick(vocab)
        }
        // Sự kiện click vào toàn bộ item
        holder.itemView.setOnClickListener { onItemClick(vocab) }

        // Sự kiện click vào nút loa
        holder.btnVolume.setOnClickListener { onVolumeClick(vocab.vocab) }
    }

    override fun getItemCount(): Int = vocabList.size

    // Hàm cập nhật dữ liệu giống ChapterAdapter của bạn
    fun updateData(newVocabs: List<Vocabulary>) {
        vocabList = newVocabs
        notifyDataSetChanged()
    }

    class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWord: TextView = view.findViewById(R.id.tvWord)
        val tvTranslation: TextView = view.findViewById(R.id.tvTranslation)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
        val btnVolume: ImageButton = view.findViewById(R.id.btnVolume)
    }
}