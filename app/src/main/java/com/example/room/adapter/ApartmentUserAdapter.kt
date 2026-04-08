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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (dbHelper == null) dbHelper = DatabaseHelper(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
        holder.tvPrice.text = formatter.format(apartment.price) + " VND/tháng"
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

        val paths = apartment.imagePaths.split(",")
        val firstPath = if (paths.isNotEmpty()) paths[0] else ""

        if (firstPath.isNotEmpty()) {
            if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                val resId = holder.itemView.context.resources.getIdentifier(firstPath, "drawable", holder.itemView.context.packageName)
                if (resId != 0) holder.img.setImageResource(resId)
                else holder.img.setImageResource(R.drawable.canho01)
            } else {
                val imgFile = File(firstPath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    holder.img.setImageBitmap(bitmap)
                } else {
                    holder.img.setImageResource(R.drawable.canho01)
                }
            }
        } else {
            holder.img.setImageResource(R.drawable.canho01)
        }

        holder.img.setOnClickListener {
            android.util.Log.d("TEST", "CLICK IMG ${apartment.id}")
            onItemClick(apartment)
        }

        holder.itemView.setOnClickListener {
            android.util.Log.d("TEST", "CLICK ITEM ${apartment.id}")
            onItemClick(apartment)
        }
    }

    override fun getItemCount() = apartments.size
}
