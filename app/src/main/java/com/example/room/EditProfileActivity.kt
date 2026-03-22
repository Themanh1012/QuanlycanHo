package com.example.room

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBackFromEditProfile)
        btnBack.setOnClickListener {
            finish()
        }

        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        btnSave.setOnClickListener {
            Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}