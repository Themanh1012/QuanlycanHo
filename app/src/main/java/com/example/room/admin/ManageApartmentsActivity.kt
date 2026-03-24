package com.example.room.admin

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

    private val TAG = "ManageApartments"
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvApartments: RecyclerView
    private lateinit var etSearchApartment: EditText
    private lateinit var fabAddApartment: FloatingActionButton
    private lateinit var ivBack: ImageView
    private lateinit var btnExit: Button
    private lateinit var adapter: ApartmentAdapter

    private var apartmentList: ArrayList<Apartment> = ArrayList()
    private var filteredList: ArrayList<Apartment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_manage_apartments)

            dbHelper = DatabaseHelper(this)

            // Init views - phải đúng với ID trong layout
            rvApartments = findViewById(R.id.rvApartments)
            etSearchApartment = findViewById(R.id.etSearchApartment)
            fabAddApartment = findViewById(R.id.fabAddApartment)
            ivBack = findViewById(R.id.ivBack)
            btnExit = findViewById(R.id.btnExit)

            // Click listeners
            ivBack.setOnClickListener { finish() }

            btnExit.setOnClickListener { finish() }

            fabAddApartment.setOnClickListener {
                val intent = Intent(this, AddEditApartmentActivity::class.java)
                intent.putExtra("mode", "add")
                startActivity(intent)
            }

            setupRecyclerView()
            setupSearch()
            loadData()

        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadData()
        } catch (e: Exception) {
            Log.e(TAG, "onResume error", e)
        }
    }

    private fun setupRecyclerView() {
        adapter = ApartmentAdapter(
            apartments = filteredList,
            onItemClick = { apartment -> showDetailDialog(apartment) },
            onEditClick = { apartment ->
                val intent = Intent(this, AddEditApartmentActivity::class.java)
                intent.putExtra("mode", "edit")
                intent.putExtra("apartment_id", apartment.id)
                startActivity(intent)
            },
            onDeleteClick = { apartment -> showDeleteDialog(apartment) }
        )

        rvApartments.layoutManager = LinearLayoutManager(this)
        rvApartments.adapter = adapter
    }

    private fun loadData() {
        apartmentList = dbHelper.getAllApartments()
        filteredList.clear()
        filteredList.addAll(apartmentList)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        etSearchApartment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApartments(s.toString())
            }
        })
    }

    private fun filterApartments(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(apartmentList)
        } else {
            for (apartment in apartmentList) {
                if (apartment.title.lowercase().contains(query.lowercase()) ||
                    apartment.address.lowercase().contains(query.lowercase())) {
                    filteredList.add(apartment)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(apartment: Apartment) {
        AlertDialog.Builder(this)
            .setTitle("Xóa căn hộ")
            .setMessage("Bạn có chắc muốn xóa \"${apartment.title}\"?")
            .setPositiveButton("Xóa") { dialog, _ ->
                dbHelper.deleteApartment(apartment.id)
                loadData()
                Toast.makeText(this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show()
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
            try {
                val imgFile = File(apartment.imagePath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    if (bitmap != null) {
                        imgApartment.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Load image error", e)
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Đóng", null)
            .create()
            .show()
    }
}