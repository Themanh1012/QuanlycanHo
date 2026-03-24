package com.example.room.listing

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.room.R
import android.widget.ImageView
import android.widget.TextView
import com.example.room.database.DatabaseHelper
import java.text.DecimalFormat
import android.widget.Button
import android.widget.Toast
class ApartmentDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_apartment_detail)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<CardView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // ====== NHẬN ID ======
        val apartmentId = intent.getIntExtra("apartment_id", -1)

        if (apartmentId == -1) {
            android.widget.Toast.makeText(this, "Lỗi: không có ID", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val apartment = dbHelper.getApartmentById(apartmentId)

        if (apartment == null) {
            android.widget.Toast.makeText(this, "Không tìm thấy dữ liệu!", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ====== FIND VIEW ======
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvAddress = findViewById<TextView>(R.id.tvAddress)
        val tvArea = findViewById<TextView>(R.id.tvArea)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val imgApartment = findViewById<ImageView>(R.id.imgApartment)

        // ====== BUTTON LƯU ======
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            dbHelper.insertSavedApartment(apartmentId)
            Toast.makeText(this, "Đã lưu căn hộ", Toast.LENGTH_SHORT).show()
        }

        // ====== SET DATA ======
        tvTitle.text = apartment.title
        tvAddress.text = apartment.address

        val formatter = DecimalFormat("#,###")
        tvPrice.text = formatter.format(apartment.price) + " VND/tháng"

        tvArea.text = "${apartment.area} m²"
        tvStatus.text = apartment.status

        tvDescription.text =
            if (apartment.description.isNotEmpty())
                apartment.description
            else "Không có mô tả"

        // ====== BUTTON THUÊ ======
        val btnRent = findViewById<Button>(R.id.btnRent)

// nếu đã thuê thì disable luôn
        if (apartment.status == "Đã thuê") {
            btnRent.text = "Đã thuê"
            btnRent.isEnabled = false
        }

        btnRent.setOnClickListener {
            if (apartment.status == "Đã thuê") {
                Toast.makeText(this, "Căn hộ đã được thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // update DB
            val updatedApartment = apartment.copy(status = "Đã thuê")
            dbHelper.updateApartment(updatedApartment)

            // update UI
            tvStatus.text = "Đã thuê"
            btnRent.text = "Đã thuê"
            btnRent.isEnabled = false

            Toast.makeText(this, "Thuê thành công!", Toast.LENGTH_SHORT).show()
        }
        // ====== LOAD IMAGE ======
        if (apartment.imagePath.isNotEmpty()) {
            val file = java.io.File(apartment.imagePath)
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                imgApartment.setImageBitmap(bitmap)
            }
        }
    }
}