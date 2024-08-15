package com.siswanto.parkirliar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animasi titik-titik
        val loadingText = findViewById<TextView>(R.id.loadingText)
        val animation = AnimationUtils.loadAnimation(this, R.anim.dot_blink)
        loadingText.startAnimation(animation)

        // Menunggu SPLASH_TIME_OUT kemudian pindah ke WalkthroughActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WalkthroughActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }
}