package com.demo.radiotask.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.radiotask.databinding.ActivitySplashScreenAlertBinding

class SplashAlertActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySplashScreenAlertBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.alertButton.setOnClickListener{
                val intent = Intent(this, HomePageActivity::class.java)
                startActivity(intent)
                finish()
        }

    }
}