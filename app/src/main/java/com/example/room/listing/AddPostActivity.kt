package com.example.room.listing

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class AddPostActivity : AppCompatActivity() {

    private val TAG = "AddPostActivity"
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_add_post)

            dbHelper = DatabaseHelper(this)

            val btnBack = findViewById<ImageView>(R.id.btnBackFromAddPost)
            val btnSubmit = findViewById<Button>(R.id.btnSubmitPost)
            val edtTitle = findViewById<EditText>(R.id.edtAddPostTitle)
            val edtPrice = findViewById<EditText>(R.id.edtAddPostPrice)
            val edtAddress = findViewById<EditText>(R.id.edtAddPostAddress)

            btnBack?.setOnClickListener {
                finish()
            }

            btnSubmit?.setOnClickListener {
                try {
                    val title = edtTitle?.text.toString().trim()
                    val priceStr = edtPrice?.text.toString().trim()
                    val address = edtAddress?.text.toString().trim()

                    if (title.isEmpty() || priceStr.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin căn hộ!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val price = priceStr.toDoubleOrNull() ?: 0.0

                    // Lấy user_id từ SharedPreferences
                    val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val userId = sharedPref.getInt("USER_ID", 0)

                    val newApartment = Apartment(
                        title = title,
                        price = price,
                        address = address,
                        id_user = userId
                    )

                    val result = dbHelper.insertApartment(newApartment)

                    if (result != -1L) {
                        Toast.makeText(this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Đăng tin thất bại!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Submit error", e)
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            finish()
        }
    }
}