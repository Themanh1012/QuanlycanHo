package com.example.room.user

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.ApartmentUserAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.listing.ApartmentDetailActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentUserAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Back
        findViewById<ImageView>(R.id.btnBackFromHistory).setOnClickListener { finish() }

        // DB
        dbHelper = DatabaseHelper(this)

        // RecyclerView
        recyclerView = findViewById(R.id.rvHistory)

        val list = dbHelper.getHistoryApartments()

        adapter = ApartmentUserAdapter(list) { apartment ->
            val intent = Intent(this, ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apartment.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        val list = dbHelper.getHistoryApartments()
        adapter = ApartmentUserAdapter(list) { apartment ->
            val intent = Intent(this, ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apartment.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}