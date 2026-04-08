package com.example.room.admin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.io.FileOutputStream

class AddEditApartmentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var apartmentId: Int = 0
    private var isEditMode = false
    private val selectedImages = mutableListOf<String>()

    private lateinit var tvTitle: TextView
    private lateinit var edtTitle: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtArea: EditText
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var btnChangeImage: TextView
    private lateinit var cardImageUpload: CardView
    private lateinit var btnDelete: Button
    private lateinit var btnSave: TextView
    private lateinit var spinnerBadge: Spinner
    private lateinit var rvImages: RecyclerView
    private lateinit var imageAdapter: ImageSelectedAdapter

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.forEach { uri ->
            saveImageFromUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_apartment)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupBadgeSpinner()
        setupRecyclerView()
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
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        cardImageUpload = findViewById(R.id.cardImageUpload)
        btnDelete = findViewById(R.id.btnDelete)
        btnSave = findViewById(R.id.btnSave)
        rvImages = findViewById(R.id.rvImages)
        
        spinnerBadge = Spinner(this)
        val layout = findViewById<LinearLayout>(R.id.layoutContainer)
        val tvLabel = TextView(this).apply {
            text = "Danh hiệu căn hộ"
            textSize = 14f
            setPadding(0, 32, 0, 8)
        }
        val index = layout.indexOfChild(findViewById(R.id.edtDescription)) - 1
        layout.addView(tvLabel, index)
        layout.addView(spinnerBadge, index + 1)
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageSelectedAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyDataSetChanged()
            updateImageVisibility()
        }
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter
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

            if (it.imagePaths.isNotEmpty()) {
                selectedImages.addAll(it.imagePaths.split(","))
                imageAdapter.notifyDataSetChanged()
                updateImageVisibility()
            }
        }
    }

    private fun updateImageVisibility() {
        if (selectedImages.isEmpty()) {
            rvImages.visibility = View.GONE
            layoutPlaceholder.visibility = View.VISIBLE
            btnChangeImage.text = "Thêm ảnh"
        } else {
            rvImages.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
            btnChangeImage.text = "Thêm ảnh tiếp"
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
        val options = arrayOf("Chọn từ Gallery (Bộ sưu tập)", "Chọn từ Camera", "Ảnh mẫu từ App")
        AlertDialog.Builder(this)
            .setTitle("Chọn phương thức")
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
        } else {
            galleryLauncher.launch("image/*")
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
        // Cập nhật đầy đủ danh sách ảnh từ screenshot bạn gửi
        val drawableNames = arrayOf(
            "canho1", "canho1_1", "canho1_2", "canho1_3", 
            "canho2_1", "canho2_2", "canho2_3", "canho2_4",
            "canho3", "canho3_1", "canho3_2", "canho3_3", 
            "canho4", "canho4_1", "canho4_2", "canho4_3", 
            "canho5", "canho5_1", "canho5_2", "canho5_3", 
            "canho6", "canho6_1", "canho6_2", "canho6_3",
            "batdongsan", "landmark", "thaodien", "vinhomes", 
            "bietthu", "chungcu", "studio"
        )
        
        val existingDrawables = drawableNames.filter { name ->
            resources.getIdentifier(name, "drawable", packageName) != 0
        }.toTypedArray()

        if (existingDrawables.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ảnh mẫu nào trong drawable!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh mẫu")
            .setItems(existingDrawables) { _, which ->
                selectedImages.add(existingDrawables[which])
                imageAdapter.notifyDataSetChanged()
                updateImageVisibility()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 2003) {
            (data?.extras?.get("data") as? Bitmap)?.let { saveBitmapToStorage(it) }
        }
    }

    private fun saveImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val imageDir = File(filesDir, "apartment_images")
                if (!imageDir.exists()) imageDir.mkdirs()
                
                val fileName = "img_${System.currentTimeMillis()}_${(1..1000).random()}.jpg"
                val imageFile = File(imageDir, fileName)
                
                FileOutputStream(imageFile).use { output ->
                    val buffer = ByteArray(4096)
                    var length: Int
                    while (stream.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                }
                
                selectedImages.add(imageFile.absolutePath)
                runOnUiThread {
                    imageAdapter.notifyDataSetChanged()
                    updateImageVisibility()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show()
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
            selectedImages.add(imageFile.absolutePath)
            imageAdapter.notifyDataSetChanged()
            updateImageVisibility()
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
        val badge = if (spinnerBadge.selectedItemPosition <= 0) "" else spinnerBadge.selectedItem.toString()

        if (title.isEmpty() || priceStr.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val area = areaStr.toDoubleOrNull() ?: 0.0
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 1)

        val apartment = Apartment(
            id = apartmentId,
            title = title,
            price = price,
            address = address,
            description = description,
            area = area,
            imagePaths = selectedImages.joinToString(","),
            status = "Còn trống",
            id_user = currentUserId,
            badge = badge
        )

        val result = if (isEditMode) dbHelper.updateApartment(apartment) else dbHelper.insertApartment(apartment).toInt()

        if (result > 0) {
            Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa căn hộ này?")
            .setPositiveButton("Xóa") { _, _ ->
                if (dbHelper.deleteApartment(apartmentId) > 0) {
                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

class ImageSelectedAdapter(private val images: List<String>, private val onDelete: (Int) -> Unit) : RecyclerView.Adapter<ImageSelectedAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgItem)
        val btnDelete = view.findViewById<ImageView>(R.id.btnDeleteImage)
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_image_selected, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = images[position]
        if (!path.contains("/") && !path.contains("\\")) {
            val resId = holder.itemView.resources.getIdentifier(path, "drawable", holder.itemView.context.packageName)
            if (resId != 0) holder.img.setImageResource(resId)
        } else {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) holder.img.setImageBitmap(bitmap)
            else holder.img.setImageResource(R.drawable.canho1)
        }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }
    override fun getItemCount() = images.size
}
