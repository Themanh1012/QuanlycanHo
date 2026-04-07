package com.example.room.admin

import android.content.Intent
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
import com.example.room.adapter.ApartmentAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import com.example.room.listing.ApartmentDetailActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageApartmentsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentAdapter
    private lateinit var edtSearch: EditText
    private lateinit var ivBack: ImageView
    private var apartmentList = ArrayList<Apartment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_apartments)

        dbHelper = DatabaseHelper(this)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvApartments)
        edtSearch = findViewById(R.id.etSearchApartment)
        ivBack = findViewById(R.id.ivBack)

        adapter = ApartmentAdapter(
            apartmentList,
            onItemClick = { apartment ->
                val intent = Intent(this, ApartmentDetailActivity::class.java)
                intent.putExtra("apartment_id", apartment.id)
                startActivity(intent)
            },
            onEditClick = { apartment ->
                val intent = Intent(this, AddEditApartmentActivity::class.java)
                intent.putExtra("mode", "edit")
                intent.putExtra("apartment_id", apartment.id)
                startActivity(intent)
            },
            onDeleteClick = { apartment ->
                showDeleteConfirmDialog(apartment)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadData() {
        apartmentList.clear()
        apartmentList.addAll(dbHelper.getAllApartments())
        adapter.updateList(apartmentList)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        // Xử lý nút thêm mới (+)
        findViewById<FloatingActionButton>(R.id.fabAddApartment).setOnClickListener {
            val intent = Intent(this, AddEditApartmentActivity::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterData(query: String) {
        val filtered = if (query.isEmpty()) {
            apartmentList
        } else {
            apartmentList.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.address.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filtered)
    }

    private fun showDeleteConfirmDialog(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa căn hộ \"${apartment.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                if (dbHelper.deleteApartment(apartment.id) > 0) {
                    Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
