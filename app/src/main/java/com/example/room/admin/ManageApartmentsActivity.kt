package com.example.quanlycanho.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlycanho.R
import com.example.quanlycanho.adapter.ApartmentAdapter
import com.example.quanlycanho.database.DatabaseHelper
import com.example.quanlycanho.model.Apartment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageApartmentsActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentAdapter
    private lateinit var etSearch: EditText
    private lateinit var ivBack: ImageView
    private lateinit var fabAdd: FloatingActionButton

    private var apartmentList = ArrayList<Apartment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_apartments)

        databaseHelper = DatabaseHelper(this)

        initViews()
        loadApartments()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        etSearch = findViewById(R.id.etSearch)
        ivBack = findViewById(R.id.ivBack)
        fabAdd = findViewById(R.id.fabAdd)

        adapter = ApartmentAdapter(
            apartmentList,
            onEditClick = { apartment -> editApartment(apartment) },
            onDeleteClick = { apartment -> deleteApartment(apartment) },
            onItemClick = { apartment -> viewApartmentDetails(apartment) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadApartments() {
        apartmentList.clear()
        apartmentList.addAll(databaseHelper.getAllApartments())
        adapter.updateList(apartmentList)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditApartmentActivity::class.java))
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApartments(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterApartments(query: String) {
        val filteredList = if (query.isEmpty()) {
            apartmentList
        } else {
            ArrayList(apartmentList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            })
        }
        adapter.updateList(filteredList)
    }

    private fun editApartment(apartment: Apartment) {
        val intent = Intent(this, AddEditApartmentActivity::class.java)
        intent.putExtra("APARTMENT_ID", apartment.id)
        startActivity(intent)
    }

    private fun deleteApartment(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle("Xóa căn hộ")
            .setMessage("Bạn có chắc muốn xóa căn hộ \"${apartment.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                databaseHelper.deleteApartment(apartment.id)
                loadApartments()
                Toast.makeText(this, "Đã xóa căn hộ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun viewApartmentDetails(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle(apartment.name)
            .setMessage("""
                Địa chỉ: ${apartment.address}
                Giá: ${String.format("%,d", apartment.price)} VNĐ/tháng
                Diện tích: ${apartment.area} m²
                Phòng ngủ: ${apartment.bedrooms}
                Phòng tắm: ${apartment.bathrooms}
                Trạng thái: ${if (apartment.status == "available") "Còn trống" else "Đã thuê"}
                Mô tả: ${apartment.description}
            """.trimIndent())
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadApartments()
    }
}