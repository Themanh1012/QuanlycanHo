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
    private var apartments: List<Apartment>,
    private val onItemClick: (Apartment) -> Unit,
    private val onUnsaveClick: (Apartment) -> Unit // Đảm bảo khai báo đúng chuẩn non-null
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgApartment: ImageView = view.findViewById(R.id.imgApartment)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        // Ánh xạ đúng ID của nút xóa (trái tim đỏ) trong item_saved.xml
        val btnUnsave: ImageView = view.findViewById(R.id.btnUnsave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apartment = apartments[position]

        // Đổ dữ liệu Text
        holder.tvTitle.text = apartment.title
        holder.tvAddress.text = apartment.address

        val formatter = DecimalFormat("#,###")
        holder.tvPrice.text = formatter.format(apartment.price) + " VNĐ/tháng"

        // Xử lý nạp Ảnh (Giữ nguyên logic cũ của dự án)
        val paths = apartment.imagePaths.split(",")
        val firstPath = if (paths.isNotEmpty()) paths[0] else ""

        if (firstPath.isNotEmpty()) {
            if (!firstPath.contains("/") && !firstPath.contains("\\")) {
                val resId = holder.itemView.context.resources.getIdentifier(firstPath, "drawable", holder.itemView.context.packageName)
                if (resId != 0) holder.imgApartment.setImageResource(resId)
                else holder.imgApartment.setImageResource(R.drawable.canho01)
            } else {
                val imgFile = File(firstPath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    holder.imgApartment.setImageBitmap(bitmap)
                } else {
                    holder.imgApartment.setImageResource(R.drawable.canho01)
                }
            }
        } else {
            holder.imgApartment.setImageResource(R.drawable.canho01)
        }

        // =========================================
        // XỬ LÝ SỰ KIỆN CLICK (FIX LỖI TẠI ĐÂY)
        // =========================================

        // 1. Click vào thẻ để xem chi tiết
        holder.itemView.setOnClickListener {
            onItemClick(apartment)
        }

        // 2. Click vào trái tim đỏ để Bỏ lưu
        // Truyền chính xác đối tượng 'apartment' hiện tại ra ngoài, đảm bảo không bao giờ Null
        holder.btnUnsave.setOnClickListener {
            onUnsaveClick(apartment)
        }
    }

    override fun getItemCount() = apartments.size
}