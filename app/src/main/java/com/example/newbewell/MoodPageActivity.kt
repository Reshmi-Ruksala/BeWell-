package com.example.newbewell

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MoodPageActivity : AppCompatActivity() {
    
    private lateinit var moodHistoryRecyclerView: RecyclerView
    private lateinit var moodHistoryAdapter: SimpleMoodHistoryAdapter
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var prefs: SharedPreferences
    
    private var selectedEmoji: String? = null
    private val moodEntries = mutableListOf<SimpleMoodEntry>()
    
    companion object {
        private const val PREFS_NAME = "mood_prefs"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.moodpage)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupSharedPreferences()
        setupRecyclerViews()
        setupEmojiClickListeners()
        setupNavigationClickListeners()
        loadMoodData()
    }
    
    private fun initializeViews() {
        moodHistoryRecyclerView = findViewById(R.id.moodHistoryRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }
    
    private fun setupSharedPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun setupRecyclerViews() {
        moodHistoryAdapter = SimpleMoodHistoryAdapter(
            moodEntries = emptyList(),
            onDeleteClick = { moodEntry -> showDeleteMoodDialog(moodEntry) },
            getMoodName = { emoji -> getMoodName(emoji) }
        )
        
        moodHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoodPageActivity)
            adapter = moodHistoryAdapter
            isNestedScrollingEnabled = false
        }
    }
    
    private fun setupEmojiClickListeners() {
        val emojiIds = listOf(R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5, R.id.emoji6, R.id.emoji7, R.id.emoji8)
        val emojis = listOf("😢", "😕", "😐", "🙂", "😊", "😄", "🤩", "🥰")
        
        emojiIds.forEachIndexed { index, emojiId ->
            findViewById<TextView>(emojiId).setOnClickListener {
                selectedEmoji = emojis[index]
                clearEmojiSelection()
                it.isSelected = true
                logMood(selectedEmoji!!)
            }
        }
    }
    
    private fun setupNavigationClickListeners() {
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
            val intent = Intent(this, HydrationActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.nav_mood_container).setOnClickListener {
            Toast.makeText(this, "You're already on the mood page!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<LinearLayout>(R.id.nav_settings_container).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun loadMoodData() {
        // Load saved mood entries from SharedPreferences
        val savedEntries = prefs.getString(KEY_MOOD_ENTRIES, "") ?: ""
        moodEntries.clear()
        
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
        
        // Sort by date (newest first)
        moodEntries.sortByDescending { it.dateTime }
        
        // Update adapter
        moodHistoryAdapter.updateMoodEntries(moodEntries)
        
        // Show/hide empty state
        if (moodEntries.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            moodHistoryRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            moodHistoryRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun logMood(emoji: String) {
        val moodEntry = SimpleMoodEntry(
            emoji = emoji,
            dateTime = System.currentTimeMillis(),
            id = UUID.randomUUID().toString()
        )
        
        moodEntries.add(0, moodEntry) // Add to beginning
        saveMoodEntries()
        saveLatestMood(emoji) // Save latest mood for homepage
        loadMoodData()
        
        Toast.makeText(this, "Mood logged: $emoji", Toast.LENGTH_SHORT).show()
    }
    
    private fun clearEmojiSelection() {
        val emojiIds = listOf(R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5, R.id.emoji6, R.id.emoji7, R.id.emoji8)
        emojiIds.forEach { findViewById<TextView>(it).isSelected = false }
    }
    
    private fun saveMoodEntries() {
        val entriesString = moodEntries.joinToString("|") { "${it.emoji},${it.dateTime},${it.id}" }
        prefs.edit().putString(KEY_MOOD_ENTRIES, entriesString).apply()
    }
    
    private fun saveLatestMood(emoji: String) {
        // Save latest mood to NewBeWellPrefs for homepage display
        val homePrefs = getSharedPreferences("NewBeWellPrefs", Context.MODE_PRIVATE)
        val moodName = getMoodName(emoji)
        homePrefs.edit().putString("latest_mood_emoji", emoji).apply()
        homePrefs.edit().putString("latest_mood_name", moodName).apply()
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
    
    private fun showDeleteMoodDialog(moodEntry: SimpleMoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Delete Mood")
            .setMessage("Are you sure you want to delete this mood entry?\n\n${moodEntry.emoji} ${getMoodName(moodEntry.emoji)}")
            .setPositiveButton("Delete") { _, _ ->
                moodEntries.removeAll { it.id == moodEntry.id }
                saveMoodEntries()
                updateLatestMoodAfterDelete() // Update latest mood for homepage
                loadMoodData()
                Toast.makeText(this, "Mood entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateLatestMoodAfterDelete() {
        // Update latest mood to the most recent remaining mood entry
        if (moodEntries.isNotEmpty()) {
            val latestEntry = moodEntries.first() // First entry is the most recent
            saveLatestMood(latestEntry.emoji)
        } else {
            // No mood entries left, reset to default
            val homePrefs = getSharedPreferences("NewBeWellPrefs", Context.MODE_PRIVATE)
            homePrefs.edit().putString("latest_mood_emoji", "😊").apply()
            homePrefs.edit().putString("latest_mood_name", "Happy").apply()
        }
    }
}

// Simple data classes
data class SimpleMoodEntry(
    val emoji: String,
    val dateTime: Long,
    val id: String
)

class SimpleMoodHistoryAdapter(
    private var moodEntries: List<SimpleMoodEntry>,
    private val onDeleteClick: (SimpleMoodEntry) -> Unit,
    private val getMoodName: (String) -> String
) : RecyclerView.Adapter<SimpleMoodHistoryAdapter.MoodViewHolder>() {
    
    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moodEmoji: TextView = itemView.findViewById(R.id.moodEmoji)
        val moodText: TextView = itemView.findViewById(R.id.moodText)
        val moodDateTime: TextView = itemView.findViewById(R.id.moodDateTime)
        val deleteButton: TextView = itemView.findViewById(R.id.deleteButton)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MoodViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.mood_history_item_simple, parent, false)
        return MoodViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        holder.moodEmoji.text = moodEntry.emoji
        holder.moodText.text = getMoodName(moodEntry.emoji)
        
        val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        holder.moodDateTime.text = dateFormat.format(Date(moodEntry.dateTime))
        
        // Delete button click listener
        holder.deleteButton.setOnClickListener {
            onDeleteClick(moodEntry)
        }
        
        // Keep long press as alternative delete method
        holder.itemView.setOnLongClickListener {
            onDeleteClick(moodEntry)
            true
        }
    }
    
    override fun getItemCount(): Int = moodEntries.size
    
    fun updateMoodEntries(newEntries: List<SimpleMoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }
}