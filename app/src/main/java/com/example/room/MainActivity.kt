package com.example.room

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.room.auth.LoginActivity
import com.example.room.fragment.HomeFragment
import com.example.room.fragment.ProfileFragment
import com.example.room.fragment.SavedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Kiểm tra đăng nhập
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            Log.d(TAG, "isLoggedIn: $isLoggedIn")

            if (!isLoggedIn) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }

            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            val container = findViewById<android.view.View>(R.id.fragment_container)
            if (container != null) {
                ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(0, systemBars.top, 0, 0)
                    insets
                }
            }

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }

            bottomNav.setOnItemSelectedListener { item ->
                var selectedFragment: Fragment? = null

                when (item.itemId) {
                    R.id.nav_home -> selectedFragment = HomeFragment()
                    R.id.nav_saved -> selectedFragment = SavedFragment()
                    R.id.nav_profile -> selectedFragment = ProfileFragment()
                }

                if (selectedFragment != null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit()
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}