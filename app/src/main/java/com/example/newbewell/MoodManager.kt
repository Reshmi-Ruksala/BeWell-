package com.example.newbewell

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages mood entry data using SharedPreferences for persistence
 * Demonstrates data persistence without databases
 */
class MoodManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("mood_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val MOOD_ENTRIES_KEY = "mood_entries"
    }
    
    // Mood entry CRUD operations
    fun addMoodEntry(moodEntry: MoodEntry): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        entries.add(moodEntry)
        return saveMoodEntries(entries)
    }
    
    fun updateMoodEntry(moodEntry: MoodEntry): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntry.id }
        return if (index != -1) {
            entries[index] = moodEntry
            saveMoodEntries(entries)
        } else {
            false
        }
    }
    
    fun deleteMoodEntry(entryId: String): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val removed = entries.removeAll { it.id == entryId }
        return if (removed) {
            saveMoodEntries(entries)
        } else {
            false
        }
    }
    
    fun getMoodEntry(entryId: String): MoodEntry? {
        return getAllMoodEntries().find { it.id == entryId }
    }
    
    fun getAllMoodEntries(): List<MoodEntry> {
        val entriesJson = prefs.getString(MOOD_ENTRIES_KEY, null)
        return if (entriesJson != null) {
            try {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson(entriesJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        return getAllMoodEntries().filter { it.date == date }
    }
    
    fun getMoodEntriesForWeek(): List<MoodEntry> {
        val weekStart = DateUtils.getWeekStartDate()
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7)
        val weekEnd = DateUtils.getDateString(0) // Today
        
        return getAllMoodEntries().filter { entry ->
            entry.date >= weekStart && entry.date <= weekEnd
        }
    }
    
    fun getRecentMoodEntries(limit: Int = 10): List<MoodEntry> {
        return getAllMoodEntries()
            .sortedByDescending { it.dateTime }
            .take(limit)
    }
    
    private fun saveMoodEntries(entries: List<MoodEntry>): Boolean {
        return try {
            val entriesJson = gson.toJson(entries)
            prefs.edit().putString(MOOD_ENTRIES_KEY, entriesJson).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Statistics
    fun getMoodStats(): MoodStats {
        val entries = getAllMoodEntries()
        val totalEntries = entries.size
        
        if (totalEntries == 0) {
            return MoodStats()
        }
        
        // Calculate average mood score
        val totalScore = entries.sumOf { MoodUtils.getMoodScore(it.emoji) }
        val averageMood = totalScore.toFloat() / totalEntries
        
        // Find most common mood
        val moodCounts = entries.groupingBy { it.emoji }.eachCount()
        val mostCommonMood = moodCounts.maxByOrNull { it.value }?.key ?: "😊"
        
        // Calculate weekly average
        val weeklyEntries = getMoodEntriesForWeek()
        val weeklyAverage = if (weeklyEntries.isNotEmpty()) {
            weeklyEntries.sumOf { MoodUtils.getMoodScore(it.emoji) }.toFloat() / weeklyEntries.size
        } else {
            averageMood
        }
        
        // Determine mood trend (simplified)
        val recentEntries = getRecentMoodEntries(7)
        val olderEntries = entries.filter { entry ->
            !recentEntries.any { it.id == entry.id }
        }.take(7)
        
        val moodTrend = when {
            recentEntries.isEmpty() || olderEntries.isEmpty() -> "stable"
            recentEntries.sumOf { MoodUtils.getMoodScore(it.emoji) } > 
            olderEntries.sumOf { MoodUtils.getMoodScore(it.emoji) } -> "improving"
            recentEntries.sumOf { MoodUtils.getMoodScore(it.emoji) } < 
            olderEntries.sumOf { MoodUtils.getMoodScore(it.emoji) } -> "declining"
            else -> "stable"
        }
        
        return MoodStats(
            totalEntries = totalEntries,
            averageMood = averageMood,
            mostCommonMood = mostCommonMood,
            moodTrend = moodTrend,
            weeklyAverage = weeklyAverage
        )
    }
    
    // Initialize with sample data
    fun initializeSampleMoods() {
        if (getAllMoodEntries().isEmpty()) {
            val sampleMoods = listOf(
                MoodEntry(
                    emoji = "😊",
                    moodName = "Happy",
                    description = "Had a great day at work!",
                    dateTime = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L), // 2 days ago
                    date = DateUtils.getDateString(2),
                    time = "14:30"
                ),
                MoodEntry(
                    emoji = "🙂",
                    moodName = "Content",
                    description = "Peaceful evening at home",
                    dateTime = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L), // 1 day ago
                    date = DateUtils.getDateString(1),
                    time = "20:15"
                ),
                MoodEntry(
                    emoji = "😄",
                    moodName = "Excited",
                    description = "Looking forward to the weekend!",
                    dateTime = System.currentTimeMillis() - (30 * 60 * 1000L), // 30 minutes ago
                    date = DateUtils.getCurrentDateString(),
                    time = DateUtils.getCurrentTimeString()
                )
            )
            
            sampleMoods.forEach { addMoodEntry(it) }
        }
    }
}
