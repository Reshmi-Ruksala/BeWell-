package com.example.newbewell

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a mood entry
 * Uses SharedPreferences for persistence without databases
 */
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val moodName: String,
    val description: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val date: String = DateUtils.getCurrentDateString(),
    val time: String = DateUtils.getCurrentTimeString(),
    val color: String = "#60D394" // Default green color
)

/**
 * Data class for mood statistics
 */
data class MoodStats(
    val totalEntries: Int = 0,
    val averageMood: Float = 0f,
    val mostCommonMood: String = "😊",
    val moodTrend: String = "stable", // "improving", "declining", "stable"
    val weeklyAverage: Float = 0f
)

/**
 * Utility object for mood-related operations
 */
object MoodUtils {
    
    // Available mood options with their properties
    val MOOD_OPTIONS = listOf(
        MoodOption("😢", "Sad", "#FF6B6B", "Feeling down or blue"),
        MoodOption("😔", "Melancholy", "#FF8E53", "Thoughtful and reflective"),
        MoodOption("😐", "Neutral", "#95A5A6", "Neither happy nor sad"),
        MoodOption("🙂", "Content", "#74B9FF", "Peaceful and satisfied"),
        MoodOption("😊", "Happy", "#00B894", "Feeling good and positive"),
        MoodOption("😄", "Excited", "#FDCB6E", "Enthusiastic and energetic"),
        MoodOption("🤩", "Amazing", "#E17055", "Absolutely fantastic!"),
        MoodOption("😴", "Tired", "#636E72", "Exhausted and sleepy"),
        MoodOption("😰", "Anxious", "#E84393", "Worried or nervous"),
        MoodOption("😤", "Frustrated", "#D63031", "Annoyed or irritated"),
        MoodOption("😌", "Relaxed", "#00CEC9", "Calm and at ease"),
        MoodOption("🤔", "Thoughtful", "#6C5CE7", "Contemplative and curious")
    )
    
    fun getMoodOption(emoji: String): MoodOption? {
        return MOOD_OPTIONS.find { it.emoji == emoji }
    }
    
    fun getMoodScore(emoji: String): Int {
        return when (emoji) {
            "😢" -> 1
            "😔" -> 2
            "😐" -> 3
            "🙂" -> 4
            "😊" -> 5
            "😄" -> 6
            "🤩" -> 7
            "😴" -> 2
            "😰" -> 2
            "😤" -> 2
            "😌" -> 5
            "🤔" -> 3
            else -> 4
        }
    }
}

/**
 * Data class for mood option properties
 */
data class MoodOption(
    val emoji: String,
    val name: String,
    val color: String,
    val description: String
)
