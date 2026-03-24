package com.example.room.user

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper

class EditProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var edtFullName: EditText
    private lateinit var edtUsername: EditText
    private lateinit var btnSave: Button

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackFromEditProfile)
        btnBack.setOnClickListener { finish() }

        // ====== FIND VIEW ======
        edtFullName = findViewById(R.id.edtFullName)
        edtUsername = findViewById(R.id.edtUsername)
        btnSave = findViewById(R.id.btnSaveProfile)

        // ====== LẤY USER ======
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        userId = sharedPref.getInt("USER_ID", -1)
        val fullName = sharedPref.getString("FULL_NAME", "")
        val username = sharedPref.getString("USERNAME", "")

        edtFullName.setText(fullName)
        edtUsername.setText(username)

        // ====== SAVE ======
        btnSave.setOnClickListener {

            val newName = edtFullName.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Không được để trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.updateUserInfo(userId, newName, 2)

            if (result > 0) {

                // 🔥 UPDATE SHARED PREF
                sharedPref.edit()
                    .putString("FULL_NAME", newName)
                    .apply()

                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                finish()

            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}