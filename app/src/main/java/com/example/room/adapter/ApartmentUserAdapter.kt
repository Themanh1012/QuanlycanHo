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
import com.example.room.database.DatabaseHelper
import android.content.Context
import android.graphics.Paint

class ApartmentUserAdapter(
    private var apartments: List<Apartment>,
    private val onItemClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentUserAdapter.ViewHolder>() {

    private var dbHelper: DatabaseHelper? = null
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgApartment)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvBadge: TextView? = view.findViewById(R.id.tvBadge)

        val btnSave: ImageView = view.findViewById(R.id.btnSave)
        val btnSaveContainer: View = view.findViewById(R.id.btnSaveContainer)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvOldPrice: TextView? = view.findViewById(R.id.tvOldPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (dbHelper == null) dbHelper = DatabaseHelper(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val apartment = apartments[position]

            val context = holder.itemView.context
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("userId", 0)

            val isSaved = dbHelper?.isApartmentSaved(apartment.id, userId) ?: false

            if (isSaved) {
                holder.btnSave.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                holder.btnSave.setImageResource(android.R.drawable.btn_star_big_off)
            }
            holder.btnSaveContainer.setOnClickListener {
                val currentSaved = dbHelper?.isApartmentSaved(apartment.id, userId) ?: false
                if (currentSaved) {
                    dbHelper?.unsaveApartment(apartment.id, userId)
                } else {
                    dbHelper?.saveApartment(apartment.id, userId)
                }
                notifyItemChanged(position)
            }

            holder.tvTitle.text = apartment.title
            holder.tvAddress.text = apartment.address
            
            val formatter = DecimalFormat("#,###")
            
            if (apartment.badge == "GIẢM GIÁ HOT") {
                val finalPrice = apartment.getFinalPrice()
                holder.tvPrice.text = formatter.format(finalPrice) + " VND/tháng"
                holder.tvOldPrice?.apply {
                    visibility = View.VISIBLE
                    text = formatter.format(apartment.price)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            } else {
                holder.tvPrice.text = formatter.format(apartment.price) + " VND/tháng"
                holder.tvOldPrice?.visibility = View.GONE
            }

            holder.tvStatus.text = apartment.status
            if (apartment.status.contains("Đã thuê")) {
                holder.tvStatus.setTextColor(android.graphics.Color.RED)
            } else {
                holder.tvStatus.setTextColor(android.graphics.Color.WHITE)
            }

            if (apartment.badge.isNotEmpty()) {
                holder.tvBadge?.text = apartment.badge
                holder.tvBadge?.visibility = View.VISIBLE
            } else {
                holder.tvBadge?.visibility = View.GONE
            }

            val paths = apartment.imagePaths.split(",").filter { it.isNotEmpty() }
            val firstPath = if (paths.isNotEmpty()) paths[0].trim() else ""

            if (firstPath.isNotEmpty()) {
                if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                    val resId = context.resources.getIdentifier(firstPath, "drawable", context.packageName)
                    if (resId != 0) {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeResource(context.resources, resId, options)
                        options.inSampleSize = calculateInSampleSize(options, 400, 300)
                        options.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
                        holder.img.setImageBitmap(bitmap)
                    } else {
                        holder.img.setImageResource(R.drawable.canho1)
                    }
                } else {
                    val imgFile = File(firstPath)
                    if (imgFile.exists()) {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        options.inSampleSize = calculateInSampleSize(options, 400, 300)
                        options.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        holder.img.setImageBitmap(bitmap)
                    } else {
                        holder.img.setImageResource(R.drawable.canho1)
                    }
                }
            } else {
                holder.img.setImageResource(R.drawable.canho1)
            }

            holder.img.setOnClickListener { onItemClick(apartment) }
            holder.itemView.setOnClickListener { onItemClick(apartment) }

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

    override fun getItemCount() = apartments.size
}
