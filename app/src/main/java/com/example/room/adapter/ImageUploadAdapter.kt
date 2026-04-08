package com.example.room.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import java.io.File

class ImageUploadAdapter(
    private val images: List<String>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageUploadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgUpload)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_upload, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = images[position]
        
        if (path.startsWith("/")) {
            val bitmap = BitmapFactory.decodeFile(path)
            holder.img.setImageBitmap(bitmap)
        } else {
            val resId = holder.itemView.context.resources.getIdentifier(path, "drawable", holder.itemView.context.packageName)
            if (resId != 0) holder.img.setImageResource(resId)
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = images.size
}