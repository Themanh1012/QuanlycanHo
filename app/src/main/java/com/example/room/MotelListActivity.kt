package com.example.room

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MotelListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motel_list)
        findViewById<ImageView>(R.id.btnBackFromMotel).setOnClickListener { finish() }
    }
}