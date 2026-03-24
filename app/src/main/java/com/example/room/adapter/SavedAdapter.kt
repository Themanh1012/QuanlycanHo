package com.example.room.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.Apartment

class SavedAdapter(
    private val list: ArrayList<Apartment>
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val btnUnsave = view.findViewById<ImageView>(R.id.btnUnsave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = list[position]

        holder.tvTitle.text = apt.title
        holder.tvAddress.text = apt.address
        holder.tvPrice.text = "${apt.price} VND"

        // ⭐ bỏ lưu
        holder.btnUnsave.setOnClickListener {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}