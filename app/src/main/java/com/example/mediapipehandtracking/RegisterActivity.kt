package com.example.mediapipehandtracking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        var btn_register = findViewById<Button>(R.id.btn_register)
        var btn_login = findViewById<Button>(R.id.btn_login)

        var user_name=findViewById<EditText>(R.id.userName)
        var psw1=findViewById<EditText>(R.id.passwd)
        var psw2=findViewById<EditText>(R.id.passwd2)

        btn_login.setOnClickListener { view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btn_register.setOnClickListener { view ->
            checkDataEntered()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("username",user_name.text.toString());
            intent.putExtra("psw",psw1.text.toString());
            startActivity(intent);
        }
    }

    fun checkDataEntered(){

    }
}