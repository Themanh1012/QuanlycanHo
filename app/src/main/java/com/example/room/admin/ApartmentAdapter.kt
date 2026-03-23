package com.example.quanlycanho.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlycanho.R
import com.example.quanlycanho.model.Apartment
import java.io.File

class ApartmentAdapter(
    private var apartmentList: List<Apartment>,
    private val onEditClick: (Apartment) -> Unit,
    private val onDeleteClick: (Apartment) -> Unit,
    private val onItemClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder>() {

    class ApartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivApartmentImage)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apartment, parent, false)
        return ApartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        val apartment = apartmentList[position]

        holder.tvName.text = apartment.name
        holder.tvAddress.text = apartment.address
        holder.tvPrice.text = "${String.format("%,d", apartment.price.toLong())} VNĐ/tháng"

        holder.tvStatus.text = if (apartment.status == "available") "Còn trống" else "Đã thuê"
        holder.tvStatus.setTextColor(
            if (apartment.status == "available")
                holder.itemView.context.getColor(R.color.status_available)
            else
                holder.itemView.context.getColor(R.color.status_occupied)
        )

        // Load image
        if (apartment.imagePath.isNotEmpty()) {
            val imgFile = File(apartment.imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(apartment.imagePath)
                holder.ivImage.setImageBitmap(bitmap)
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_apartment)
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_apartment)
        }

        holder.ivEdit.setOnClickListener { onEditClick(apartment) }
        holder.ivDelete.setOnClickListener { onDeleteClick(apartment) }
        holder.itemView.setOnClickListener { onItemClick(apartment) }
    }

    override fun getItemCount(): Int = apartmentList.size

    fun updateList(newList: List<Apartment>) {
        apartmentList = newList
        notifyDataSetChanged()
    }
}