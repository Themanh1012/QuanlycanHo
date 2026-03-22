package com.example.room.listing

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R

class MotelListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motel_list)
        findViewById<ImageView>(R.id.btnBackFromMotel).setOnClickListener { finish() }
    }
}