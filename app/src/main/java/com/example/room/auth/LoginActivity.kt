package com.example.room.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.room.MainActivity
import com.example.room.R
import com.example.room.admin.AdminDashboardActivity
import com.example.room.database.DatabaseHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_login)

            dbHelper = DatabaseHelper(this)

            val edtUsername = findViewById<EditText>(R.id.edtUsername)
            val edtPassword = findViewById<EditText>(R.id.edtPassword)
            val btnLogin = findViewById<Button>(R.id.btnLogin)
            val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

            btnLogin.setOnClickListener {
                val username = edtUsername.text.toString().trim()
                val password = edtPassword.text.toString().trim()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val user = dbHelper.checkLogin(username, password)

                    if (user != null) {
                        // Lưu thông tin đăng nhập
                        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putInt("USER_ID", user.id)
                            putString("USERNAME", user.username)
                            putString("FULL_NAME", user.fullName)
                            putInt("ROLE", user.role)
                            putBoolean("IS_LOGGED_IN", true)
                            apply()
                        }

                        Log.d(TAG, "Login success: ${user.username}, role: ${user.role}")

                        if (user.role == 1) {
                            // Admin
                            Toast.makeText(this, "Chào Admin: ${user.fullName}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            // Khách hàng
                            Toast.makeText(this, "Chào ${user.fullName}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login error", e)
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            tvGoToRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            Toast.makeText(this, "Lỗi khởi tạo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}