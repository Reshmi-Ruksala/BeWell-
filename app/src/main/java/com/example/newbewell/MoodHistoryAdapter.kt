package com.example.newbewell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodHistoryAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder>() {

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        val oldSize = moodEntries.size
        moodEntries = newEntries
        val newSize = moodEntries.size
        
        if (newSize > oldSize) {
            // New entries were added
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (newSize < oldSize) {
            // Entries were removed
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        } else {
            // Data was updated
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mood_history_item, parent, false)
        return MoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodHistoryViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodEmoji: TextView = itemView.findViewById(R.id.moodEmoji)
        private val moodName: TextView = itemView.findViewById(R.id.moodName)
        private val moodTime: TextView = itemView.findViewById(R.id.moodTime)
        private val moodDescription: TextView = itemView.findViewById(R.id.moodDescription)
        private val moodDate: TextView = itemView.findViewById(R.id.moodDate)
        private val deleteMoodButton: Button = itemView.findViewById(R.id.deleteMoodButton)

        fun bind(moodEntry: MoodEntry) {
            moodEmoji.text = moodEntry.emoji
            moodName.text = moodEntry.moodName
            moodTime.text = moodEntry.time
            moodDate.text = DateUtils.formatDateForDisplay(moodEntry.date)
            
            // Show description if available
            if (moodEntry.description.isNotEmpty()) {
                moodDescription.text = moodEntry.description
                moodDescription.visibility = View.VISIBLE
            } else {
                moodDescription.text = "No description"
                moodDescription.visibility = View.VISIBLE
                moodDescription.setTextColor(itemView.context.getColor(R.color.text_secondary_subtle))
            }

            deleteMoodButton.setOnClickListener {
                onDeleteClick(moodEntry)
            }
        }
    }
}
