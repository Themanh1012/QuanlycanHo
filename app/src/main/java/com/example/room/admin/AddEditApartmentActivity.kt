package com.example.room.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddEditApartmentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var apartmentId: Int = 0
    private var isEditMode = false
    private var selectedImagePath: String = ""

    private lateinit var tvTitle: TextView
    private lateinit var edtTitle: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtArea: EditText
    private lateinit var imgApartment: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var btnChangeImage: TextView
    private lateinit var cardImageUpload: CardView
    private lateinit var btnDelete: Button
    private lateinit var btnSave: TextView

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        private const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)

        dbHelper = DatabaseHelper(this)
        initViews()
        checkMode()
        setupClickListeners()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        edtTitle = findViewById(R.id.edtTitle)
        edtPrice = findViewById(R.id.edtPrice)
        edtAddress = findViewById(R.id.edtAddress)
        edtDescription = findViewById(R.id.edtDescription)
        edtArea = findViewById(R.id.edtArea)
        imgApartment = findViewById(R.id.imgApartment)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        cardImageUpload = findViewById(R.id.cardImageUpload)
        btnDelete = findViewById(R.id.btnDelete)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun checkMode() {
        isEditMode = intent.getStringExtra("mode") == "edit"
        apartmentId = intent.getIntExtra("apartmentId", 0)

        if (isEditMode && apartmentId > 0) {
            tvTitle.text = "Sửa căn hộ"
            btnDelete.visibility = View.VISIBLE
            loadApartmentData()
        } else {
            tvTitle.text = "Thêm căn hộ mới"
            btnDelete.visibility = View.GONE
            selectedImagePath = ""
        }
    }

    private fun loadApartmentData() {
        val apartment = dbHelper.getApartmentById(apartmentId)
        apartment?.let {
            edtTitle.setText(it.title)
            edtPrice.setText(it.price.toLong().toString())
            edtAddress.setText(it.address)
            edtDescription.setText(it.description)
            edtArea.setText(if (it.area > 0) it.area.toLong().toString() else "")

            if (it.imagePath.isNotEmpty()) {
                selectedImagePath = it.imagePath
                loadImage(it.imagePath)
            }
        }
    }

    private fun loadImage(path: String) {
        try {
            val imgFile = File(path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                if (bitmap != null) {
                    imgApartment.setImageBitmap(bitmap)
                    imgApartment.visibility = View.VISIBLE
                    layoutPlaceholder.visibility = View.GONE
                    btnChangeImage.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        btnSave.setOnClickListener { saveApartment() }
        cardImageUpload.setOnClickListener { checkPermissionAndOpenPicker() }
        btnChangeImage.setOnClickListener { checkPermissionAndOpenPicker() }
        btnDelete.setOnClickListener { showDeleteDialog() }
    }

    private fun checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openImagePicker()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Cần cấp quyền truy cập ảnh!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(it)
                    inputStream?.let { stream ->
                        // LƯU VÀO INTERNAL STORAGE - Đảm bảo ảnh không bị mất
                        val imageDir = File(filesDir, "apartment_images")
                        if (!imageDir.exists()) {
                            imageDir.mkdirs()
                        }

                        val fileName = "img_${System.currentTimeMillis()}.jpg"
                        val imageFile = File(imageDir, fileName)
                        val outputStream = FileOutputStream(imageFile)

                        val buffer = ByteArray(1024)
                        var length: Int
                        while (stream.read(buffer).also { len -> length = len } > 0) {
                            outputStream.write(buffer, 0, length)
                        }

                        stream.close()
                        outputStream.flush()
                        outputStream.close()

                        selectedImagePath = imageFile.absolutePath

                        // Hiển thị ảnh
                        val bitmap = BitmapFactory.decodeFile(selectedImagePath)
                        if (bitmap != null) {
                            imgApartment.setImageBitmap(bitmap)
                            imgApartment.visibility = View.VISIBLE
                            layoutPlaceholder.visibility = View.GONE
                            btnChangeImage.visibility = View.VISIBLE
                            Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveApartment() {
        val title = edtTitle.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val areaStr = edtArea.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá thuê!", Toast.LENGTH_SHORT).show()
            return
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val area = areaStr.toDoubleOrNull() ?: 0.0

        val apartment = Apartment(
            id = apartmentId,
            title = title,
            price = price,
            address = address,
            description = description,
            area = area,
            imagePath = selectedImagePath,
            status = "Còn trống",
            id_user = 1
        )

        val result = if (isEditMode) {
            dbHelper.updateApartment(apartment)
        } else {
            dbHelper.insertApartment(apartment).toInt()
        }

        if (result > 0) {
            Toast.makeText(this, if (isEditMode) "Cập nhật thành công!" else "Thêm thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa căn hộ này?")
            .setPositiveButton("Xóa") { dialog, _ ->
                val result = dbHelper.deleteApartment(apartmentId)
                if (result > 0) {
                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}