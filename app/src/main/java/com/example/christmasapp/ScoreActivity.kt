package com.example.christmasapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class ScoreActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // Retrieve the score from the intent extras.
        val score = intent.getIntExtra("score", 0)
        val tvScore = findViewById<TextView>(R.id.tv_score)
        tvScore.text = "High Score: $score"

        // Set up the Play Again button.
        val playAgainButton = findViewById<Button>(R.id.btn_play_again)
        playAgainButton.setOnClickListener {
            // Restart the game by launching MainActivity (or WelcomeActivity if desired)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
