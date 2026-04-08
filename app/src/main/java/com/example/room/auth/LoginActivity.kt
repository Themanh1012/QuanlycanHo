package com.example.room.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
            dbHelper.ensureDefaultUsers()

            val edtUsername = findViewById<EditText>(R.id.edtUsername)
            val edtPassword = findViewById<EditText>(R.id.edtPassword)
            val btnLogin = findViewById<Button>(R.id.btnLogin)
            val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
            val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

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
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("userId", user.id)
                            putString("username", user.username)
                            putString("fullName", user.fullName)
                            putInt("role", user.role)
                            putBoolean("IS_LOGGED_IN", true)
                            apply()
                        }

                        if (user.role == 1) {
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        finish()
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

            tvForgotPassword.setOnClickListener {
                showForgotPasswordDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            Toast.makeText(this, "Lỗi khởi tạo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quên mật khẩu")
        
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val edtUser = view.findViewById<EditText>(R.id.edtResetUsername)
        val edtName = view.findViewById<EditText>(R.id.edtResetFullName)
        val edtNewPass = view.findViewById<EditText>(R.id.edtResetNewPassword)
        
        builder.setView(view)
        builder.setPositiveButton("Đổi mật khẩu") { dialog, _ ->
            val username = edtUser.text.toString().trim()
            val fullName = edtName.text.toString().trim()
            val newPass = edtNewPass.text.toString().trim()
            
            if (username.isEmpty() || fullName.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            
            if (dbHelper.resetPassword(username, fullName, newPass)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Thông tin không chính xác!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}