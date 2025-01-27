package com.example.auditpro

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Deadline : AppCompatActivity() {
    private var isRunning = false
    private var elapsedTime = 0L
    private var startTime: Long? = null
    private lateinit var stopwatchText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val handler = Handler()

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime += 1000 // Increment elapsed time by 1 second
                updateStopwatchDisplay()
                handler.postDelayed(this, 1000) // Repeat every second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_deadline)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        stopwatchText = findViewById(R.id.stopwatch_text)
        val startButton: Button = findViewById(R.id.start_button)
        val stopButton: Button = findViewById(R.id.stop_button)
        val resetButton: Button = findViewById(R.id.reset_button)

        sharedPreferences = getSharedPreferences("StopwatchPrefs", MODE_PRIVATE)

        // Load saved elapsed time and start time
        elapsedTime = sharedPreferences.getLong("elapsedTime", 0)
        startTime = sharedPreferences.getLong("startTime", -1)

        // If there's a saved start time, calculate the difference
        if (startTime != -1L) {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - startTime!!
            elapsedTime += timeDifference // Add the difference to elapsed time
            updateStopwatchDisplay()
        }

        startButton.setOnClickListener {
            isRunning = true
            startTime = System.currentTimeMillis() // Set start time when started
            handler.post(runnable) // Start the stopwatch
        }

        stopButton.setOnClickListener {
            isRunning = false
            handler.removeCallbacks(runnable) // Stop the stopwatch
            sharedPreferences.edit().putLong("startTime", startTime ?: -1).apply() // Save start time
        }

        resetButton.setOnClickListener {
            elapsedTime = 0 // Reset elapsed time
            updateStopwatchDisplay()
            sharedPreferences.edit().clear().apply() // Clear saved preferences
            startTime = null // Reset start time
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume stopwatch if it was running
        if (isRunning) {
            startTime = System.currentTimeMillis() // Update start time
            handler.post(runnable) // Restart the timer
        } else if (startTime != null) {
            // If the stopwatch was not running but there is a saved start time
            isRunning = true
            handler.post(runnable) // Start ticking again
        }
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
        handler.removeCallbacks(runnable) // Stop the stopwatch
        // Save elapsed time and current start time
        sharedPreferences.edit().putLong("elapsedTime", elapsedTime).apply()
        sharedPreferences.edit().putLong("startTime", startTime ?: -1).apply()
    }

    private fun updateStopwatchDisplay() {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
        stopwatchText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
