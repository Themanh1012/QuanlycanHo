package com.example.room.listing

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.adapter.ApartmentUserAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class ApartmentListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentUserAdapter
    private var list = ArrayList<Apartment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartment_list)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.rvApartmentsUser)

        loadData()

        adapter = ApartmentUserAdapter(list) { apartment ->

        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btnBackFromAptList).setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        list.clear()
        list.addAll(dbHelper.getAllApartments()) // 🔥 CHÍNH LÀ DÒNG NỐI ADMIN → USER
    }
}