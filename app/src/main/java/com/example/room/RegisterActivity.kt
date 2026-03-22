package com.example.room

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                Toast.makeText(this, getString(R.string.register_toast_empty), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.register_toast_success), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.register_toast_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }
}