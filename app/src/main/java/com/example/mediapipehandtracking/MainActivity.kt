package com.example.mediapipehandtracking

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var userName : TextView = findViewById(R.id.userName)
        var Password : TextView = findViewById(R.id.passwd)
        var btn_register : Button = findViewById(R.id.btn_register)
        var btn_login : Button = findViewById(R.id.btnLogin)

        var username = intent.getStringExtra("username")
        var password = intent.getStringExtra("psw")
        userName.text = username
        Password.text = password

        btn_register.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        btn_login.setOnClickListener{view ->
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
        }

    }
}
