package com.example.newbewell

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import java.util.*

class HabbitpageActivity : AppCompatActivity() {
    
    private lateinit var habitManager: HabitManager
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var greetingText: android.widget.TextView
    private lateinit var greetingSubtitle: android.widget.TextView
    private lateinit var todayProgressText: android.widget.TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addHabitButton: CardView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.habbitpage)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupHabitManager()
        setupRecyclerView()
        setupClickListeners()
        updateGreeting()
        loadHabits()
    }
    
    private fun initializeViews() {
        habitsRecyclerView = findViewById(R.id.habitsRecyclerView)
        greetingText = findViewById(R.id.greetingText)
        greetingSubtitle = findViewById(R.id.greetingSubtitle)
        todayProgressText = findViewById(R.id.todayProgressText)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        addHabitButton = findViewById(R.id.addHabitButton)
    }
    
    private fun setupHabitManager() {
        habitManager = HabitManager(this)
        habitManager.initializeSampleHabits()
    }
    
    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(
            habits = emptyList(),
            onIncrementClick = { habit -> incrementHabit(habit) },
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteHabitDialog(habit) },
            onCompletionToggle = { habit, isCompleted -> toggleHabitCompletion(habit, isCompleted) }
        )
        
        habitsAdapter.setHabitManager(habitManager)
        
        habitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HabbitpageActivity)
            adapter = habitsAdapter
        }
    }
    
    private fun setupClickListeners() {
        addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
        
        
        // Bottom navigation click listeners
        findViewById<LinearLayout>(R.id.nav_home_container).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_habits_container).setOnClickListener {
            // Already on habits page
            Toast.makeText(this, "You're already on the habits page!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<LinearLayout>(R.id.nav_hydration_container).setOnClickListener {
            val intent = Intent(this, HydrationActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_mood_container).setOnClickListener {
            val intent = Intent(this, MoodPageActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<LinearLayout>(R.id.nav_settings_container).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good Morning! ☀️"
            in 12..17 -> "Good Afternoon! 🌤️"
            in 18..21 -> "Good Evening! 🌅"
            else -> "Good Night! 🌙"
        }
        
        greetingText.text = greeting
        
        val subtitle = when (hour) {
            in 5..11 -> "Ready to build healthy habits today?"
            in 12..17 -> "Keep up the great work!"
            in 18..21 -> "How did your habits go today?"
            else -> "Time to wind down and reflect"
        }
        
        greetingSubtitle.text = subtitle
    }
    
    private fun loadHabits() {
        val habits = habitManager.getAllHabits()
        val today = DateUtils.getCurrentDateString()
        
        // Update today's progress
        val completedToday = habits.count { habit ->
            val completion = habitManager.getHabitCompletion(habit.id, today)
            completion?.isCompleted == true
        }
        
        todayProgressText.text = "$completedToday/${habits.size} habits completed"
        
        // Update adapter with new data
        habitsAdapter.updateHabits(habits)
        
        // Show/hide empty state
        if (habits.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            habitsRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            habitsRecyclerView.visibility = View.VISIBLE
        }
        
        // Force RecyclerView to refresh and scroll to show new items
        habitsRecyclerView.post {
            habitsAdapter.notifyDataSetChanged()
            if (habits.isNotEmpty()) {
                // Scroll to the last added item
                habitsRecyclerView.smoothScrollToPosition(habits.size - 1)
            }
        }
    }
    
    private fun incrementHabit(habit: Habit) {
        val today = DateUtils.getCurrentDateString()
        val currentCompletion = habitManager.getHabitCompletion(habit.id, today)
        val newCount = (currentCompletion?.completedCount ?: 0) + 1
        
        habitManager.markHabitCompleted(habit.id, today, newCount)
        loadHabits()
        
        if (newCount >= habit.targetCount) {
            Toast.makeText(this, "🎉 ${habit.name} completed for today!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val today = DateUtils.getCurrentDateString()
        
        if (isCompleted) {
            // Mark as completed with target count
            habitManager.markHabitCompleted(habit.id, today, habit.targetCount)
            Toast.makeText(this, "✅ ${habit.name} marked as completed!", Toast.LENGTH_SHORT).show()
        } else {
            // Mark as not completed (reset to 0)
            habitManager.markHabitCompleted(habit.id, today, 0)
            Toast.makeText(this, "📝 ${habit.name} marked as pending", Toast.LENGTH_SHORT).show()
        }
        
        loadHabits()
    }
    
    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_habit, null)
        val nameEditText = dialogView.findViewById<android.widget.EditText>(R.id.habitNameEditText)
        val descriptionEditText = dialogView.findViewById<android.widget.EditText>(R.id.habitDescriptionEditText)
        val targetCountEditText = dialogView.findViewById<android.widget.EditText>(R.id.targetCountEditText)
        val unitEditText = dialogView.findViewById<android.widget.EditText>(R.id.unitEditText)
        val iconEditText = dialogView.findViewById<android.widget.EditText>(R.id.iconEditText)
        
        AlertDialog.Builder(this)
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val unit = unitEditText.text.toString().trim().ifEmpty { "times" }
                val icon = iconEditText.text.toString().trim().ifEmpty { "🎯" }
                
                if (name.isNotEmpty()) {
                    val habit = Habit(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        unit = unit,
                        icon = icon
                    )
                    
                    if (habitManager.addHabit(habit)) {
                        Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show()
                        loadHabits()
                    } else {
                        Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_habit, null)
        val nameEditText = dialogView.findViewById<android.widget.EditText>(R.id.habitNameEditText)
        val descriptionEditText = dialogView.findViewById<android.widget.EditText>(R.id.habitDescriptionEditText)
        val targetCountEditText = dialogView.findViewById<android.widget.EditText>(R.id.targetCountEditText)
        val unitEditText = dialogView.findViewById<android.widget.EditText>(R.id.unitEditText)
        val iconEditText = dialogView.findViewById<android.widget.EditText>(R.id.iconEditText)
        
        // Pre-fill with current values
        nameEditText.setText(habit.name)
        descriptionEditText.setText(habit.description)
        targetCountEditText.setText(habit.targetCount.toString())
        unitEditText.setText(habit.unit)
        iconEditText.setText(habit.icon)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val unit = unitEditText.text.toString().trim().ifEmpty { "times" }
                val icon = iconEditText.text.toString().trim().ifEmpty { "🎯" }
                
                if (name.isNotEmpty()) {
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        unit = unit,
                        icon = icon
                    )
                    
                    if (habitManager.updateHabit(updatedHabit)) {
                        Toast.makeText(this, "Habit updated successfully!", Toast.LENGTH_SHORT).show()
                        loadHabits()
                    } else {
                        Toast.makeText(this, "Failed to update habit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteHabitDialog(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                if (habitManager.deleteHabit(habit.id)) {
                    Toast.makeText(this, "Habit deleted successfully!", Toast.LENGTH_SHORT).show()
                    loadHabits()
                } else {
                    Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}