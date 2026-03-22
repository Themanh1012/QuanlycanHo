package com.example.room

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat // Đã Import đúng thư viện mới

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Nút Back
        findViewById<ImageView>(R.id.btnBackFromSettings).setOnClickListener { finish() }

        // ĐÃ SỬA TẠI ĐÂY: Dùng SwitchCompat thay vì Switch
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)

        // Khởi tạo bộ nhớ tạm để lưu cài đặt của người dùng
        val sharedPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Kiểm tra xem trước đó người dùng đang bật hay tắt Dark Mode để set trạng thái cho Switch
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        switchDarkMode.isChecked = isDarkMode

        // Bắt sự kiện khi người dùng gạt công tắc
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Bật Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("isDarkMode", true)
            } else {
                // Tắt Dark Mode (Về Light Mode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("isDarkMode", false)
            }
            editor.apply() // Lưu cấu hình
        }
    }
}