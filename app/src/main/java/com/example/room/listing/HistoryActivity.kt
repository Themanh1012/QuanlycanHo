package com.example.room.listing

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.ApartmentVerticalAdapter
import com.example.room.database.DatabaseHelper
import android.widget.Toast
import com.example.room.model.Apartment
import android.widget.Button

class HistoryActivity : AppCompatActivity() {
    private lateinit var adapter: ApartmentVerticalAdapter
    private var list = ArrayList<Apartment>()

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        tvEmpty = findViewById(R.id.tvEmptyHistory)
        dbHelper = DatabaseHelper(this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var userId = sharedPref.getInt("userId", -1)

        if (userId <= 0) {
            userId = 1   // 👉 fix tạm
        }

        list = dbHelper.getViewHistory(userId)

        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        adapter = ApartmentVerticalAdapter(list) { }

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        val btnClear = findViewById<Button>(R.id.btnClearHistory)

        btnClear.setOnClickListener {
            dbHelper.clearViewHistory(userId)

            list.clear()
            adapter.notifyDataSetChanged()

            tvEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE

            Toast.makeText(this, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show()
        }

        // nút back
        findViewById<ImageView>(R.id.btnBackFromHistory).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var userId = sharedPref.getInt("userId", -1)

        if (userId <= 0) {
            userId = 1   //  fix tạm
        }
        android.util.Log.d("DEBUG", "userId = $userId")

        list.clear()
        list.addAll(dbHelper.getViewHistory(userId))

        adapter.notifyDataSetChanged()

        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }
    }
}