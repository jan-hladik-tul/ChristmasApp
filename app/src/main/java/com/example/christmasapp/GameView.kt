package com.example.christmasapp

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    // Thread and running flag for the game loop
    private var thread: Thread? = null
    @Volatile private var running = false

    // Public speed multiplier for falling objects (adjustable via an in-game button)
    var speedMultiplier: Float = 1.0f

    // Score multiplier power-up variable (default is 1; becomes 2 when active)
    private var scoreMultiplierPowerup: Int = 1

    // Basket/Sleigh properties
    private var basketX = 0f
    private var basketY = 0f
    private var basketWidth = 200f
    private var basketHeight = 100f
    private lateinit var basketBitmap: Bitmap

    // Gift properties
    private data class Gift(var x: Float, var y: Float, val bitmap: Bitmap)
    private val gifts = mutableListOf<Gift>()
    private val giftFallSpeed = 10f
    private lateinit var giftBitmap: Bitmap

    // Power-up properties (for score multiplier)
    private data class PowerUp(var x: Float, var y: Float, val bitmap: Bitmap)
    private val powerUps = mutableListOf<PowerUp>()
    private val powerUpFallSpeed = 8f
    private lateinit var powerUpBitmap: Bitmap  // Uses R.drawable.multiplier

    // Background image
    private lateinit var backgroundBitmap: Bitmap

    // Score and paint for drawing the score text
    private var score = 0
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Handler to reset the score multiplier after a set duration
    private val handler = Handler(Looper.getMainLooper())

    init {
        holder.addCallback(this)
        isFocusable = true

        // Load the gift drawable, tint it red, and convert it to a bitmap.
        giftBitmap = ContextCompat.getDrawable(context, R.drawable.gift)
            ?.apply { setTint(Color.RED) }
            ?.toBitmap(50, 50)
            ?: throw IllegalStateException("Failed to load gift drawable")

        // Load the multiplier power-up drawable and convert it to a bitmap.
        powerUpBitmap = ContextCompat.getDrawable(context, R.drawable.multiplier)
            ?.toBitmap(70, 70)
            ?: throw IllegalStateException("Failed to load power-up drawable")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Center the basket horizontally near the bottom.
        basketX = (w / 2 - basketWidth / 2)
        basketY = h - basketHeight - 50f

        // Load and tint the sleigh drawable (vector asset) to green, then convert it to a bitmap.
        basketBitmap = ContextCompat.getDrawable(context, R.drawable.sleigh)
            ?.apply { setTint(Color.GREEN) }
            ?.toBitmap(basketWidth.toInt(), basketHeight.toInt())
            ?: throw IllegalStateException("Failed to load sleigh drawable")

        // Load the background drawable and scale it to the view's dimensions.
        backgroundBitmap = ContextCompat.getDrawable(context, R.drawable.christmas_background)
            ?.toBitmap(w, h)
            ?: throw IllegalStateException("Failed to load background drawable")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Not used in this example.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue

            val canvas = holder.lockCanvas()
            canvas?.let {
                updateGame()
                drawGame(it)
                holder.unlockCanvasAndPost(it)
            }
            // Delay roughly 30 ms per frame (~33 FPS)
            Thread.sleep(30)
        }
    }

    // Update positions and handle collisions for gifts and power-ups.
    private fun updateGame() {
        // Update gifts
        val giftIterator = gifts.iterator()
        while (giftIterator.hasNext()) {
            val gift = giftIterator.next()
            gift.y += giftFallSpeed * speedMultiplier

            // Check collision with the basket/sleigh.
            if (gift.y + gift.bitmap.height >= basketY &&
                gift.x + gift.bitmap.width >= basketX &&
                gift.x <= basketX + basketWidth) {
                // Increase score by the current multiplier value.
                score += scoreMultiplierPowerup
                giftIterator.remove()
            } else if (gift.y > height) {
                giftIterator.remove()
            }
        }

        // Spawn new gifts randomly (about 10% chance per frame)
        if (Random.nextInt(0, 100) < 10) {
            val x = Random.nextFloat() * (width - giftBitmap.width)
            gifts.add(Gift(x, -giftBitmap.height.toFloat(), giftBitmap))
        }

        // Update power-ups
        val powerUpIterator = powerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val powerUp = powerUpIterator.next()
            powerUp.y += powerUpFallSpeed * speedMultiplier

            // Check collision with the basket.
            if (powerUp.y + powerUp.bitmap.height >= basketY &&
                powerUp.x + powerUp.bitmap.width >= basketX &&
                powerUp.x <= basketX + basketWidth) {
                // Activate a 2× score multiplier for 5 seconds.
                activateScoreMultiplier(2, 5000)
                powerUpIterator.remove()
            } else if (powerUp.y > height) {
                powerUpIterator.remove()
            }
        }

        // Spawn new power-ups rarely (about a 0.1% chance per frame)
        if (Random.nextInt(0, 1000) < 2) {
            val x = Random.nextFloat() * (width - powerUpBitmap.width)
            powerUps.add(PowerUp(x, -powerUpBitmap.height.toFloat(), powerUpBitmap))
        }
    }

    // Draw all game elements: background, gifts, power-ups, basket, and score.
    private fun drawGame(canvas: Canvas) {
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
        for (gift in gifts) {
            canvas.drawBitmap(gift.bitmap, gift.x, gift.y, null)
        }
        for (powerUp in powerUps) {
            canvas.drawBitmap(powerUp.bitmap, powerUp.x, powerUp.y, null)
        }
        canvas.drawBitmap(basketBitmap, basketX, basketY, null)
        canvas.drawText("Score: $score", 50f, 100f, textPaint)
    }

    // Handle touch events to move the basket horizontally.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                basketX = event.x - basketWidth / 2
                if (basketX < 0) basketX = 0f
                if (basketX + basketWidth > width) basketX = width - basketWidth
            }
        }
        return true
    }

    // Public method to get the current score.
    fun getScore(): Int = score

    // Pause and resume methods to control the game loop.
    fun pause() {
        running = false
        thread?.join()
    }

    fun resume() {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    // Activate a score multiplier for a set duration.
    // When called, the score multiplier becomes 'multiplier' (e.g., 2×) and resets to 1 after 'durationMillis' milliseconds.
    private fun activateScoreMultiplier(multiplier: Int, durationMillis: Long) {
        scoreMultiplierPowerup = multiplier
        handler.postDelayed({
            scoreMultiplierPowerup = 1
        }, durationMillis)
    }
}
