package com.example.room.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.User
import com.google.android.material.button.MaterialButton

class AddEditUserActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etUsername: EditText
    private lateinit var etFullName: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var ivBack: ImageView

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_user)

        databaseHelper = DatabaseHelper(this)

        initViews()
        setupSpinner()
        checkEditMode()
        setupListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etFullName = findViewById(R.id.etFullName)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnSave = findViewById(R.id.btnSave)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun setupSpinner() {
        val roles = arrayOf("Khách hàng", "Quản trị viên")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun checkEditMode() {
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId != -1) {
            loadUserData()
        }
    }

    private fun loadUserData() {
        val user = databaseHelper.getUserById(userId)
        user?.let {
            etUsername.setText(it.username)
            etFullName.setText(it.fullName)
            spinnerRole.setSelection(if (it.role == 1) 1 else 0)
        }
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveUser() }
    }

    private fun saveUser() {
        val username = etUsername.text.toString().trim()
        val fullName = etFullName.text.toString().trim()
        val role = if (spinnerRole.selectedItemPosition == 1) 1 else 2

        if (username.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val success = if (userId == -1) {
            val newUser = User(
                id = 0,
                username = username,
                password = "123",
                fullName = fullName,
                role = role
            )
            databaseHelper.insertUser(newUser) > 0
        } else {
            databaseHelper.updateUserInfo(userId, fullName, role) > 0
        }

        if (success) {
            Toast.makeText(this, if (userId == -1) "Đã thêm người dùng" else "Đã cập nhật người dùng", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi lưu người dùng", Toast.LENGTH_SHORT).show()
        }
    }
}
