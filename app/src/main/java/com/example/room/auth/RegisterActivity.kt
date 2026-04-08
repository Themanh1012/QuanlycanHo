package com.example.room.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val edtFullName = findViewById<EditText>(R.id.edtFullName)
        val edtUsername = findViewById<EditText>(R.id.edtRegUsername)
        val edtPassword = findViewById<EditText>(R.id.edtRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra tài khoản không được chứa dấu cách
            if (username.contains(" ")) {
                Toast.makeText(this, "Tài khoản không được chứa khoảng trắng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu không được nhỏ hơn 4 ký tự
            if (password.length < 4) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 4 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra tài khoản đã tồn tại chưa
            if (dbHelper.isUsernameExists(username)) {
                Toast.makeText(this, "Tài khoản này đã tồn tại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                username = username,
                password = password,
                fullName = fullName,
                role = 2
            )

            val result = dbHelper.insertUser(newUser)

            if (result != -1L) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}