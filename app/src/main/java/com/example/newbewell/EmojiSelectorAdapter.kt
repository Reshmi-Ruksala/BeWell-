package com.example.newbewell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmojiSelectorAdapter(
    private val moodOptions: List<MoodOption>,
    private val onMoodSelected: (MoodOption) -> Unit
) : RecyclerView.Adapter<EmojiSelectorAdapter.EmojiViewHolder>() {

    private var selectedPosition = -1

    fun clearSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.emoji_selector_item, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(moodOptions[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = moodOptions.size

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiText: TextView = itemView.findViewById(R.id.emojiText)
        private val emojiName: TextView = itemView.findViewById(R.id.emojiName)
        private val container: LinearLayout = itemView.findViewById(R.id.container)

        fun bind(moodOption: MoodOption, isSelected: Boolean) {
            emojiText.text = moodOption.emoji
            emojiName.text = moodOption.name
            container.isSelected = isSelected

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                
                if (oldPosition != -1) {
                    notifyItemChanged(oldPosition)
                }
                notifyItemChanged(selectedPosition)
                
                onMoodSelected(moodOption)
            }
        }
    }
}
