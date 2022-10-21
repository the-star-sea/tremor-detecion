package com.example.mediapipehandtracking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var btn_detection = findViewById<ImageButton>(R.id.btn_detection)
        btn_detection.setOnClickListener{view ->
            val intent = Intent(this, DetectActivity::class.java)
            startActivity(intent)
        }
    }
}