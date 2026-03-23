package com.example.room.admin

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.DecimalFormat

class ManageApartmentsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentAdapter
    private lateinit var edtSearch: EditText
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvApartmentCount: TextView

    private var apartmentList: ArrayList<Apartment> = ArrayList()
    private var filteredList: ArrayList<Apartment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_apartments)

        dbHelper = DatabaseHelper(this)

        initViews()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        loadData()

        // Handle search query from AdminDashboardActivity
        val searchQuery = intent.getStringExtra("search_query")
        if (!searchQuery.isNullOrEmpty()) {
            edtSearch.setText(searchQuery)
            filterApartments(searchQuery)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewApartments)
        edtSearch = findViewById(R.id.edtSearch)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        tvApartmentCount = findViewById(R.id.tvApartmentCount)
    }

    private fun setupRecyclerView() {
        adapter = ApartmentAdapter(
            filteredList,
            onEditClick = { apartment -> openEditActivity(apartment) },
            onDeleteClick = { apartment -> showDeleteDialog(apartment) },
            onDetailClick = { apartment -> showDetailDialog(apartment) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApartments(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            val intent = Intent(this, AddEditApartmentActivity::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }
    }

    private fun loadData() {
        apartmentList = dbHelper.getAllApartments()
        filteredList.clear()
        filteredList.addAll(apartmentList)
        adapter.notifyDataSetChanged()

        tvApartmentCount.text = apartmentList.size.toString()

        if (filteredList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun filterApartments(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(apartmentList)
        } else {
            val searchQuery = query.lowercase().trim()
            for (apartment in apartmentList) {
                if (apartment.title.lowercase().contains(searchQuery) ||
                    apartment.address.lowercase().contains(searchQuery)) {
                    filteredList.add(apartment)
                }
            }
        }
        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun openEditActivity(apartment: Apartment) {
        val intent = Intent(this, AddEditApartmentActivity::class.java)
        intent.putExtra("mode", "edit")
        intent.putExtra("apartmentId", apartment.id)
        startActivity(intent)
    }

    private fun showDeleteDialog(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa căn hộ \"${apartment.title}\"?")
            .setPositiveButton("Xóa") { dialog, _ ->
                val result = dbHelper.deleteApartment(apartment.id)
                if (result > 0) {
                    Toast.makeText(this, "Đã xóa căn hộ!", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDetailDialog(apartment: Apartment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_apartment_detail, null)

        val imgApartment = dialogView.findViewById<ImageView>(R.id.imgApartment)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvAddress = dialogView.findViewById<TextView>(R.id.tvAddress)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tvPrice)
        val tvArea = dialogView.findViewById<TextView>(R.id.tvArea)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatus)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)

        tvTitle.text = apartment.title
        tvAddress.text = apartment.address

        val formatter = DecimalFormat("#,###")
        tvPrice.text = formatter.format(apartment.price) + " VND/tháng"
        tvArea.text = "${apartment.area} m²"
        tvStatus.text = apartment.status
        tvDescription.text = if (apartment.description.isNotEmpty()) apartment.description else "Không có mô tả"

        if (apartment.imagePath.isNotEmpty()) {
            val imgFile = File(apartment.imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imgApartment.setImageBitmap(bitmap)
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Đóng", null)
            .create()
            .show()
    }
}