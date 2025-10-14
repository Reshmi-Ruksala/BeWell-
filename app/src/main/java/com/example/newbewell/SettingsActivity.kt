package com.example.newbewell

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var notificationSwitch: Switch
    private lateinit var exportDataLayout: LinearLayout
    private lateinit var resetDataLayout: LinearLayout
    private lateinit var backupButton: Button
    private lateinit var restoreButton: Button
    
    private lateinit var prefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
    
    // Permission request launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            notificationSwitch.isChecked = false
        }
    }
    
    // File picker launcher for backup/restore
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            try {
                backupDataToFile(it)
            } catch (e: Exception) {
                Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val fileRestoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                restoreDataFromFile(it)
            } catch (e: Exception) {
                Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupSharedPreferences()
        initializeViews()
        setupClickListeners()
        loadSettings()
    }
    
    private fun setupSharedPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private fun initializeViews() {
        notificationSwitch = findViewById(R.id.notificationSwitch)
        exportDataLayout = findViewById(R.id.exportDataLayout)
        resetDataLayout = findViewById(R.id.resetDataLayout)
        backupButton = findViewById(R.id.backupButton)
        restoreButton = findViewById(R.id.restoreButton)
    }
    
    private fun setupClickListeners() {
        // Navigation
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
            val intent = Intent(this, HydrationActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_mood_container).setOnClickListener {
            val intent = Intent(this, MoodPageActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_settings_container).setOnClickListener {
            Toast.makeText(this, "You're already on the settings page!", Toast.LENGTH_SHORT).show()
        }
        
        // Notification switch
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkNotificationPermission()
            } else {
                disableNotifications()
            }
        }
        
        // Export Data
        exportDataLayout.setOnClickListener {
            exportDataToText()
        }
        
        // Reset Data
        resetDataLayout.setOnClickListener {
            showResetDataDialog()
        }
        
        // Backup Button
        backupButton.setOnClickListener {
            filePickerLauncher.launch("bewell_backup_${getCurrentDateString()}.txt")
        }
        
        // Restore Button
        restoreButton.setOnClickListener {
            fileRestoreLauncher.launch(arrayOf("text/plain"))
        }
    }
    
    private fun loadSettings() {
        val notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        notificationSwitch.isChecked = notificationsEnabled
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                enableNotifications()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            enableNotifications()
        }
    }
    
    private fun enableNotifications() {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply()
        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
    }
    
    private fun disableNotifications() {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply()
        Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportDataToText() {
        try {
            val data = StringBuilder()
            data.append("BeWell+ Data Export\n")
            data.append("Generated: ${getCurrentDateTimeString()}\n\n")
            
            // Export mood data
            val moodPrefs = getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
            val moodEntries = moodPrefs.getString("mood_entries", "") ?: ""
            data.append("=== MOOD DATA ===\n")
            data.append("Mood Entries: $moodEntries\n\n")
            
            // Export hydration data
            val hydrationPrefs = getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
            val dailyGoal = hydrationPrefs.getInt("daily_goal", 8)
            val today = DateUtils.getCurrentDateString()
            val glassesConsumed = hydrationPrefs.getInt("glasses_consumed_$today", 0)
            data.append("=== HYDRATION DATA ===\n")
            data.append("Daily Goal: $dailyGoal\n")
            data.append("Today's Consumption: $glassesConsumed\n\n")
            
            // Export habit data
            val habitPrefs = getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)
            val habits = habitPrefs.getString("habits", "") ?: ""
            val completions = habitPrefs.getString("completions", "") ?: ""
            data.append("=== HABIT DATA ===\n")
            data.append("Habits: $habits\n")
            data.append("Completions: $completions\n\n")
            
            // Export settings
            data.append("=== SETTINGS ===\n")
            data.append("Notifications Enabled: ${prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)}\n")
            
            // Save to file
            val fileName = "bewell_export_${getCurrentDateString()}.txt"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            FileWriter(file).use { writer ->
                writer.write(data.toString())
            }
            
            Toast.makeText(this, "Data exported to: $fileName", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showResetDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Reset All Data")
            .setMessage("Are you sure you want to reset all data? This action cannot be undone.\n\nThis will delete:\n• All mood entries\n• All habit data\n• All hydration data\n• All settings")
            .setPositiveButton("Reset") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetAllData() {
        try {
            // Clear mood data
            val moodPrefs = getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
            moodPrefs.edit().clear().apply()
            
            // Clear hydration data
            val hydrationPrefs = getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
            hydrationPrefs.edit().clear().apply()
            
            // Clear habit data
            val habitPrefs = getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)
            habitPrefs.edit().clear().apply()
            
            // Clear homepage data
            val homePrefs = getSharedPreferences("NewBeWellPrefs", Context.MODE_PRIVATE)
            homePrefs.edit().clear().apply()
            
            // Keep settings but reset notifications
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply()
            notificationSwitch.isChecked = true
            
            Toast.makeText(this, "All data has been reset", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Reset failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun backupDataToFile(uri: android.net.Uri) {
        try {
            val data = StringBuilder()
            data.append("BeWell+ Backup\n")
            data.append("Generated: ${getCurrentDateTimeString()}\n\n")
            
            // Backup all SharedPreferences
            val allPrefs = listOf(
                "mood_prefs",
                "hydration_prefs", 
                "habit_tracker",
                "NewBeWellPrefs",
                PREFS_NAME
            )
            
            allPrefs.forEach { prefsName ->
                val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                data.append("=== $prefsName ===\n")
                val allEntries = prefs.all
                allEntries.forEach { (key, value) ->
                    data.append("$key = $value\n")
                }
                data.append("\n")
            }
            
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data.toString().toByteArray())
            }
            
            Toast.makeText(this, "Backup created successfully", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            throw Exception("Failed to create backup: ${e.message}")
        }
    }
    
    private fun restoreDataFromFile(uri: android.net.Uri) {
        AlertDialog.Builder(this)
            .setTitle("📂 Restore from File")
            .setMessage("Are you sure you want to restore data from file? This will overwrite all current data.")
            .setPositiveButton("Restore") { _, _ ->
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val data = inputStream.bufferedReader().readText()
                        parseAndRestoreData(data)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun parseAndRestoreData(backupData: String) {
        try {
            val lines = backupData.split("\n")
            var currentPrefsName = ""
            var currentPrefs: SharedPreferences? = null
            
            // First, clear all existing data
            clearAllAppData()
            
            for (line in lines) {
                when {
                    line.startsWith("=== ") && line.endsWith(" ===") -> {
                        // New SharedPreferences section
                        currentPrefsName = line.substring(4, line.length - 4)
                        currentPrefs = getSharedPreferences(currentPrefsName, Context.MODE_PRIVATE)
                    }
                    line.contains(" = ") && currentPrefs != null -> {
                        // Key-value pair
                        val parts = line.split(" = ", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0]
                            val value = parts[1]
                            setPreferenceValue(currentPrefs, key, value)
                        }
                    }
                }
            }
            
            Toast.makeText(this, "Data restored successfully!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Restore parsing failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setPreferenceValue(prefs: SharedPreferences, key: String, value: String) {
        val editor = prefs.edit()
        
        when {
            value == "true" -> editor.putBoolean(key, true)
            value == "false" -> editor.putBoolean(key, false)
            value.matches(Regex("\\d+")) -> editor.putInt(key, value.toInt())
            value.matches(Regex("\\d+\\.\\d+")) -> editor.putFloat(key, value.toFloat())
            value.matches(Regex("\\d+L")) -> editor.putLong(key, value.substring(0, value.length - 1).toLong())
            else -> editor.putString(key, value)
        }
        
        editor.apply()
    }
    
    private fun clearAllAppData() {
        // Clear all SharedPreferences before restoring
        val allPrefs = listOf(
            "mood_prefs",
            "hydration_prefs", 
            "habit_tracker",
            "NewBeWellPrefs",
            PREFS_NAME
        )
        
        allPrefs.forEach { prefsName ->
            val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun getCurrentDateTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}