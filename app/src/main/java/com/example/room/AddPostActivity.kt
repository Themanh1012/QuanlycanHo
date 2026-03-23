package com.example.room

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class AddPostActivity : AppCompatActivity() {


    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        dbHelper = DatabaseHelper(this)

        // Ánh xạ các nút và ô nhập liệu từ XML dựa vào các ID vừa thêm
        val btnBack = findViewById<ImageView>(R.id.btnBackFromAddPost)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitPost)
        val edtTitle = findViewById<EditText>(R.id.edtAddPostTitle)
        val edtPrice = findViewById<EditText>(R.id.edtAddPostPrice)
        val edtAddress = findViewById<EditText>(R.id.edtAddPostAddress)


        btnBack.setOnClickListener {
            finish()
        }


        btnSubmit.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val priceStr = edtPrice.text.toString().trim()
            val address = edtAddress.text.toString().trim()


            if (title.isEmpty() || priceStr.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin căn hộ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val price = priceStr.toDoubleOrNull() ?: 0.0


            val newApartment = Apartment(
                title = title,
                price = price,
                address = address,
                id_user = 1
            )


            val result = dbHelper.insertApartment(newApartment)

            if (result != -1L) {
                Toast.makeText(this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Đăng tin thất bại, hãy thử lại!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}