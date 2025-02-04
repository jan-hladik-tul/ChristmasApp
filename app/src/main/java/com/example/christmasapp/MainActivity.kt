package com.example.christmasapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    // Array of speed multipliers (from previous example)
    private val multipliers = arrayOf(1f, 2f, 5f)
    private var currentMultiplierIndex = 0

    // Set game duration (e.g., 60 seconds)
    private val gameDurationMillis: Long = 60000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add GameView to the layout.
        val container = findViewById<FrameLayout>(R.id.game_container)
        gameView = GameView(this)
        container.addView(gameView, 0) // Add behind the overlay views

        // Set up the speed button.
        val speedButton = findViewById<Button>(R.id.btn_speed)
        speedButton.setOnClickListener {
            currentMultiplierIndex = (currentMultiplierIndex + 1) % multipliers.size
            gameView.speedMultiplier = multipliers[currentMultiplierIndex]
            speedButton.text = "Speed: ${multipliers[currentMultiplierIndex]}x"
        }

        // Get reference to the timer TextView.
        val tvTimer = findViewById<TextView>(R.id.tv_timer)

        // Start a CountDownTimer for the game duration.
        object : CountDownTimer(gameDurationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the timer TextView (seconds remaining).
                val secondsRemaining = millisUntilFinished / 1000
                tvTimer.text = "Time: $secondsRemaining s"
            }

            override fun onFinish() {
                // End the game.
                gameView.pause()
                // Optionally update the timer display to zero.
                tvTimer.text = "Time: 0 s"
                // Launch ScoreActivity, passing the score.
                val intent = Intent(this@MainActivity, ScoreActivity::class.java)
                intent.putExtra("score", gameView.getScore())
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }
}
