package com.example.newbewell

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class HydrationActivity : AppCompatActivity() {
    
    private lateinit var prefs: SharedPreferences
    private lateinit var todayProgressText: TextView
    private lateinit var currentGoalText: TextView
    private lateinit var goalInputEditText: EditText
    private lateinit var setGoalButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var waterGlassesContainer: LinearLayout
    private lateinit var addGlassButton: Button
    private lateinit var glassesConsumedText: TextView
    private lateinit var goalRemainingText: TextView
    private lateinit var completionStatusText: TextView
    
    // Simple reminder controls
    private lateinit var reminderIntervalInput: EditText
    private lateinit var setReminderButton: Button
    private lateinit var testNotificationButton: Button
    private lateinit var reminderStatus: TextView
    
    private var dailyGoal = 8
    private var glassesConsumed = 0
    private val today = DateUtils.getCurrentDateString()
    
    // Permission request launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied. Notifications won't work.", Toast.LENGTH_LONG).show()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "hydration_prefs"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_GLASSES_CONSUMED = "glasses_consumed_"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.hydration)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupSharedPreferences()
        loadData()
        setupClickListeners()
        setupReminderControls()
        updateUI()
        setupNavigation()
    }
    
    private fun initializeViews() {
        todayProgressText = findViewById(R.id.todayProgressText)
        currentGoalText = findViewById(R.id.currentGoalText)
        goalInputEditText = findViewById(R.id.goalInputEditText)
        setGoalButton = findViewById(R.id.setGoalButton)
        progressBar = findViewById(R.id.progressBar)
        progressPercentageText = findViewById(R.id.progressPercentageText)
        waterGlassesContainer = findViewById(R.id.waterGlassesContainer)
        addGlassButton = findViewById(R.id.addGlassButton)
        glassesConsumedText = findViewById(R.id.glassesConsumedText)
        goalRemainingText = findViewById(R.id.goalRemainingText)
        completionStatusText = findViewById(R.id.completionStatusText)
        
        // Simple reminder controls
        reminderIntervalInput = findViewById(R.id.reminderIntervalInput)
        setReminderButton = findViewById(R.id.setReminderButton)
        testNotificationButton = findViewById(R.id.testNotificationButton)
        reminderStatus = findViewById(R.id.reminderStatus)
    }
    
    private fun setupSharedPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun loadData() {
        // Load daily goal
        dailyGoal = prefs.getInt(KEY_DAILY_GOAL, 8)
        
        // Load glasses consumed for today
        glassesConsumed = prefs.getInt(KEY_GLASSES_CONSUMED + today, 0)
    }
    
    private fun setupClickListeners() {
        setGoalButton.setOnClickListener {
            val newGoal = goalInputEditText.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                dailyGoal = newGoal
                prefs.edit().putInt(KEY_DAILY_GOAL, dailyGoal).apply()
                currentGoalText.text = "$dailyGoal glasses"
                goalInputEditText.text.clear()
                updateUI()
                Toast.makeText(this, "Daily goal set to $dailyGoal glasses!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
        
        addGlassButton.setOnClickListener {
            addGlass()
        }
    }
    
    private fun addGlass() {
        if (glassesConsumed < dailyGoal) {
            glassesConsumed++
            prefs.edit().putInt(KEY_GLASSES_CONSUMED + today, glassesConsumed).apply()
            updateUI()
            
            val message = when {
                glassesConsumed == dailyGoal -> "🎉 Congratulations! You've reached your daily goal!"
                glassesConsumed >= dailyGoal * 0.8 -> "💪 Almost there! Keep it up!"
                glassesConsumed >= dailyGoal * 0.5 -> "👍 Great progress!"
                else -> "💧 Good start! Keep drinking water!"
            }
            
            Toast.makeText(this, "Added 1 glass of water!\n$message", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "You've already reached your daily goal! 🎉", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateUI() {
        // Update progress text
        todayProgressText.text = "$glassesConsumed / $dailyGoal glasses"
        currentGoalText.text = "$dailyGoal glasses"
        
        // Update progress bar and percentage
        val progressPercentage = if (dailyGoal > 0) {
            (glassesConsumed.toFloat() / dailyGoal * 100).toInt()
        } else {
            0
        }
        progressBar.progress = progressPercentage
        progressPercentageText.text = "$progressPercentage%"
        
        // Update summary stats
        glassesConsumedText.text = glassesConsumed.toString()
        goalRemainingText.text = (dailyGoal - glassesConsumed).toString()
        completionStatusText.text = "$progressPercentage%"
        
        // Update water glasses display
        updateWaterGlassesDisplay()
        
        // Update add glass button
        if (glassesConsumed >= dailyGoal) {
            addGlassButton.text = "🎉 Goal Achieved!"
            addGlassButton.isEnabled = false
            addGlassButton.alpha = 0.7f
        } else {
            addGlassButton.text = "➕ Add Glass of Water"
            addGlassButton.isEnabled = true
            addGlassButton.alpha = 1.0f
        }
    }
    
    private fun updateWaterGlassesDisplay() {
        waterGlassesContainer.removeAllViews()
        
        // Create a grid layout for water glasses
        val maxColumns = 4
        var currentRow: LinearLayout? = null
        
        for (i in 1..dailyGoal) {
            if (i % maxColumns == 1 || currentRow == null) {
                // Create new row
                currentRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                waterGlassesContainer.addView(currentRow)
            }
            
            // Create glass button
            val glassButton = Button(this).apply {
                text = if (i <= glassesConsumed) "💧" else "🥛"
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(4, 4, 4, 4)
                }
                
                if (i <= glassesConsumed) {
                    // Glass is consumed
                    background = getDrawable(R.drawable.button_primary)
                    setTextColor(getColor(R.color.white))
                } else {
                    // Glass is not consumed
                    background = getDrawable(R.drawable.habit_icon_background)
                    setTextColor(getColor(R.color.text_secondary_subtle))
                }
                
                // Make button clickable only if it's the next glass to consume
                isEnabled = (i == glassesConsumed + 1)
                
                setOnClickListener {
                    if (i == glassesConsumed + 1) {
                        addGlass()
                    }
                }
            }
            
            currentRow.addView(glassButton)
        }
    }
    
    private fun setupNavigation() {
        // Bottom navigation click listeners
        findViewById<LinearLayout>(R.id.nav_home_container).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_habits_container).setOnClickListener {
            val intent = Intent(this, HabbitpageActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_hydration_container).setOnClickListener {
            // Already on hydration page
            Toast.makeText(this, "You're already on the hydration page!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<LinearLayout>(R.id.nav_mood_container).setOnClickListener {
            val intent = Intent(this, MoodPageActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_settings_container).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupReminderControls() {
        // Load saved interval
        val savedInterval = prefs.getInt(KEY_REMINDER_INTERVAL, 30) // Default to 30 minutes
        reminderIntervalInput.setText(savedInterval.toString())
        updateReminderStatus()
        
        // Setup button click listener
        setReminderButton.setOnClickListener {
            val inputText = reminderIntervalInput.text.toString().trim()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter a number of minutes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val interval = inputText.toIntOrNull()
            if (interval == null || interval < 1 || interval > 1440) {
                Toast.makeText(this, "Please enter a valid number between 1 and 1440 minutes (24 hours)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            prefs.edit().putInt(KEY_REMINDER_INTERVAL, interval).apply()
            updateReminderStatus()
            Toast.makeText(this, "Water reminder set for every $interval minute(s)!", Toast.LENGTH_SHORT).show()
        }
        
        // Test notification button
        testNotificationButton.setOnClickListener {
            if (checkNotificationPermission()) {
                sendTestNotification()
            } else {
                requestNotificationPermission()
            }
        }
    }
    
    private fun updateReminderStatus() {
        val interval = prefs.getInt(KEY_REMINDER_INTERVAL, 30)
        reminderStatus.text = "Reminder set for every $interval minute(s)"
        reminderStatus.setTextColor(getColor(R.color.primary_green))
    }
    
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notifications are allowed by default
            true
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    private fun sendTestNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8+
        val channelId = "water_reminder_channel"
        val channelName = "Water Reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_hydration)
            .setContentTitle("💧 Time to Drink Water!")
            .setContentText("Stay hydrated! Remember to drink a glass of water.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
        
        // Show notification
        notificationManager.notify(999, notification)
        Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show()
    }
}