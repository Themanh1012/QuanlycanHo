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
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
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
        val userRole = sharedPref.getInt("role", 2)

        val apartmentId = intent.getIntExtra("apartment_id", -1)

        if (apartmentId != -1 && userId != 0) {
            dbHelper.addViewHistory(apartmentId, userId)
        }

        val btnBack = findViewById<CardView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        if (apartmentId == -1) {
            Toast.makeText(this, "Lỗi: không có ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        var apartment = dbHelper.getApartmentById(apartmentId)

        if (apartment == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvAddress = findViewById<TextView>(R.id.tvAddress)
        val tvArea = findViewById<TextView>(R.id.tvArea)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val btnRent = findViewById<Button>(R.id.btnRent)
        val viewPagerImage = findViewById<ViewPager2>(R.id.viewPagerImage)
        val tvImageCount = findViewById<TextView>(R.id.tvImageCount)
        
        val btnSaveIcon = findViewById<CardView>(R.id.btnSaveIcon)
        val imgSave = findViewById<ImageView>(R.id.imgSave)
        
        val layoutRenterInfo = findViewById<LinearLayout>(R.id.layoutRenterInfo)
        val tvRenterInfo = findViewById<TextView>(R.id.tvRenterInfo)

        fun updateUI() {
            val current = apartment ?: return
            
            if (current.status.contains("Đã thuê", ignoreCase = true)) {
                tvStatus.text = "Đã thuê"
                tvStatus.setTextColor(android.graphics.Color.RED)
                
                // Nếu là Admin thì nút hiển thị "Hủy cho thuê", nếu là Khách thì hiển thị "Phòng đã có chủ"
                if (userRole == 1) {
                    btnRent.text = "Hủy cho thuê"
                    btnRent.isEnabled = true
                    btnRent.alpha = 1.0f
                    btnRent.setBackgroundColor(android.graphics.Color.GRAY)
                } else {
                    btnRent.text = "PHÒNG ĐÃ CÓ CHỦ"
                    btnRent.isEnabled = false
                    btnRent.alpha = 0.5f
                    btnRent.setBackgroundColor(android.graphics.Color.parseColor("#F43F5E"))
                }
                
                val renter = current.id_renter?.let { dbHelper.getUserById(it) }
                if (renter != null) {
                    layoutRenterInfo.visibility = View.VISIBLE
                    tvRenterInfo.text = "Khách thuê: ${renter.fullName}\nLiên hệ: ${renter.username}"
                } else {
                    layoutRenterInfo.visibility = View.GONE
                }
            } else {
                tvStatus.text = "Còn trống"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
                layoutRenterInfo.visibility = View.GONE
                btnRent.text = "Thuê ngay"
                btnRent.isEnabled = true
                btnRent.alpha = 1.0f
                btnRent.setBackgroundColor(android.graphics.Color.parseColor("#F43F5E"))
            }

            val isSaved = dbHelper.isApartmentSaved(apartmentId, userId)
            if (isSaved) {
                imgSave.setImageResource(android.R.drawable.btn_star_big_on)
                imgSave.setColorFilter(android.graphics.Color.parseColor("#F43F5E"))
            } else {
                imgSave.setImageResource(android.R.drawable.btn_star_big_off)
                imgSave.setColorFilter(android.graphics.Color.parseColor("#64748B"))
            }
        }
        
        updateUI()

        tvTitle.text = apartment!!.title
        tvAddress.text = apartment!!.address
        val formatter = DecimalFormat("#,###")
        tvPrice.text = formatter.format(apartment!!.price) + " VND/tháng"
        tvArea.text = "${apartment!!.area} m²"
        tvDescription.text = if (apartment!!.description.isNotEmpty()) apartment!!.description else "Không có mô tả chi tiết cho căn hộ này."

        btnSaveIcon.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.isApartmentSaved(apartmentId, userId)) {
                dbHelper.unsaveApartment(apartmentId, userId)
                Toast.makeText(this, "Đã bỏ lưu", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.saveApartment(apartmentId, userId)
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }

        btnRent.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập để thực hiện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Xử lý nút khi trạng thái đã thuê
            if (apartment!!.status.contains("Đã thuê", ignoreCase = true)) {
                if (userRole == 1) {
                    // Admin Hủy thuê
                    AlertDialog.Builder(this)
                        .setTitle("Hủy cho thuê")
                        .setMessage("Bạn có chắc chắn muốn hủy trạng thái cho thuê và đuổi khách ra?")
                        .setPositiveButton("Xác nhận") { _, _ ->
                            val result = dbHelper.cancelRental(apartmentId)
                            if (result > 0) {
                                apartment = dbHelper.getApartmentById(apartmentId)
                                updateUI()
                                Toast.makeText(this, "Đã hủy cho thuê thành công!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                }
                return@setOnClickListener
            }

            // Logic Thuê ngay dành cho Khách hàng
            if (userRole == 1) {
                Toast.makeText(this, "Admin không thể thực hiện thuê nhà!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.rentApartment(apartmentId, userId)
            if (result > 0) {
                apartment = dbHelper.getApartmentById(apartmentId)
                updateUI()
                Toast.makeText(this, "Thuê thành công!", Toast.LENGTH_LONG).show()
            }
        }

        val paths = apartment!!.imagePaths.split(",").filter { it.isNotEmpty() }
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
