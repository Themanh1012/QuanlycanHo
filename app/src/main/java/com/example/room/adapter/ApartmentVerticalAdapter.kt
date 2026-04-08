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

class ApartmentVerticalAdapter(
    private var apartments: List<Apartment>,
    private val onItemClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentVerticalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgApartment)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvBadge: TextView? = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // CHÚ Ý DÒNG NÀY: Dùng layout dọc
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment_vertical, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apartment = apartments[position]

        holder.tvTitle.text = apartment.title
        holder.tvAddress.text = apartment.address

        val formatter = DecimalFormat("#,###")
        holder.tvPrice.text = formatter.format(apartment.price)

        if (apartment.badge.isNotEmpty()) {
            holder.tvBadge?.text = apartment.badge
            holder.tvBadge?.visibility = View.VISIBLE
        } else {
            holder.tvBadge?.visibility = View.GONE
        }

        val paths = apartment.imagePaths.split(",")
        val firstPath = if (paths.isNotEmpty()) paths[0] else ""

        if (firstPath.isNotEmpty()) {
            if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                val resId = holder.itemView.context.resources.getIdentifier(firstPath, "drawable", holder.itemView.context.packageName)
                if (resId != 0) holder.img.setImageResource(resId)
                else holder.img.setImageResource(R.drawable.canho1)
            } else {
                val imgFile = File(firstPath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    holder.img.setImageBitmap(bitmap)
                } else {
                    holder.img.setImageResource(R.drawable.canho1)
                }
            }
        } else {
            holder.img.setImageResource(R.drawable.canho1)
        }

        holder.itemView.setOnClickListener { onItemClick(apartment) }
    }

    override fun getItemCount() = apartments.size
}