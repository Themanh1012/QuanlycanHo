package com.example.quanlycanho.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import com.example.quanlycanho.R
import com.example.quanlycanho.database.DatabaseHelper

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tvTotalApartments: TextView
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvOccupied: TextView
    private lateinit var tvAvailable: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        databaseHelper = DatabaseHelper(this)

        initViews()
        loadStatistics()
        setupClickListeners()
    }

    private fun initViews() {
        tvTotalApartments = findViewById(R.id.tvTotalApartments)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvOccupied = findViewById(R.id.tvOccupied)
        tvAvailable = findViewById(R.id.tvAvailable)
    }

    private fun loadStatistics() {
        val totalApartments = databaseHelper.getTotalApartments()
        val totalUsers = databaseHelper.getTotalUsers()
        val occupiedCount = databaseHelper.getOccupiedApartmentsCount()
        val availableCount = totalApartments - occupiedCount

        tvTotalApartments.text = totalApartments.toString()
        tvTotalUsers.text = totalUsers.toString()
        tvOccupied.text = occupiedCount.toString()
        tvAvailable.text = availableCount.toString()
    }

    private fun setupClickListeners() {
        // Quản lý căn hộ
        findViewById<CardView>(R.id.cardApartments).setOnClickListener {
            startActivity(Intent(this, ManageApartmentsActivity::class.java))
        }

        // Quản lý người dùng
        findViewById<CardView>(R.id.cardUsers).setOnClickListener {
            startActivity(Intent(this, ManageUserActivity::class.java))
        }

        // Đăng xuất
        findViewById<ImageView>(R.id.ivLogout).setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, Class.forName("com.example.quanlycanho.auth.LoginActivity"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }
}