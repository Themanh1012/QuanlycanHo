package com.example.room.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.text.DecimalFormat

class ApartmentAdapter(
    private var apartments: ArrayList<Apartment>,
    private val onItemClick: (Apartment) -> Unit,
    private val onEditClick: (Apartment) -> Unit,
    private val onDeleteClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder>() {

    private var dbHelper: DatabaseHelper? = null

    class ApartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgApartment: ImageView = itemView.findViewById(R.id.imgApartment)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvArea: TextView = itemView.findViewById(R.id.tvArea)
        val tvRenterName: TextView = itemView.findViewById(R.id.tvRenterName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        if (dbHelper == null) dbHelper = DatabaseHelper(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment, parent, false)
        return ApartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        try {
            val apartment = apartments[position]

            holder.tvTitle.text = apartment.title
            holder.tvAddress.text = apartment.address
            holder.tvArea.text = "Diện tích: ${apartment.area} m²"
            
            val formatter = DecimalFormat("#,###")
            holder.tvPrice.text = formatter.format(apartment.price) + " VND/tháng"

            if (apartment.status.contains("Đã thuê", ignoreCase = true)) {
                val renter = apartment.id_renter?.let { dbHelper?.getUserById(it) }
                holder.tvStatus.text = "Đã thuê"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rented)
                
                holder.tvRenterName.visibility = View.VISIBLE
                holder.tvRenterName.text = "Người thuê: ${renter?.fullName ?: "N/A"}"
            } else {
                holder.tvStatus.text = "Còn trống"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available)
                holder.tvRenterName.visibility = View.GONE
            }

            val paths = apartment.imagePaths.split(",").filter { it.isNotEmpty() }
            val firstPath = if (paths.isNotEmpty()) paths[0].trim() else ""

            if (firstPath.isNotEmpty()) {
                val context = holder.itemView.context
                if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                    val resId = context.resources.getIdentifier(firstPath, "drawable", context.packageName)
                    if (resId != 0) {
                        // Tối ưu nạp ảnh từ DRAWABLE
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeResource(context.resources, resId, options)
                        options.inSampleSize = calculateInSampleSize(options, 300, 200)
                        options.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
                        holder.imgApartment.setImageBitmap(bitmap)
                    } else {
                        holder.imgApartment.setImageResource(R.drawable.canho1)
                    }
                } else {
                    val imgFile = File(firstPath)
                    if (imgFile.exists()) {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        options.inSampleSize = calculateInSampleSize(options, 300, 200)
                        options.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        holder.imgApartment.setImageBitmap(bitmap)
                    } else {
                        holder.imgApartment.setImageResource(R.drawable.canho1)
                    }
                }
            } else {
                holder.imgApartment.setImageResource(R.drawable.canho1)
            }

            holder.btnDetail.setOnClickListener { onItemClick(apartment) }
            holder.btnEdit.setOnClickListener { onEditClick(apartment) }
            holder.btnDelete.setOnClickListener { onDeleteClick(apartment) }
            
        } catch (e: Exception) {
            e.printStackTrace()
            holder.imgApartment.setImageResource(R.drawable.canho1)
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

    override fun getItemCount(): Int = apartments.size

    fun updateList(newList: ArrayList<Apartment>) {
        this.apartments = newList
        notifyDataSetChanged()
    }
}
