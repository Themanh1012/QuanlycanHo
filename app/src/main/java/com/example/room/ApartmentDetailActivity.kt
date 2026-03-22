package com.example.room

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ApartmentDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Hỗ trợ tràn viền màn hình
        setContentView(R.layout.activity_apartment_detail)

        // Bắt sự kiện bấm nút Back (Mũi tên trên cùng bên trái)
        val btnBack = findViewById<CardView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng màn hình chi tiết, tự động quay về Trang chủ
        }
    }
}