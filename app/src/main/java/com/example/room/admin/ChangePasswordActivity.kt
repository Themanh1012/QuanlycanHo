package com.example.room.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.google.android.material.button.MaterialButton

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var ivBack: ImageView

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        databaseHelper = DatabaseHelper(this)

        userId = intent.getIntExtra("USER_ID", -1)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = etOldPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 3) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 3 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (!databaseHelper.checkPassword(userId, oldPassword)) {
            Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show()
            return
        }

        val success = databaseHelper.changePassword(userId, newPassword) > 0

        if (success) {
            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi đổi mật khẩu", Toast.LENGTH_SHORT).show()
        }
    }
}