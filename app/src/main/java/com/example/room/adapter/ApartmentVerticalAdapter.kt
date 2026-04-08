package com.example.room.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import java.io.File
import java.text.DecimalFormat

class ApartmentVerticalAdapter(
    private var apartments: List<Apartment>,
    private val onItemClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentVerticalAdapter.ViewHolder>() {

    // Khai báo DatabaseHelper
    private var dbHelper: DatabaseHelper? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgApartment)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvBadge: TextView? = view.findViewById(R.id.tvBadge)
        val tvStatus: TextView? = view.findViewById(R.id.tvStatus) // Ánh xạ Status

        // Ánh xạ icon Trái tim
        val btnFavoriteCard: CardView? = view.findViewById(R.id.btnFavoriteCard)
        val ivFavoriteIcon: ImageView? = view.findViewById(R.id.ivFavoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Khởi tạo DB nếu chưa có
        if (dbHelper == null) dbHelper = DatabaseHelper(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment_vertical, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apartment = apartments[position]
        val context = holder.itemView.context

        // Lấy ID người dùng hiện tại
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        // 1. Kiểm tra xem đã lưu chưa để đổi màu Trái tim
        val isSaved = dbHelper?.isApartmentSaved(apartment.id, userId) ?: false
        if (isSaved) {
            holder.ivFavoriteIcon?.setImageResource(android.R.drawable.btn_star_big_on)
            holder.ivFavoriteIcon?.setColorFilter(Color.parseColor("#EF4444")) // Màu đỏ
        } else {
            holder.ivFavoriteIcon?.setImageResource(android.R.drawable.btn_star_big_off)
            holder.ivFavoriteIcon?.setColorFilter(Color.parseColor("#FFFFFF")) // Màu trắng
        }

        // 2. Logic Lưu / Bỏ lưu khi bấm vào Trái tim
        holder.btnFavoriteCard?.setOnClickListener {
            val currentSaved = dbHelper?.isApartmentSaved(apartment.id, userId) ?: false
            if (currentSaved) {
                dbHelper?.unsaveApartment(apartment.id, userId)
            } else {
                dbHelper?.saveApartment(apartment.id, userId)
            }
            // Tải lại đúng cái thẻ này để nó đổi màu tim ngay lập tức
            notifyItemChanged(position)
        }

        // Đổ dữ liệu Text
        holder.tvTitle.text = apartment.title
        holder.tvAddress.text = apartment.address

        val formatter = DecimalFormat("#,###")
        holder.tvPrice.text = formatter.format(apartment.price) + " VND/tháng"

        // Đổ dữ liệu Trạng thái
        holder.tvStatus?.text = apartment.status
        if (apartment.status.contains("Đã thuê")) {
            holder.tvStatus?.setTextColor(Color.RED)
        } else {
            holder.tvStatus?.setTextColor(Color.WHITE)
        }

        // Đổ dữ liệu Tag (Badge)
        if (apartment.badge.isNotEmpty()) {
            holder.tvBadge?.text = apartment.badge
            holder.tvBadge?.visibility = View.VISIBLE
        } else {
            holder.tvBadge?.visibility = View.GONE
        }

        // Đổ dữ liệu Ảnh
        val paths = apartment.imagePaths.split(",")
        val firstPath = if (paths.isNotEmpty()) paths[0] else ""

        if (firstPath.isNotEmpty()) {
            if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                val resId = context.resources.getIdentifier(firstPath, "drawable", context.packageName)
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

        // Click vào thẻ để xem Chi tiết
        holder.itemView.setOnClickListener { onItemClick(apartment) }
    }

    override fun getItemCount() = apartments.size
}