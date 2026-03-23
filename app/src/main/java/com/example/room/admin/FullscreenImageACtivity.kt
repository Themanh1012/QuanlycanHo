package com.example.room.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.room.R
import java.io.File

class FullscreenImageActivity : AppCompatActivity() {

    private lateinit var ivImage: ImageView
    private lateinit var ivClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        ivImage = findViewById(R.id.ivImage)
        ivClose = findViewById(R.id.ivClose)

        val imagePath = intent.getStringExtra("IMAGE_PATH")

        if (!imagePath.isNullOrEmpty()) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                ivImage.setImageBitmap(bitmap)
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        ivClose.setOnClickListener {
            finish()
        }

        ivImage.setOnClickListener {
            finish()
        }
    }
}