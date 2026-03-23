package com.example.room.admin

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
import java.io.File
import java.text.DecimalFormat

class ApartmentAdapter(
    private var apartmentList: ArrayList<Apartment>,
    private val onEditClick: (Apartment) -> Unit,
    private val onDeleteClick: (Apartment) -> Unit,
    private val onDetailClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder>() {

    class ApartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgApartment: ImageView = itemView.findViewById(R.id.imgApartment)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvArea: TextView = itemView.findViewById(R.id.tvArea)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apartment, parent, false)
        return ApartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        val apartment = apartmentList[position]

        holder.tvTitle.text = apartment.title
        holder.tvPrice.text = formatPrice(apartment.price)
        holder.tvAddress.text = apartment.address
        holder.tvArea.text = "Diện tích: ${apartment.area} m²"

        // Set status
        holder.tvStatus.text = apartment.status
        if (apartment.status == "Còn trống") {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available)
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rented)
        }

        // Load image
        if (apartment.imagePath.isNotEmpty()) {
            val imgFile = File(apartment.imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.imgApartment.setImageBitmap(bitmap)
            }
        }

        // Click events
        holder.btnDetail.setOnClickListener { onDetailClick(apartment) }
        holder.btnEdit.setOnClickListener { onEditClick(apartment) }
        holder.btnDelete.setOnClickListener { onDeleteClick(apartment) }
    }

    override fun getItemCount(): Int = apartmentList.size

    fun updateList(newList: ArrayList<Apartment>) {
        apartmentList = newList
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Double): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(price) + " VND/tháng"
    }
}