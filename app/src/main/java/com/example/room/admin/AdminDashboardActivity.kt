package com.example.room.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val intent = Intent(this, ManageUserActivity::class.java)
        startActivity(intent)
        finish()
    }
}