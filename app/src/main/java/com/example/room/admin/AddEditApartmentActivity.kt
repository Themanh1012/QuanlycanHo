package com.example.room.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
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

class AddEditApartmentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var apartmentId: Int = 0
    private var isEditMode = false
    private var selectedImagePath: String = ""
    private var selectedBadge: String = ""

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
    private lateinit var spinnerBadge: Spinner

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        private const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupBadgeSpinner()
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
        
        spinnerBadge = Spinner(this)
        val layout = findViewById<LinearLayout>(R.id.layoutContainer)
        val tvLabel = TextView(this).apply {
            text = "Danh hiệu căn hộ"
            textSize = 14f
            setPadding(0, 16, 0, 4)
        }
        layout.addView(tvLabel, layout.indexOfChild(findViewById(R.id.edtDescription)))
        layout.addView(spinnerBadge, layout.indexOfChild(tvLabel) + 1)
    }

    private fun setupBadgeSpinner() {
        val badges = arrayOf("Không có", "VIP KIM CƯƠNG", "HẠNG VÀNG", "HẠNG BẠC", "GIẢM GIÁ HOT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, badges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBadge.adapter = adapter
    }

    private fun checkMode() {
        isEditMode = intent.getStringExtra("mode") == "edit"
        apartmentId = intent.getIntExtra("apartment_id", 0)

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
            
            val badges = arrayOf("Không có", "VIP KIM CƯƠNG", "HẠNG VÀNG", "HẠNG BẠC", "GIẢM GIÁ HOT")
            val index = badges.indexOf(it.badge)
            if (index >= 0) spinnerBadge.setSelection(index)

            if (it.imagePath.isNotEmpty()) {
                selectedImagePath = it.imagePath
                loadImage(it.imagePath)
            }
        }
    }

    private fun loadImage(path: String) {
        try {
            if (!path.contains("/") && !path.contains("\\")) {
                val resId = resources.getIdentifier(path, "drawable", packageName)
                if (resId != 0) {
                    imgApartment.setImageResource(resId)
                    imgApartment.visibility = View.VISIBLE
                    layoutPlaceholder.visibility = View.GONE
                    btnChangeImage.visibility = View.VISIBLE
                    return
                }
            }

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
        cardImageUpload.setOnClickListener { showImageSourceDialog() }
        btnChangeImage.setOnClickListener { showImageSourceDialog() }
        btnDelete.setOnClickListener { showDeleteDialog() }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Chọn từ Gallery", "Chọn từ Camera", "Ảnh mẫu từ App")
        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkPermissionAndOpenGallery()
                    1 -> openCamera()
                    2 -> showDrawableImages()
                }
            }
            .show()
    }

    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
            } else { openGallery() }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            } else { openGallery() }
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2001)
        } else {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2003)
        }
    }

    private fun showDrawableImages() {
        val drawableNames = arrayOf("canho01", "canho02", "canho03", "batdongsan", "landmark", "thaodien", "vinhomes", "bietthu", "chungcu", "studio")
        val existingDrawables = drawableNames.filter { name ->
            resources.getIdentifier(name, "drawable", packageName) != 0
        }.toTypedArray()

        if (existingDrawables.isEmpty()) {
            Toast.makeText(this, "Không có ảnh mẫu nào", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh mẫu")
            .setItems(existingDrawables) { _, which ->
                val drawableName = existingDrawables[which]
                val drawableId = resources.getIdentifier(drawableName, "drawable", packageName)
                if (drawableId != 0) {
                    selectedImagePath = drawableName 
                    imgApartment.setImageResource(drawableId)
                    imgApartment.visibility = View.VISIBLE
                    layoutPlaceholder.visibility = View.GONE
                    btnChangeImage.visibility = View.VISIBLE
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { saveImageFromUri(it) }
                }
            }
            2003 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    (data.extras?.get("data") as? Bitmap)?.let { saveBitmapToStorage(it) }
                }
            }
        }
    }

    private fun saveImageFromUri(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val imageDir = File(filesDir, "apartment_images")
                if (!imageDir.exists()) imageDir.mkdirs()

                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val imageFile = File(imageDir, fileName)
                FileOutputStream(imageFile).use { output ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (stream.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                }
                selectedImagePath = imageFile.absolutePath
                val bitmap = BitmapFactory.decodeFile(selectedImagePath)
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

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        try {
            val imageDir = File(filesDir, "apartment_images")
            if (!imageDir.exists()) imageDir.mkdirs()

            val fileName = "camera_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imageDir, fileName)
            FileOutputStream(imageFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            selectedImagePath = imageFile.absolutePath
            imgApartment.setImageBitmap(bitmap)
            imgApartment.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
            btnChangeImage.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveApartment() {
        val title = edtTitle.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val areaStr = edtArea.text.toString().trim()
        val badge = if (spinnerBadge.selectedItemPosition == 0) "" else spinnerBadge.selectedItem.toString()

        if (title.isEmpty()) { Toast.makeText(this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show(); return }
        if (priceStr.isEmpty()) { Toast.makeText(this, "Vui lòng nhập giá thuê!", Toast.LENGTH_SHORT).show(); return }
        if (address.isEmpty()) { Toast.makeText(this, "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show(); return }

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
            id_user = 1,
            badge = badge
        )

        val result = if (isEditMode) dbHelper.updateApartment(apartment) else dbHelper.insertApartment(apartment).toInt()

        if (result > 0) {
            Toast.makeText(this, if (isEditMode) "Cập nhật thành công!" else "Thêm căn hộ thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa căn hộ này?")
            .setPositiveButton("Xóa") { dialog, _ ->
                val result = dbHelper.deleteApartment(apartmentId)
                if (result > 0) {
                    Toast.makeText(this, "Đã xóa căn hộ!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
