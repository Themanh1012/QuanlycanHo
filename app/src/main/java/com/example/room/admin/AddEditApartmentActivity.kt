package com.example.room.admin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.ImageUploadAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.io.FileOutputStream
import android.net.Uri

class AddEditApartmentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var apartmentId: Int = -1
    private var selectedImages = ArrayList<String>()
    
    private lateinit var edtTitle: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtArea: EditText
    private lateinit var edtDescription: EditText
    private lateinit var rvImages: RecyclerView
    private lateinit var layoutPlaceholder: View
    private lateinit var imageAdapter: ImageUploadAdapter
    private lateinit var spinnerStatus: Spinner

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val path = saveImageToInternalStorage(uri)
                if (path != null) {
                    selectedImages.add(path)
                }
            }
            updateImageUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)

        dbHelper = DatabaseHelper(this)
        apartmentId = intent.getIntExtra("apartment_id", -1)

        initViews()
        
        if (apartmentId != -1) {
            loadApartmentData()
            findViewById<TextView>(R.id.tvTitle).text = "Chỉnh sửa căn hộ"
            findViewById<Button>(R.id.btnDelete).visibility = View.VISIBLE
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSave).setOnClickListener { saveApartment() }
        findViewById<View>(R.id.cardImageUpload).setOnClickListener { pickImagesLauncher.launch("image/*") }
        findViewById<View>(R.id.btnChangeImage).setOnClickListener { pickImagesLauncher.launch("image/*") }
        
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            dbHelper.deleteApartment(apartmentId)
            Toast.makeText(this, "Đã xóa căn hộ", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun initViews() {
        edtTitle = findViewById(R.id.edtTitle)
        edtPrice = findViewById(R.id.edtPrice)
        edtAddress = findViewById(R.id.edtAddress)
        edtArea = findViewById(R.id.edtArea)
        edtDescription = findViewById(R.id.edtDescription)
        rvImages = findViewById(R.id.rvImages)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        spinnerStatus = findViewById(R.id.spinnerStatus)

        // Thiết lập Spinner trạng thái
        val statusOptions = arrayOf("Còn trống", "Đã thuê")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        imageAdapter = ImageUploadAdapter(selectedImages) { position: Int ->
            selectedImages.removeAt(position)
            updateImageUI()
        }
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter
    }

    private fun updateImageUI() {
        if (selectedImages.isEmpty()) {
            rvImages.visibility = View.GONE
            layoutPlaceholder.visibility = View.VISIBLE
        } else {
            rvImages.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun loadApartmentData() {
        val apartment = dbHelper.getApartmentById(apartmentId)
        apartment?.let {
            edtTitle.setText(it.title)
            edtPrice.setText(it.price.toLong().toString())
            edtAddress.setText(it.address)
            edtArea.setText(it.area.toString())
            edtDescription.setText(it.description)
            
            if (it.imagePaths.isNotEmpty()) {
                selectedImages.clear()
                selectedImages.addAll(it.imagePaths.split(",").filter { path -> path.isNotEmpty() })
                updateImageUI()
            }

            val statusOptions = arrayOf("Còn trống", "Đã thuê")
            val statusIndex = statusOptions.indexOf(it.status)
            if (statusIndex != -1) {
                spinnerStatus.setSelection(statusIndex)
            }
        }
    }

    private fun saveApartment() {
        val title = edtTitle.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val areaStr = edtArea.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (title.isEmpty() || priceStr.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ các trường có dấu *", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val area = areaStr.toDoubleOrNull() ?: 0.0
        val imagePaths = selectedImages.joinToString(",")

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val adminId = sharedPref.getInt("userId", 1)

        val existingApt = if (apartmentId != -1) dbHelper.getApartmentById(apartmentId) else null

        val apartment = Apartment(
            id = if (apartmentId != -1) apartmentId else 0,
            title = title,
            price = price,
            address = address,
            description = description,
            area = area,
            imagePaths = imagePaths,
            status = status,
            id_user = adminId,
            id_renter = if (status == "Còn trống") null else existingApt?.id_renter,
            badge = existingApt?.badge ?: ""
        )

        val result = if (apartmentId == -1) {
            dbHelper.insertApartment(apartment)
        } else {
            dbHelper.updateApartment(apartment).toLong()
        }

        if (result > 0) {
            Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
