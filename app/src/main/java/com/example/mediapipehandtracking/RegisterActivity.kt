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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class RegisterActivity : AppCompatActivity() {
    lateinit var btn_register:Button
    lateinit var btn_login:Button
    lateinit var user_name:EditText
    lateinit var psw1:EditText
    lateinit var psw2:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        init()
        btn_login.setOnClickListener { view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        registerListener()

    }

    private fun init(){
        btn_register = findViewById<Button>(R.id.btn_register)
        btn_login = findViewById<Button>(R.id.btn_login)
        user_name=findViewById<EditText>(R.id.userName)
        psw1=findViewById<EditText>(R.id.passwd)
        psw2=findViewById<EditText>(R.id.passwd2)
    }

    private fun registerListener(){
        btn_register.setOnClickListener {
            if (validateUserName() && validatePassword()) {
                val responseRegisterClass = ResponseRegisterClass(
                    user_name.getText().toString(),
                    psw1.getText().toString()
                )
                val apiService: ApiService = Network.getInstance().create(ApiService::class.java)
                apiService.addUser(responseRegisterClass)
                    .enqueue(object : Callback<ResponseClass?> {
                        override fun onResponse(
                            call: Call<ResponseClass?>,
                            response: Response<ResponseClass?>
                        ) {
                            if (response.body() != null) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "something went wrong! please try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseClass?>, t: Throwable) {}
                    })
            }
        }
    }

    private fun validatePassword(): Boolean {
        if (psw1.text.toString().length < 6) {
            psw1.setError("password must be atleast 6 characters")
            psw1.requestFocus()
            return false
        } else if (TextUtils.isEmpty(psw1.getText().toString())) {
            psw1.setError("password cannot be empty")
            psw1.requestFocus()
            return false
        }
        if (psw1.text.toString() != psw2.text.toString()){
            psw2.setError("password should be the same")
            psw2.requestFocus()
            return false
        }
        return true
    }

    private fun validateUserName(): Boolean {
        if (TextUtils.isEmpty(user_name.getText().toString())) {
            user_name.setError("username cannot be empty")
            user_name.requestFocus()
            return false
        }
        return true
    }
}