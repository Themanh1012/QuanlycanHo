package com.example.room.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.Apartment
import java.io.File
import java.text.DecimalFormat

class SavedAdapter(
    private val list: ArrayList<Apartment>
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgApartment = view.findViewById<ImageView>(R.id.imgApartment)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val btnUnsave = view.findViewById<ImageView>(R.id.btnUnsave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val apt = list[position]

            holder.tvTitle.text = apt.title
            holder.tvAddress.text = apt.address
            val formatter = DecimalFormat("#,###")
            holder.tvPrice.text = formatter.format(apt.price) + " VND"

            // Xử lý ảnh an toàn (tránh crash do ảnh quá lớn)
            val paths = apt.imagePaths.split(",").filter { it.isNotEmpty() }
            val firstPath = if (paths.isNotEmpty()) paths[0].trim() else ""

            if (firstPath.isNotEmpty()) {
                if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                    val resId = holder.itemView.context.resources.getIdentifier(firstPath, "drawable", holder.itemView.context.packageName)
                    if (resId != 0) {
                        holder.imgApartment.setImageResource(resId)
                    } else {
                        holder.imgApartment.setImageResource(R.drawable.canho1)
                    }
                } else {
                    val imgFile = File(firstPath)
                    if (imgFile.exists()) {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        options.inSampleSize = calculateInSampleSize(options, 300, 200)
                        options.inJustDecodeBounds = false
                        
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        if (bitmap != null) {
                            holder.imgApartment.setImageBitmap(bitmap)
                        } else {
                            holder.imgApartment.setImageResource(R.drawable.canho1)
                        }
                    } else {
                        holder.imgApartment.setImageResource(R.drawable.canho1)
                    }
                }
            } else {
                holder.imgApartment.setImageResource(R.drawable.canho1)
            }

            // ⭐ bỏ lưu
            holder.btnUnsave.setOnClickListener {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}