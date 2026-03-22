package com.example.room.listing

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R

class AddPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Nút Back
        val btnBack = findViewById<ImageView>(R.id.btnBackFromAddPost)
        btnBack.setOnClickListener {
            finish()
        }

        // Nút Đăng tin (Hiện thông báo giả lập)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitPost)
        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Đăng tin thành công! Chờ admin duyệt.", Toast.LENGTH_SHORT).show()
            finish() // Đăng xong thì tự quay về trang chủ
        }
    }
}