package com.example.room.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.Apartment

class ApartmentAdapter(
    private val context: Context,
    private val apartmentList: ArrayList<Apartment>
) : RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder>() {


    class ApartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val tvAddress: TextView = itemView.findViewById(R.id.tvItemAddress)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_apartment, parent, false)
        return ApartmentViewHolder(view)
    }


    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        val apartment = apartmentList[position]

        holder.tvTitle.text = apartment.title

        holder.tvPrice.text = "%,.0f VNĐ/tháng".format(apartment.price)
        holder.tvAddress.text = apartment.address
    }


    override fun getItemCount(): Int {
        return apartmentList.size
    }
}