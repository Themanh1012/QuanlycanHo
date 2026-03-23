package com.example.room.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.room.R
import com.example.room.auth.LoginActivity
import com.example.room.database.DatabaseHelper

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalApartments: TextView
    private lateinit var tvAvailableCount: TextView
    private lateinit var tvRentedCount: TextView
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        dbHelper = DatabaseHelper(this)

        initViews()
        loadData()
        setupClickListeners()
        setupSearch()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalApartments = findViewById(R.id.tvTotalApartments)
        tvAvailableCount = findViewById(R.id.tvAvailableCount)
        tvRentedCount = findViewById(R.id.tvRentedCount)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun loadData() {
        val totalUsers = dbHelper.getAllUsers().size
        val totalApartments = dbHelper.getAllApartments().size
        val availableCount = dbHelper.getAvailableApartmentsCount()
        val rentedCount = dbHelper.getOccupiedApartmentsCount()

        tvTotalUsers.text = totalUsers.toString()
        tvTotalApartments.text = totalApartments.toString()
        tvAvailableCount.text = availableCount.toString()
        tvRentedCount.text = rentedCount.toString()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    val intent = Intent(this@AdminDashboardActivity, ManageApartmentsActivity::class.java)
                    intent.putExtra("search_query", s.toString())
                    startActivity(intent)
                    etSearch.text.clear()
                }
            }
        })
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardManageApartments).setOnClickListener {
            startActivity(Intent(this, ManageApartmentsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardManageUsers).setOnClickListener {
            startActivity(Intent(this, ManageUserActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddApartment).setOnClickListener {
            val intent = Intent(this, AddEditApartmentActivity::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewApartments).setOnClickListener {
            startActivity(Intent(this, ManageApartmentsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btnNotification).setOnClickListener {
            Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { dialog, _ ->
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}