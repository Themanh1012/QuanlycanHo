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

        // Lấy userId từ SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        val btnBack = findViewById<CardView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val apartmentId = intent.getIntExtra("apartment_id", -1)

        if (apartmentId == -1) {
            Toast.makeText(this, "Lỗi: không có ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val apartment = dbHelper.getApartmentById(apartmentId)

        if (apartment == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show()
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
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnRent = findViewById<Button>(R.id.btnRent)

        // ====== CHECK STATUS ======
        if (apartment.status == "Đã thuê") {
            btnRent.text = "Đã thuê"
            btnRent.isEnabled = false
            
            // Hiển thị thêm thông tin người thuê nếu là Admin (tùy chọn)
            val renter = apartment.id_renter?.let { dbHelper.getUserById(it) }
            renter?.let {
                tvStatus.text = "Đã thuê bởi: ${it.fullName}"
            }
        }

        // ====== BUTTON LƯU ======
        val isSaved = dbHelper.isApartmentSaved(apartmentId, userId)
        btnSave.text = if (isSaved) "Đã lưu" else "Lưu căn hộ"

        btnSave.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập để lưu căn hộ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.isApartmentSaved(apartmentId, userId)) {
                dbHelper.unsaveApartment(apartmentId, userId)
                btnSave.text = "Lưu căn hộ"
            } else {
                dbHelper.saveApartment(apartmentId, userId)
                btnSave.text = "Đã lưu"
            }
        }

        // ====== SET DATA ======
        tvTitle.text = apartment.title
        tvAddress.text = apartment.address
        val formatter = DecimalFormat("#,###")
        tvPrice.text = formatter.format(apartment.price) + " VND/tháng"
        tvArea.text = "${apartment.area} m²"
        if (apartment.status != "Đã thuê") tvStatus.text = apartment.status
        tvDescription.text = if (apartment.description.isNotEmpty()) apartment.description else "Không có mô tả"

        // ====== BUTTON THUÊ ======
        btnRent.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập để thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (apartment.status == "Đã thuê") {
                Toast.makeText(this, "Căn hộ đã được thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // GỌI HÀM THUÊ MỚI TRONG DATABASE HELPER
            val result = dbHelper.rentApartment(apartmentId, userId)

            if (result > 0) {
                tvStatus.text = "Đã thuê"
                btnRent.text = "Đã thuê"
                btnRent.isEnabled = false
                Toast.makeText(this, "Thuê thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi khi thuê, thử lại sau!", Toast.LENGTH_SHORT).show()
            }
        }

        // ====== LOAD IMAGE ======
        if (apartment.imagePath.isNotEmpty()) {
            if (!apartment.imagePath.contains("/") && !apartment.imagePath.contains("\\")) {
                val resId = resources.getIdentifier(apartment.imagePath, "drawable", packageName)
                if (resId != 0) imgApartment.setImageResource(resId)
            } else {
                val file = java.io.File(apartment.imagePath)
                if (file.exists()) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    imgApartment.setImageBitmap(bitmap)
                }
            }
        }
    }
}
