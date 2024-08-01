package com.example.guru2_cleanspirit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guru2_cleanspirit.src.DBHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.editTextEmail)
        etPassword = findViewById(R.id.editTextPassword)
        btnRegister = findViewById(R.id.buttonRegister)
        btnLogin = findViewById(R.id.buttonSignIn)

        dbHelper = DBHelper(this)

        // 회원가입 버튼 클릭 시
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val result = dbHelper.insertUser(username, password)
                if (result) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 로그인 버튼 클릭 시
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val result = dbHelper.checkUser(username, password)
                if (result) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish() // 현재 액티비티를 종료하여 뒤로 가기 버튼 클릭 시 로그인 화면으로 돌아오지 않도록 함
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "잘못된 사용자 이름 또는 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
