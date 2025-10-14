package com.example.newbewell

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class HabitManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val HABITS_KEY = "habits"
        private const val COMPLETIONS_KEY = "completions"
    }
    
    // Habit CRUD operations
    fun addHabit(habit: Habit): Boolean {
        val habits = getAllHabits().toMutableList()
        habits.add(habit)
        return saveHabits(habits)
    }
    
    fun updateHabit(habit: Habit): Boolean {
        val habits = getAllHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        return if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        } else {
            false
        }
    }
    
    fun deleteHabit(habitId: String): Boolean {
        val habits = getAllHabits().toMutableList()
        val removed = habits.removeAll { it.id == habitId }
        if (removed) {
            // Also remove all completions for this habit
            removeHabitCompletions(habitId)
            return saveHabits(habits)
        }
        return false
    }
    
    fun getHabit(habitId: String): Habit? {
        return getAllHabits().find { it.id == habitId }
    }
    
    fun getAllHabits(): List<Habit> {
        val habitsJson = prefs.getString(HABITS_KEY, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(habitsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveHabits(habits: List<Habit>): Boolean {
        return try {
            val habitsJson = gson.toJson(habits)
            prefs.edit().putString(HABITS_KEY, habitsJson).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Habit completion operations
    fun markHabitCompleted(habitId: String, date: String = DateUtils.getCurrentDateString(), count: Int = 1): Boolean {
        val completions = getAllCompletions().toMutableList()
        val existingIndex = completions.indexOfFirst { it.habitId == habitId && it.date == date }
        
        val habit = getHabit(habitId) ?: return false
        val isCompleted = count >= habit.targetCount
        
        val completion = HabitCompletion(
            habitId = habitId,
            date = date,
            completedCount = count,
            isCompleted = isCompleted
        )
        
        if (existingIndex != -1) {
            completions[existingIndex] = completion
        } else {
            completions.add(completion)
        }
        
        return saveCompletions(completions)
    }
    
    fun getHabitCompletion(habitId: String, date: String = DateUtils.getCurrentDateString()): HabitCompletion? {
        return getAllCompletions().find { it.habitId == habitId && it.date == date }
    }
    
    fun getAllCompletions(): List<HabitCompletion> {
        val completionsJson = prefs.getString(COMPLETIONS_KEY, null)
        return if (completionsJson != null) {
            try {
                val type = object : TypeToken<List<HabitCompletion>>() {}.type
                gson.fromJson(completionsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun getHabitCompletions(habitId: String): List<HabitCompletion> {
        return getAllCompletions().filter { it.habitId == habitId }
    }
    
    private fun saveCompletions(completions: List<HabitCompletion>): Boolean {
        return try {
            val completionsJson = gson.toJson(completions)
            prefs.edit().putString(COMPLETIONS_KEY, completionsJson).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun removeHabitCompletions(habitId: String) {
        val completions = getAllCompletions().filter { it.habitId != habitId }
        saveCompletions(completions)
    }
    
    // Statistics
    fun getHabitStats(habitId: String): HabitStats {
        val completions = getHabitCompletions(habitId)
        val totalDays = completions.size
        val completedDays = completions.count { it.isCompleted }
        val completionRate = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f
        
        // Calculate streaks
        val sortedCompletions = completions.sortedByDescending { it.date }
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        
        for (completion in sortedCompletions) {
            if (completion.isCompleted) {
                tempStreak++
                if (currentStreak == 0) currentStreak = tempStreak
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 0
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)
        
        return HabitStats(
            habitId = habitId,
            totalDays = totalDays,
            completedDays = completedDays,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = completionRate
        )
    }
    
    // Initialize with sample data
    fun initializeSampleHabits() {
        if (getAllHabits().isEmpty()) {
            val sampleHabits = listOf(
                Habit(
                    name = "Drink Water",
                    description = "Stay hydrated throughout the day",
                    icon = "💧",
                    targetCount = 8,
                    unit = "glasses",
                    color = "#35A7FF"
                ),
                Habit(
                    name = "Meditate",
                    description = "Practice mindfulness and relaxation",
                    icon = "🧘",
                    targetCount = 1,
                    unit = "session",
                    color = "#60D394"
                ),
                Habit(
                    name = "Take Steps",
                    description = "Stay active and healthy",
                    icon = "🚶",
                    targetCount = 10000,
                    unit = "steps",
                    color = "#00BCD4"
                )
            )
            
            sampleHabits.forEach { addHabit(it) }
        }
    }
}
