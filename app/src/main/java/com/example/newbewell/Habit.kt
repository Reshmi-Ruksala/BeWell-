package com.example.newbewell

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a daily habit
 * Uses SharedPreferences for persistence without databases
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val icon: String = "💧", // Default water drop emoji
    val targetCount: Int = 1, // How many times per day
    val unit: String = "times", // e.g., "glasses", "minutes", "steps"
    val color: String = "#60D394", // Default green color
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Data class representing habit completion for a specific date
 */
data class HabitCompletion(
    val habitId: String,
    val date: String, // Format: "yyyy-MM-dd"
    val completedCount: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for habit statistics
 */
data class HabitStats(
    val habitId: String,
    val totalDays: Int = 0,
    val completedDays: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f
)

/**
 * Utility object for date formatting
 */
object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    
    fun getCurrentDateString(): String = dateFormat.format(Date())
    
    fun getCurrentTimeString(): String = timeFormat.format(Date())
    
    fun getCurrentDateTimeString(): String = dateTimeFormat.format(Date())
    
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun formatDateTimeForDisplay(dateTime: Long): String {
        return dateTimeFormat.format(Date(dateTime))
    }
    
    fun getDateString(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }
    
    fun getWeekStartDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return dateFormat.format(calendar.time)
    }
}
