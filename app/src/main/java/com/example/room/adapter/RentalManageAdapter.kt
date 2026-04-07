package com.example.room.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.text.DecimalFormat

class RentalManageAdapter(
    private var list: ArrayList<Apartment>,
    private val dbHelper: DatabaseHelper,
    private val onEndRental: (Apartment) -> Unit
) : RecyclerView.Adapter<RentalManageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgApartment)
        val tvTitle: TextView = view.findViewById(R.id.tvApartmentTitle)
        val tvRenter: TextView = view.findViewById(R.id.tvRenterName)
        val tvPrice: TextView = view.findViewById(R.id.tvRentPrice)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val btnEnd: ImageView = view.findViewById(R.id.btnEndContract)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rental_manage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = list[position]
        holder.tvTitle.text = apt.title
        holder.tvAddress.text = apt.address
        
        val formatter = DecimalFormat("#,###")
        holder.tvPrice.text = "Giá: ${formatter.format(apt.price)} VND/tháng"

        // Lấy tên người thuê
        val renter = apt.id_renter?.let { dbHelper.getUserById(it) }
        holder.tvRenter.text = "Người thuê: ${renter?.fullName ?: "Không xác định"}"

        // Load ảnh
        val paths = apt.imagePaths.split(",")
        val firstPath = if (paths.isNotEmpty()) paths[0] else ""
        if (firstPath.isNotEmpty()) {
            if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                val resId = holder.itemView.context.resources.getIdentifier(firstPath, "drawable", holder.itemView.context.packageName)
                if (resId != 0) holder.img.setImageResource(resId)
            } else {
                val file = File(firstPath)
                if (file.exists()) {
                    holder.img.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                }
            }
        }

        holder.btnEnd.setOnClickListener { onEndRental(apt) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Apartment>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
