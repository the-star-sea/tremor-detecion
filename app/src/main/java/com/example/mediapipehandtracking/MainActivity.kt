package com.example.mediapipehandtracking

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediapipehandtracking.remote.ApiService
import com.example.mediapipehandtracking.remote.Network
import com.example.mediapipehandtracking.responseClasses.ResponseClass
import com.example.mediapipehandtracking.responseClasses.ResponseRegisterClass

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    lateinit var userName : EditText
    lateinit var Password : EditText
    lateinit var btn_register : Button
    lateinit var btn_login : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()

        loginListener()
        btn_register.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun init(){
        userName= findViewById(R.id.userName)
        Password = findViewById(R.id.passwd)
        btn_register = findViewById(R.id.btn_register)
        btn_login = findViewById(R.id.btnLogin)
    }

    private fun loginListener(){
        btn_login.setOnClickListener{
            if (validateUsername() && validatePassword()) {
                val responseRegisterClass = ResponseRegisterClass(
                    userName.text.toString(),
                    Password.text.toString()
                )
                val apiService: ApiService = Network.getInstance().create(ApiService::class.java)
                apiService.getUser(responseRegisterClass)
                    .enqueue(object : Callback<ResponseClass?> {
                        override fun onResponse(
                            call: Call<ResponseClass?>,
                            response: Response<ResponseClass?>
                        ) {
                            if (response.body() != null) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("username", response.body()!!.username)
                                startActivity(intent)
                            }
                        }

                        override fun onFailure(call: Call<ResponseClass?>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                "Invalid credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }

    private fun validateUsername(): Boolean {
        if (TextUtils.isEmpty(userName.getText().toString())) {
            userName.setError("username cannot be empty")
            userName.requestFocus()
            return false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if (TextUtils.isEmpty(Password.getText().toString())) {
            Password.setError("password cannot be empty")
            Password.requestFocus()
            return false
        }
        return true
    }
}
