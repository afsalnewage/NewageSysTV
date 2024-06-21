package com.dev.nastv

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val splashIcon: ImageView = findViewById(R.id.splashIcon)
        // Create scaling animation
        val scaleX = ObjectAnimator.ofFloat(splashIcon, "scaleX", 0.5f, 1.5f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(splashIcon, "scaleY", 0.5f, 1.5f, 1.0f)
        scaleX.duration = 2000 // 2 seconds
        scaleY.duration = 2000 // 2 seconds
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        // Start scaling animation
        scaleX.start()
        scaleY.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}