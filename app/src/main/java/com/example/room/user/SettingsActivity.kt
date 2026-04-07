package com.example.room.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.room.R
import com.example.room.admin.ChangePasswordActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 1. Nút Back
        findViewById<ImageView>(R.id.btnBackFromSettings).setOnClickListener {
            finish()
        }

        // 2. Chuyển trang Đổi mật khẩu
        findViewById<LinearLayout>(R.id.btnChangePassword).setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            // Ở file LoginActivity, user id được lưu bằng key "userId"
            val userId = sharedPref.getInt("userId", -1)

            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("USER_ID", userId) // Truyền ID qua trang ChangePassword
            startActivity(intent)
        }

        // 3. Xử lý logic nút công tắc Dark Mode
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val appConfigPref = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)

        // Đọc trạng thái lưu trước đó
        val isDarkMode = appConfigPref.getBoolean("isDarkMode", false)
        switchDarkMode.isChecked = isDarkMode

        // Bắt sự kiện khi gạt công tắc
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            appConfigPref.edit().putBoolean("isDarkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}