package com.example.quanlycanho.admin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlycanho.R
import com.example.quanlycanho.database.DatabaseHelper
import com.example.quanlycanho.model.Apartment
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddEditApartmentActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var ivApartmentImage: ImageView
    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPrice: EditText
    private lateinit var etArea: EditText
    private lateinit var etBedrooms: EditText
    private lateinit var etBathrooms: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var ivBack: ImageView

    private var apartmentId: Long = -1
    private var selectedImagePath: String = ""
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)

        databaseHelper = DatabaseHelper(this)

        initViews()
        setupSpinner()
        checkEditMode()
        setupListeners()
    }

    private fun initViews() {
        ivApartmentImage = findViewById(R.id.ivApartmentImage)
        etName = findViewById(R.id.etName)
        etAddress = findViewById(R.id.etAddress)
        etPrice = findViewById(R.id.etPrice)
        etArea = findViewById(R.id.etArea)
        etBedrooms = findViewById(R.id.etBedrooms)
        etBathrooms = findViewById(R.id.etBathrooms)
        etDescription = findViewById(R.id.etDescription)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSave = findViewById(R.id.btnSave)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun setupSpinner() {
        val statusOptions = arrayOf("available", "occupied")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }

    private fun checkEditMode() {
        apartmentId = intent.getLongExtra("APARTMENT_ID", -1)
        if (apartmentId != -1L) {
            title = "Chỉnh sửa căn hộ"
            loadApartmentData()
        } else {
            title = "Thêm căn hộ mới"
        }
    }

    private fun loadApartmentData() {
        val apartment = databaseHelper.getApartmentById(apartmentId)
        apartment?.let {
            etName.setText(it.name)
            etAddress.setText(it.address)
            etPrice.setText(it.price.toString())
            etArea.setText(it.area.toString())
            etBedrooms.setText(it.bedrooms.toString())
            etBathrooms.setText(it.bathrooms.toString())
            etDescription.setText(it.description)
            spinnerStatus.setSelection(if (it.status == "available") 0 else 1)
            selectedImagePath = it.imagePath

            if (it.imagePath.isNotEmpty()) {
                val imgFile = File(it.imagePath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(it.imagePath)
                    ivApartmentImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        ivApartmentImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSave.setOnClickListener {
            saveApartment()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let {
                ivApartmentImage.setImageURI(it)
                selectedImagePath = saveImageToInternalStorage(it)
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val fileName = "apartment_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }

    private fun saveApartment() {
        val name = etName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val areaStr = etArea.text.toString().trim()
        val bedroomsStr = etBedrooms.text.toString().trim()
        val bathroomsStr = etBathrooms.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (name.isEmpty() || address.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val area = areaStr.toDoubleOrNull() ?: 0.0
        val bedrooms = bedroomsStr.toIntOrNull() ?: 0
        val bathrooms = bathroomsStr.toIntOrNull() ?: 0

        val apartment = Apartment(
            id = apartmentId,
            name = name,
            address = address,
            price = price,
            area = area,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            description = description,
            status = status,
            imagePath = selectedImagePath
        )

        val success = if (apartmentId == -1L) {
            databaseHelper.addApartment(apartment) > 0
        } else {
            databaseHelper.updateApartment(apartment)
        }

        if (success) {
            Toast.makeText(this, if (apartmentId == -1L) "Đã thêm căn hộ" else "Đã cập nhật căn hộ", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi lưu căn hộ", Toast.LENGTH_SHORT).show()
        }
    }
}