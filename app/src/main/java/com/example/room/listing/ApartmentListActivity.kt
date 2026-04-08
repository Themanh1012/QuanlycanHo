package com.example.room.listing

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.ApartmentVerticalAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class ApartmentListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentVerticalAdapter
    private var list = ArrayList<Apartment>()
    private var filterType: String = "ALL" // Mặc định là hiện tất cả

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartment_list)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.rvApartmentsUser)

        // 1. Nhận "Mật mã" từ Trang chủ (HomeFragment)
        filterType = intent.getStringExtra("FILTER_TYPE") ?: "ALL"

        // 2. Đổi tiêu đề màn hình dựa theo mật mã
        val tvListTitle = findViewById<TextView>(R.id.tvListTitle)
        when (filterType) {
            "DIAMOND" -> tvListTitle.text = "Căn hộ Kim Cương"
            "VIP" -> tvListTitle.text = "Căn hộ VIP"
            "DISCOUNT" -> tvListTitle.text = "Ưu đãi & Giảm giá"
            else -> tvListTitle.text = "Tất cả Căn hộ"
        }

        // 3. Tải dữ liệu đã được lọc
        loadData()

        adapter = ApartmentVerticalAdapter(list) { apartment ->
            val intent = Intent(this, ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apartment.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btnBackFromAptList).setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        val allApts = dbHelper.getAllApartments()

        // Lọc dữ liệu trực tiếp dựa trên loại Badge
        val filtered = when (filterType) {
            "DIAMOND" -> allApts.filter { it.badge == "VIP KIM CƯƠNG" }
            "VIP" -> allApts.filter { it.badge == "HẠNG VÀNG" || it.badge == "HẠNG BẠC" }
            "DISCOUNT" -> allApts.filter { it.badge == "GIẢM GIÁ HOT" }
            else -> allApts
        }

        list.clear()
        list.addAll(filtered)
    }
}