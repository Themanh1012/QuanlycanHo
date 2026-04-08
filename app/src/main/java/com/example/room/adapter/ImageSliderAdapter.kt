package com.example.room.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import java.io.File

class ImageSliderAdapter(private val imagePaths: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imgSlider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val path = imagePaths[position].trim()
        if (path.isEmpty()) return

        if (!path.contains("/") && !path.contains("\\")) {
            // Load từ drawable
            val resId = holder.itemView.context.resources.getIdentifier(path, "drawable", holder.itemView.context.packageName)
            if (resId != 0) {
                holder.imageView.setImageResource(resId)
            } else {
                holder.imageView.setImageResource(R.drawable.canho1)
            }
        } else {
            // Load từ file hệ thống
            val imgFile = File(path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.imageView.setImageBitmap(bitmap)
            } else {
                holder.imageView.setImageResource(R.drawable.canho1)
            }
        }
    }

    override fun getItemCount(): Int = imagePaths.size
}
