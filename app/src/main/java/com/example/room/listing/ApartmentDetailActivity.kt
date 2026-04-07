package com.example.room.listing

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.room.R
import android.widget.TextView
import com.example.room.database.DatabaseHelper
import java.text.DecimalFormat
import android.widget.Button
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.room.adapter.ImageSliderAdapter

class ApartmentDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_apartment_detail)

        dbHelper = DatabaseHelper(this)

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

        var apartment = dbHelper.getApartmentById(apartmentId)

        val initialApartment = apartment
        if (initialApartment == null) {
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
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnRent = findViewById<Button>(R.id.btnRent)
        val viewPagerImage = findViewById<ViewPager2>(R.id.viewPagerImage)
        val tvImageCount = findViewById<TextView>(R.id.tvImageCount)

        // ====== CHECK STATUS ======
        fun updateUI() {
            val current = apartment
            if (current?.status == "Đã thuê") {
                btnRent.text = "Đã thuê"
                btnRent.isEnabled = false
                val renter = current.id_renter?.let { dbHelper.getUserById(it) }
                tvStatus.text = if (renter != null) "Đã thuê bởi: ${renter.fullName}" else "Đã thuê"
            } else {
                tvStatus.text = current?.status
                btnRent.text = "Thuê ngay"
                btnRent.isEnabled = true
            }
        }
        
        updateUI()

        // ====== BUTTON LƯU ======
        val isSaved = dbHelper.isApartmentSaved(apartmentId, userId)
        btnSave.text = if (isSaved) "Đã lưu" else "Yêu thích"

        btnSave.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.isApartmentSaved(apartmentId, userId)) {
                dbHelper.unsaveApartment(apartmentId, userId)
                btnSave.text = "Yêu thích"
            } else {
                dbHelper.saveApartment(apartmentId, userId)
                btnSave.text = "Đã lưu"
            }
        }

        // ====== SET DATA ======
        tvTitle.text = initialApartment.title
        tvAddress.text = initialApartment.address
        val formatter = DecimalFormat("#,###")
        tvPrice.text = formatter.format(initialApartment.price) + " VND/tháng"
        tvArea.text = "${initialApartment.area} m²"
        tvDescription.text = if (initialApartment.description.isNotEmpty()) initialApartment.description else "Không có mô tả"

        // ====== BUTTON THUÊ (QUAN TRỌNG) ======
        btnRent.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập để thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // GỌI HÀM THUÊ TRỰC TIẾP TRONG DATABASE
            val result = dbHelper.rentApartment(apartmentId, userId)

            if (result > 0) {
                // Tải lại dữ liệu mới nhất từ DB để cập nhật id_renter
                apartment = dbHelper.getApartmentById(apartmentId)
                updateUI()
                Toast.makeText(this, "Thuê thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi khi thuê, thử lại sau!", Toast.LENGTH_SHORT).show()
            }
        }

        // ====== SLIDER IMAGE ======
        val paths = initialApartment.imagePaths.split(",").filter { it.isNotEmpty() }
        if (paths.isNotEmpty()) {
            viewPagerImage.adapter = ImageSliderAdapter(paths)
            tvImageCount.text = "1/${paths.size}"
            viewPagerImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tvImageCount.text = "${position + 1}/${paths.size}"
                }
            })
        }
    }
}
