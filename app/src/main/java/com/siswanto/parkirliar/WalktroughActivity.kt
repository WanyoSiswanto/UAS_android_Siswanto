package com.siswanto.parkirliar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class WalkthroughActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walktrough)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnSkip = findViewById<Button>(R.id.btnSkip)

        viewPager.adapter = WalkthroughAdapter(this)

        btnNext.setOnClickListener {
            if (viewPager.currentItem < 1) {
                viewPager.currentItem += 1
            } else {
                // Transisi ke halaman berikutnya setelah walkthrough selesai
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        btnSkip.setOnClickListener {
            // Langsung ke HomeActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}