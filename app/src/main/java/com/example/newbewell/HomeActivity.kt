package com.example.newbewell

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    
    // UI Elements
    private lateinit var greetingText: TextView
    private lateinit var greetingSubtitle: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var currentDateText: TextView
    private lateinit var currentMoodText: TextView
    private lateinit var currentMoodEmoji: TextView
    
    // Habit Progress Views
    private lateinit var habit1Name: TextView
    private lateinit var habit1Progress: TextView
    private lateinit var habit1ProgressBar: ProgressBar
    private lateinit var habit2Name: TextView
    private lateinit var habit2Progress: TextView
    private lateinit var habit2ProgressBar: ProgressBar
    private lateinit var habit3Name: TextView
    private lateinit var habit3Progress: TextView
    private lateinit var habit3ProgressBar: ProgressBar
    
    // Hydration Progress Views
    private lateinit var hydrationName: TextView
    private lateinit var hydrationProgress: TextView
    private lateinit var hydrationProgressBar: ProgressBar
    
    // Mood Trend Views
    private lateinit var moodTrendSummary: TextView
    private lateinit var moodAverageScore: TextView
    private lateinit var bestMoodDay: TextView
    private lateinit var moodConsistency: TextView
    private lateinit var moodStreak: TextView
    
    // Mood trend data
    private lateinit var moodPrefs: SharedPreferences
    private val moodPoints = mutableListOf<View>()
    private val moodTrendLine: View by lazy { findViewById(R.id.moodTrendLine) }
    
    // Clock Handler for real-time updates
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            loadMoodTrend()
            clockHandler.postDelayed(this, 60000) // Update every minute
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupClickListeners()
        updateGreeting()
        updateClock()
        loadCurrentMood()
        loadMoodTrend()
        loadHabitProgress()
        loadHydrationProgress()
        
        // Start clock updates
        clockHandler.post(clockRunnable)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh current mood, habit progress, and hydration when returning to homepage
        loadCurrentMood()
        loadMoodTrend()
        loadHabitProgress()
        loadHydrationProgress()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacks(clockRunnable)
    }
    
    private fun initializeViews() {
        // Greeting views
        greetingText = findViewById(R.id.greetingText)
        greetingSubtitle = findViewById(R.id.greetingSubtitle)
        
        // Clock views
        currentTimeText = findViewById(R.id.currentTimeText)
        currentDateText = findViewById(R.id.currentDateText)
        
        // Mood views
        currentMoodText = findViewById(R.id.currentMoodText)
        currentMoodEmoji = findViewById(R.id.currentMoodEmoji)
        
        // Mood trend views
        moodTrendSummary = findViewById(R.id.moodTrendSummary)
        moodAverageScore = findViewById(R.id.moodAverageScore)
        bestMoodDay = findViewById(R.id.bestMoodDay)
        moodConsistency = findViewById(R.id.moodConsistency)
        moodStreak = findViewById(R.id.moodStreak)
        
        // Initialize mood trend data
        moodPrefs = getSharedPreferences("mood_prefs", MODE_PRIVATE)
        
        // Initialize mood points for line chart
        moodPoints.addAll(listOf(
            findViewById(R.id.moodPoint1), findViewById(R.id.moodPoint2), findViewById(R.id.moodPoint3),
            findViewById(R.id.moodPoint4), findViewById(R.id.moodPoint5), findViewById(R.id.moodPoint6), findViewById(R.id.moodPoint7)
        ))
        
        // Habit progress views
        habit1Name = findViewById(R.id.habit1Name)
        habit1Progress = findViewById(R.id.habit1Progress)
        habit1ProgressBar = findViewById(R.id.habit1ProgressBar)
        habit2Name = findViewById(R.id.habit2Name)
        habit2Progress = findViewById(R.id.habit2Progress)
        habit2ProgressBar = findViewById(R.id.habit2ProgressBar)
        habit3Name = findViewById(R.id.habit3Name)
        habit3Progress = findViewById(R.id.habit3Progress)
        habit3ProgressBar = findViewById(R.id.habit3ProgressBar)
        
        // Hydration progress views
        hydrationName = findViewById(R.id.hydrationName)
        hydrationProgress = findViewById(R.id.hydrationProgress)
        hydrationProgressBar = findViewById(R.id.hydrationProgressBar)
    }
    
    private fun setupClickListeners() {
        // Navigation
        findViewById<LinearLayout>(R.id.nav_home_container).setOnClickListener {
            Toast.makeText(this, "You're already on the home page!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<LinearLayout>(R.id.nav_habits_container).setOnClickListener {
            val intent = Intent(this, HabbitpageActivity::class.java)
            startActivity(intent)
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
            in 5..11 -> "Ready to make today amazing?"
            in 12..17 -> "Keep up the great work!"
            in 18..21 -> "How did your day go?"
            else -> "Time to wind down and reflect"
        }
        
        greetingSubtitle.text = subtitle
    }
    
    private fun updateClock() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        currentTimeText.text = timeFormat.format(calendar.time)
        currentDateText.text = dateFormat.format(calendar.time)
    }
    
    private fun loadCurrentMood() {
        // Load the latest mood from SharedPreferences (saved by MoodPageActivity)
        val sharedPrefs = getSharedPreferences("NewBeWellPrefs", MODE_PRIVATE)
        val latestMoodEmoji = sharedPrefs.getString("latest_mood_emoji", "😊")
        val latestMoodName = sharedPrefs.getString("latest_mood_name", "Happy")
        
        // Update the mood display
        currentMoodEmoji.text = latestMoodEmoji
        currentMoodText.text = latestMoodName
        
        // Note: MoodManager fallback removed to simplify and use direct SharedPreferences
    }
    
    private fun getMoodName(emoji: String): String {
        return when (emoji) {
            "😢" -> "Sad"
            "😕" -> "Slightly Sad"
            "😐" -> "Neutral"
            "🙂" -> "Slightly Happy"
            "😊" -> "Happy"
            "😄" -> "Very Happy"
            "🤩" -> "Excited"
            "🥰" -> "Loved"
            else -> "Happy"
        }
    }
    
    private fun loadHabitProgress() {
        val sharedPrefs = getSharedPreferences("NewBeWellPrefs", MODE_PRIVATE)
        
        // Load habit data from SharedPreferences
        // Habit 1 - Water Intake
        val waterTarget = sharedPrefs.getInt("water_target", 8)
        val waterConsumed = sharedPrefs.getInt("water_consumed", 0)
        val waterProgress = if (waterTarget > 0) ((waterConsumed.toFloat() / waterTarget) * 100).toInt() else 0
        
        habit1Name.text = "Drink Water"
        habit1Progress.text = "$waterConsumed/$waterTarget"
        habit1ProgressBar.progress = waterProgress
        
        // Update progress text color based on completion
        if (waterProgress >= 100) {
            habit1Progress.setTextColor(resources.getColor(R.color.primary_green, null))
        } else {
            habit1Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
        }
        
        // Habit 2 - Exercise
        val exerciseTarget = sharedPrefs.getInt("exercise_target", 1)
        val exerciseCompleted = sharedPrefs.getInt("exercise_completed", 0)
        val exerciseProgress = if (exerciseTarget > 0) ((exerciseCompleted.toFloat() / exerciseTarget) * 100).toInt() else 0
        
        habit2Name.text = "Exercise"
        habit2Progress.text = "$exerciseCompleted/$exerciseTarget"
        habit2ProgressBar.progress = exerciseProgress
        
        if (exerciseProgress >= 100) {
            habit2Progress.setTextColor(resources.getColor(R.color.primary_green, null))
        } else {
            habit2Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
        }
        
        // Habit 3 - Reading
        val readingTarget = sharedPrefs.getInt("reading_target", 2)
        val readingCompleted = sharedPrefs.getInt("reading_completed", 0)
        val readingProgress = if (readingTarget > 0) ((readingCompleted.toFloat() / readingTarget) * 100).toInt() else 0
        
        habit3Name.text = "Read Books"
        habit3Progress.text = "$readingCompleted/$readingTarget"
        habit3ProgressBar.progress = readingProgress
        
        if (readingProgress >= 100) {
            habit3Progress.setTextColor(resources.getColor(R.color.primary_green, null))
        } else {
            habit3Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
        }
        
        // Try to load from HabitManager if available
        try {
            val habitManager = HabitManager(this)
            val today = DateUtils.getCurrentDateString()
            val allHabits = habitManager.getAllHabits()
            
            if (allHabits.isNotEmpty()) {
                // Update habits based on HabitManager data
                allHabits.take(3).forEachIndexed { index, habit ->
                    val completion = habitManager.getHabitCompletion(habit.id, today)
                    val completedCount = completion?.completedCount ?: 0
                    val targetCount = habit.targetCount
                    val progress = if (targetCount > 0) ((completedCount.toFloat() / targetCount) * 100).toInt() else 0
                    
                    when (index) {
                        0 -> {
                            habit1Name.text = habit.name
                            habit1Progress.text = "$completedCount/$targetCount"
                            habit1ProgressBar.progress = progress
                            if (progress >= 100) {
                                habit1Progress.setTextColor(resources.getColor(R.color.primary_green, null))
                            } else {
                                habit1Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
                            }
                        }
                        1 -> {
                            habit2Name.text = habit.name
                            habit2Progress.text = "$completedCount/$targetCount"
                            habit2ProgressBar.progress = progress
                            if (progress >= 100) {
                                habit2Progress.setTextColor(resources.getColor(R.color.primary_green, null))
                            } else {
                                habit2Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
                            }
                        }
                        2 -> {
                            habit3Name.text = habit.name
                            habit3Progress.text = "$completedCount/$targetCount"
                            habit3ProgressBar.progress = progress
                            if (progress >= 100) {
                                habit3Progress.setTextColor(resources.getColor(R.color.primary_green, null))
                            } else {
                                habit3Progress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // If HabitManager is not available, use SharedPreferences data
            // This ensures the app doesn't crash if there are issues
        }
    }
    
    private fun loadHydrationProgress() {
        val hydrationPrefs = getSharedPreferences("hydration_prefs", MODE_PRIVATE)
        val today = DateUtils.getCurrentDateString()
        
        // Load hydration data from SharedPreferences (using HydrationActivity keys)
        val waterTarget = hydrationPrefs.getInt("daily_goal", 8)
        val waterConsumed = hydrationPrefs.getInt("glasses_consumed_$today", 0)
        val hydrationProgressValue = if (waterTarget > 0) ((waterConsumed.toFloat() / waterTarget) * 100).toInt() else 0
        
        // Update hydration display
        hydrationName.text = "Water Intake"
        hydrationProgress.text = "$waterConsumed/$waterTarget glasses"
        hydrationProgressBar.progress = hydrationProgressValue
        
        // Update progress text color based on completion
        if (hydrationProgressValue >= 100) {
            hydrationProgress.setTextColor(resources.getColor(R.color.primary_green, null))
        } else {
            hydrationProgress.setTextColor(resources.getColor(R.color.text_secondary_subtle, null))
        }
    }
    
    private fun loadMoodTrend() {
        try {
            // Load mood entries from SharedPreferences
            val savedEntries = moodPrefs.getString("mood_entries", "") ?: ""
            val moodEntries = mutableListOf<SimpleMoodEntry>()
            
            if (savedEntries.isNotEmpty()) {
                savedEntries.split("|").forEach { entry ->
                    val parts = entry.split(",")
                    if (parts.size >= 3) {
                        moodEntries.add(SimpleMoodEntry(
                            emoji = parts[0],
                            dateTime = parts[1].toLongOrNull() ?: System.currentTimeMillis(),
                            id = parts[2]
                        ))
                    }
                }
            }
            
            // Get last 7 days starting from 6 days ago to today
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            
            val weeklyMoods = mutableListOf<SimpleMoodEntry>()
            val dayLabels = mutableListOf<String>()
            
            // Get moods for each of the last 7 days
            for (i in 6 downTo 0) {
                calendar.timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
                val dateString = dateFormat.format(calendar.time)
                val dayName = dayFormat.format(calendar.time)
                dayLabels.add(dayName)
                
                val dayStart = getDayStart(dateString)
                val dayEnd = getDayEnd(dateString)
                
                val dayMoods = moodEntries.filter { it.dateTime in dayStart..dayEnd }
                if (dayMoods.isNotEmpty()) {
                    // Get the latest mood for this day
                    val latestMood = dayMoods.maxByOrNull { it.dateTime }!!
                    weeklyMoods.add(latestMood)
                } else {
                    // Add neutral mood for missing days
                    weeklyMoods.add(SimpleMoodEntry(
                        emoji = "😐",
                        dateTime = calendar.timeInMillis,
                        id = ""
                    ))
                }
            }
            
            // Update mood chart with correct day mapping
            updateMoodChart(weeklyMoods, dayLabels)
            
            // Calculate and update insights
            updateMoodInsights(weeklyMoods)
            
        } catch (e: Exception) {
            // If there's an error, show default mood trend
            val calendar = Calendar.getInstance()
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val defaultMoods = mutableListOf<SimpleMoodEntry>()
            val dayLabels = mutableListOf<String>()
            
            for (i in 6 downTo 0) {
                calendar.timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
                val dayName = dayFormat.format(calendar.time)
                dayLabels.add(dayName)
                
                val defaultEmojis = listOf("😊", "😐", "😄", "🙂", "🤩", "😕", "🥰")
                defaultMoods.add(SimpleMoodEntry(
                    emoji = defaultEmojis[i],
                    dateTime = calendar.timeInMillis,
                    id = ""
                ))
            }
            updateMoodChart(defaultMoods, dayLabels)
            updateMoodInsights(defaultMoods)
        }
    }
    
    private fun getLast7Days(): List<String> {
        val days = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
            days.add(dateFormat.format(calendar.time))
        }
        return days
    }
    
    private fun getDayStart(dateString: String): Long {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        calendar.time = date!!
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getDayEnd(dateString: String): Long {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        calendar.time = date!!
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    private fun updateMoodChart(moods: List<SimpleMoodEntry>, dayLabels: List<String>) {
        val moodScores = mapOf(
            "😢" to 1, "😕" to 2, "😐" to 3, "🙂" to 4,
            "😊" to 5, "😄" to 6, "🤩" to 7, "🥰" to 8
        )
        
        // Mood level positions (0-4 for Excited to Sad)
        val moodLevels = mapOf(
            1 to 4, // Sad
            2 to 4, // Slightly Sad  
            3 to 3, // Neutral
            4 to 2, // Slightly Happy
            5 to 1, // Happy
            6 to 1, // Very Happy
            7 to 0, // Excited
            8 to 0  // Loved
        )
        
        // Update day labels
        val dayTextViews = listOf(
            findViewById<TextView>(R.id.dayLabel1),
            findViewById<TextView>(R.id.dayLabel2), 
            findViewById<TextView>(R.id.dayLabel3),
            findViewById<TextView>(R.id.dayLabel4),
            findViewById<TextView>(R.id.dayLabel5),
            findViewById<TextView>(R.id.dayLabel6),
            findViewById<TextView>(R.id.dayLabel7)
        )
        
        // Update day labels with actual day names
        dayLabels.forEachIndexed { index, dayName ->
            if (index < dayTextViews.size) {
                dayTextViews[index].text = dayName
            }
        }
        
        moods.forEachIndexed { index, mood ->
            if (index < moodPoints.size) {
                val score = moodScores[mood.emoji] ?: 3
                val level = moodLevels[score] ?: 3
                
                // Position the mood point based on level (0=top, 4=bottom)
                val layoutParams = moodPoints[index].layoutParams as LinearLayout.LayoutParams
                layoutParams.topMargin = (level * 20) + 20 // Adjust positioning
                moodPoints[index].layoutParams = layoutParams
                
                // Set color based on mood score
                val colorRes = when {
                    score >= 6 -> R.color.primary_green
                    score >= 4 -> R.color.accent_teal
                    score >= 3 -> R.color.secondary_blue
                    else -> R.color.status_missed_red
                }
                
                // Update point background color
                val drawable = resources.getDrawable(R.drawable.mood_point_background, null)
                drawable.setTint(resources.getColor(colorRes, null))
                moodPoints[index].background = drawable
            }
        }
        
        // Update trend line visibility
        moodTrendLine.visibility = View.VISIBLE
    }
    
    private fun updateMoodInsights(moods: List<SimpleMoodEntry>) {
        val moodScores = mapOf(
            "😢" to 1, "😕" to 2, "😐" to 3, "🙂" to 4,
            "😊" to 5, "😄" to 6, "🤩" to 7, "🥰" to 8
        )
        
        val scores = moods.map { moodScores[it.emoji] ?: 3 }
        val averageScore = scores.average()
        
        // Update average score
        moodAverageScore.text = String.format("%.1f/8", averageScore)
        
        // Find best day
        val bestDayIndex = scores.indexOf(scores.maxOrNull() ?: 3)
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        bestMoodDay.text = dayNames[bestDayIndex]
        
        // Calculate consistency
        val consistency = when {
            scores.all { it >= 6 } -> "Excellent"
            scores.count { it >= 5 } >= 5 -> "Good"
            scores.count { it >= 4 } >= 4 -> "Fair"
            else -> "Needs Work"
        }
        moodConsistency.text = consistency
        
        // Calculate streak (consecutive good days from today)
        var streak = 0
        for (i in scores.size - 1 downTo 0) {
            if (scores[i] >= 5) {
                streak++
            } else {
                break
            }
        }
        moodStreak.text = "$streak days"
        
        // Update trend summary
        val trend = when {
            averageScore >= 6 -> "Trending up! 📈"
            averageScore >= 4 -> "Stable mood 📊"
            else -> "Let's improve! 💪"
        }
        moodTrendSummary.text = trend
    }
}