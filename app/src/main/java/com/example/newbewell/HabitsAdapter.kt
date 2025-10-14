package com.example.newbewell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitsAdapter(
    private var habits: List<Habit>,
    private val onIncrementClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onCompletionToggle: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private lateinit var habitManager: HabitManager

    fun setHabitManager(manager: HabitManager) {
        habitManager = manager
    }

    fun updateHabits(newHabits: List<Habit>) {
        val oldSize = habits.size
        habits = newHabits
        val newSize = habits.size
        
        if (newSize > oldSize) {
            // New habits were added
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (newSize < oldSize) {
            // Habits were removed
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        } else {
            // Data was updated
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.habit_item_layout, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val habitIcon: TextView = itemView.findViewById(R.id.habitIcon)
        private val habitName: TextView = itemView.findViewById(R.id.habitName)
        private val habitDescription: TextView = itemView.findViewById(R.id.habitDescription)
        private val completionCheckbox: CheckBox = itemView.findViewById(R.id.completionCheckbox)
        private val completionText: TextView = itemView.findViewById(R.id.completionText)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val streakText: TextView = itemView.findViewById(R.id.streakText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val incrementButton: Button = itemView.findViewById(R.id.incrementButton)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(habit: Habit) {
            habitIcon.text = habit.icon
            habitName.text = habit.name
            habitDescription.text = habit.description

            // Get today's completion status
            val today = DateUtils.getCurrentDateString()
            val completion = habitManager.getHabitCompletion(habit.id, today)
            val completedCount = completion?.completedCount ?: 0
            val isCompleted = completion?.isCompleted == true

            // Update progress text
            progressText.text = "$completedCount / ${habit.targetCount} ${habit.unit}"

            // Update progress bar
            val progressPercentage = if (habit.targetCount > 0) {
                (completedCount.toFloat() / habit.targetCount * 100).toInt()
            } else {
                0
            }
            progressBar.progress = progressPercentage

            // Update completion checkbox
            completionCheckbox.isChecked = isCompleted
            if (isCompleted) {
                completionText.text = "Done"
                completionText.setTextColor(itemView.context.getColor(R.color.status_complete_green))
            } else {
                completionText.text = "Done"
                completionText.setTextColor(itemView.context.getColor(R.color.text_secondary_subtle))
            }

            // Update streak
            val stats = habitManager.getHabitStats(habit.id)
            streakText.text = "🔥 ${stats.currentStreak} day streak"

            // Set click listeners
            completionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCompletionToggle(habit, isChecked)
            }

            incrementButton.setOnClickListener {
                onIncrementClick(habit)
            }

            editButton.setOnClickListener {
                onEditClick(habit)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(habit)
            }

            // Disable increment button if already completed
            incrementButton.isEnabled = !isCompleted
            if (isCompleted) {
                incrementButton.alpha = 0.5f
            } else {
                incrementButton.alpha = 1.0f
            }
        }
    }
}
