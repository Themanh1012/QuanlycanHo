package com.example.room

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.room.auth.LoginActivity
import com.example.room.fragment.HomeFragment
import com.example.room.fragment.ProfileFragment
import com.example.room.fragment.SavedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var btnGlobalScrollTop: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            Log.d(TAG, "isLoggedIn: \$isLoggedIn")

            if (!isLoggedIn) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }

            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            val container = findViewById<View>(R.id.fragment_container)
            if (container != null) {
                ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(0, systemBars.top, 0, 0)
                    insets
                }
            }

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            // Ánh xạ nút Lên đầu trang
            btnGlobalScrollTop = findViewById(R.id.btnGlobalScrollTop)

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

            // ==========================================
            // LOGIC NÚT LÊN ĐẦU TRANG MƯỢT & CHỐNG LỖI
            // ==========================================
            val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)

            fragmentContainer?.viewTreeObserver?.addOnScrollChangedListener {
                // Dò tìm View nào đang hiện trên màn hình
                val currentScroll = findViewById<View>(R.id.mainScrollView)
                    ?: findViewById<View>(R.id.rvSaved)
                    ?: findViewById<View>(R.id.profileScrollView)

                // canScrollVertically(-1) trả về true nghĩa là màn hình đã bị cuộn xuống (có thể cuộn lên)
                if (currentScroll != null && currentScroll.canScrollVertically(-1)) {
                    btnGlobalScrollTop?.visibility = View.VISIBLE
                } else {
                    btnGlobalScrollTop?.visibility = View.GONE
                }
            }

            // Xử lý khi bấm nút sẽ kéo các danh sách về Toạ độ 0 (Về đỉnh)
            btnGlobalScrollTop?.setOnClickListener {
                findViewById<ScrollView>(R.id.mainScrollView)?.smoothScrollTo(0, 0)
                findViewById<RecyclerView>(R.id.rvSaved)?.smoothScrollToPosition(0)
                findViewById<ScrollView>(R.id.profileScrollView)?.smoothScrollTo(0, 0)
            }
            // ==========================================

        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}