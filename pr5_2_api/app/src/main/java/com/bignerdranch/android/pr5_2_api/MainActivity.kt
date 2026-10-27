package com.bignerdranch.android.pr5_2_api

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etUserPassword = findViewById<EditText>(R.id.etUserPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val prefs = getSharedPreferences("MusicPrefs", Context.MODE_PRIVATE)

        // Автоматический вход, если пользователь уже авторизовывался ранее
        if (prefs.contains("username")) {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString()
            val pass = etUserPassword.text.toString()

            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Заполните все поля ввода!", Toast.LENGTH_SHORT).show()
            } else {
                // Сохраняем имя в SharedPreferences
                prefs.edit().putString("username", user).apply()

                // Переход на второй экран
                val intent = Intent()
                intent.setClassName(this, "com.bignerdranch.android.pr5_2_api.SearchActivity")
                startActivity(intent)
                finish()
            }
        }
    }
}