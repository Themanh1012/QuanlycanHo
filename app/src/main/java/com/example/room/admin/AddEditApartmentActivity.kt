package com.example.room.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
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

    // Views
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
        apartmentId = intent.getIntExtra("apartment_id", 0)  // Đổi từ "apartmentId" thành "apartment_id"

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
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveApartment()
        }

        cardImageUpload.setOnClickListener {
            showImageSourceDialog()
        }

        btnChangeImage.setOnClickListener {
            showImageSourceDialog()
        }

        btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    // ==================== CHỌN NGUỒN ẢNH ====================

    private fun showImageSourceDialog() {
        val options = arrayOf("Chọn từ Gallery", "Chọn từ Camera", "Ảnh mẫu từ App")

        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkPermissionAndOpenGallery()  // Gallery
                    1 -> openCamera()  // Camera
                    2 -> showDrawableImages()  // Drawable
                }
            }
            .show()
    }

    // ==================== TỪ GALLERY ====================

    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openGallery()
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
        }
    }

    // ==================== TỪ CAMERA ====================

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                2001
            )
        } else {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2003)
        }
    }

    // ==================== TỪ DRAWABLE (ẢNH MẪU) ====================

    private fun showDrawableImages() {
        // Danh sách tên ảnh trong res/drawable (không có đuôi file)
        val drawableNames = arrayOf(
            "canho01",
            "canho02",
            "batdongsan"
        )

        // Lọc chỉ lấy những ảnh thực sự tồn tại
        val existingDrawables = drawableNames.filter { name ->
            val id = resources.getIdentifier(name, "drawable", packageName)
            id != 0
        }.toTypedArray()

        if (existingDrawables.isEmpty()) {
            Toast.makeText(this, "Không có ảnh mẫu nào. Vui lòng thêm ảnh vào res/drawable/", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh mẫu")
            .setItems(existingDrawables) { dialog, which ->
                val drawableName = existingDrawables[which]
                val drawableId = resources.getIdentifier(drawableName, "drawable", packageName)
                if (drawableId != 0) {
                    val path = copyDrawableToStorage(drawableId)
                    if (path.isNotEmpty()) {
                        selectedImagePath = path
                        loadImage(selectedImagePath)
                        Toast.makeText(this, "Đã chọn ảnh mẫu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun copyDrawableToStorage(drawableId: Int): String {
        try {
            // Tạo thư mục lưu ảnh
            val imageDir = File(filesDir, "apartment_images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }

            // Tạo file với tên unique
            val fileName = "drawable_${System.currentTimeMillis()}.png"
            val imageFile = File(imageDir, fileName)

            // Lấy bitmap từ drawable
            val bitmap = BitmapFactory.decodeResource(resources, drawableId)

            // Lưu bitmap ra file
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi lưu ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            return ""
        }
    }

    // ==================== XỬ LÝ KẾT QUẢ ====================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Cần cấp quyền truy cập ảnh!", Toast.LENGTH_LONG).show()
                }
            }
            2001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Cần cấp quyền Camera!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    uri?.let {
                        saveImageFromUri(it)
                    }
                }
            }
            2003 -> { // Camera
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        saveBitmapToStorage(it)
                    }
                }
            }
        }
    }

    private fun saveImageFromUri(uri: android.net.Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                // Tạo thư mục lưu ảnh
                val imageDir = File(filesDir, "apartment_images")
                if (!imageDir.exists()) {
                    imageDir.mkdirs()
                }

                // Tạo file với tên unique
                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val imageFile = File(imageDir, fileName)
                val outputStream = FileOutputStream(imageFile)

                // Copy dữ liệu
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

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        try {
            // Tạo thư mục lưu ảnh
            val imageDir = File(filesDir, "apartment_images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }

            // Tạo file với tên unique
            val fileName = "camera_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imageDir, fileName)
            val outputStream = FileOutputStream(imageFile)

            // Lưu bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            selectedImagePath = imageFile.absolutePath

            // Hiển thị ảnh
            imgApartment.setImageBitmap(bitmap)
            imgApartment.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
            btnChangeImage.visibility = View.VISIBLE
            Toast.makeText(this, "Đã chụp ảnh", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ==================== LƯU CĂN HỘ ====================

    private fun saveApartment() {
        val title = edtTitle.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val areaStr = edtArea.text.toString().trim()

        // Validation
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
            Toast.makeText(
                this,
                if (isEditMode) "Cập nhật thành công!" else "Thêm căn hộ thành công!",
                Toast.LENGTH_SHORT
            ).show()
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