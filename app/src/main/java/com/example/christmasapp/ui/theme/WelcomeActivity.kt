package com.example.christmasapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Set up the Play button to launch the game.
        val playButton = findViewById<Button>(R.id.btn_play)
        playButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Optionally finish to prevent returning to the welcome screen.
        }
    }
}
