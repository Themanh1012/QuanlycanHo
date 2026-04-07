package com.example.room.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.RentalManageAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class ManageRentalsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RentalManageAdapter
    private lateinit var etSearch: EditText
    private var rentalList = ArrayList<Apartment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_rentals)

        dbHelper = DatabaseHelper(this)

        initViews()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvRentals)
        etSearch = findViewById(R.id.etSearchRental)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        adapter = RentalManageAdapter(rentalList, dbHelper) { apartment ->
            showEndRentalDialog(apartment)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        // Lấy tất cả căn hộ từ database
        val allApts = dbHelper.getAllApartments()
        rentalList.clear()
        
        // CẢI TIẾN: Lọc linh hoạt hơn (không phân biệt hoa thường, cắt khoảng trắng)
        rentalList.addAll(allApts.filter { 
            it.status.trim().equals("Đã thuê", ignoreCase = true) 
        })
        
        adapter.updateData(rentalList)
        
        if (rentalList.isEmpty()) {
            Toast.makeText(this, "Hiện chưa có căn hộ nào được thuê", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filter(text: String) {
        if (text.isEmpty()) {
            adapter.updateData(rentalList)
            return
        }
        val filteredList = rentalList.filter {
            val renter = it.id_renter?.let { id -> dbHelper.getUserById(id) }
            it.title.contains(text, ignoreCase = true) ||
                    (renter?.fullName?.contains(text, ignoreCase = true) ?: false) ||
                    it.address.contains(text, ignoreCase = true)
        }
        adapter.updateData(filteredList)
    }

    private fun showEndRentalDialog(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle("Kết thúc hợp đồng")
            .setMessage("Xác nhận khách đã trả phòng cho căn hộ \"${apartment.title}\"?\nTrạng thái sẽ chuyển về \"Còn trống\".")
            .setPositiveButton("Xác nhận") { _, _ ->
                val cv = android.content.ContentValues().apply {
                    put("status", "Còn trống")
                    put("id_renter", null as Int?)
                }
                val result = dbHelper.writableDatabase.update("apartments", cv, "id=?", arrayOf(apartment.id.toString()))
                if (result > 0) {
                    Toast.makeText(this, "Đã cập nhật trạng thái trống", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
