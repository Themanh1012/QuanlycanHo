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
import com.example.room.model.Apartment
import android.content.Intent
import com.example.room.listing.ApartmentDetailActivity
import com.example.room.database.DatabaseHelper
import java.io.File
import java.text.DecimalFormat

class ApartmentUserAdapter(
    private val list: ArrayList<Apartment>,
    private val onClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentUserAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val btnDetail = view.findViewById<Button>(R.id.btnDetail)
        val imgApartment: ImageView? = view.findViewById(R.id.imgApartment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apartment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = list[position]

        holder.tvTitle.text = apt.title
        holder.tvAddress.text = apt.address

        // Format giá đúng
        val formatter = DecimalFormat("#,###")
        holder.tvPrice.text = formatter.format(apt.price) + " VND"

        val tvStatus = holder.itemView.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = apt.status

        if (apt.status == "Đã thuê") {
            tvStatus.setBackgroundResource(R.drawable.bg_status_rented)
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_status_available)
        }

        // ẨN NÚT SỬA/XÓA CHO KHÁCH HÀNG
        val btnEdit = holder.itemView.findViewById<ImageView>(R.id.btnEdit)
        val btnDelete = holder.itemView.findViewById<ImageView>(R.id.btnDelete)
        btnEdit?.visibility = View.GONE
        btnDelete?.visibility = View.GONE

        // Hiển thị ảnh
        if (holder.imgApartment != null) {
            if (apt.imagePath.isNotEmpty()) {
                try {
                    val imgFile = File(apt.imagePath)
                    if (imgFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        if (bitmap != null) {
                            holder.imgApartment.setImageBitmap(bitmap)
                        } else {
                            holder.imgApartment.setImageResource(R.drawable.canho01)
                        }
                    } else {
                        holder.imgApartment.setImageResource(R.drawable.canho01)
                    }
                } catch (e: Exception) {
                    holder.imgApartment.setImageResource(R.drawable.canho01)
                }
            } else {
                holder.imgApartment.setImageResource(R.drawable.canho01)
            }
        }

        holder.btnDetail.setOnClickListener {
            val context = holder.itemView.context

            val dbHelper = DatabaseHelper(context)
            val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("userId", 0)
            dbHelper.insertViewHistory(apt.id, userId)

            val intent = Intent(context, ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apt.id)
            context.startActivity(intent)
        }
    }
}