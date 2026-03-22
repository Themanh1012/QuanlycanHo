package com.example.room.listing

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R

class ApartmentListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartment_list)

        findViewById<ImageView>(R.id.btnBackFromAptList).setOnClickListener { finish() }
    }
}