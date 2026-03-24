package com.example.room.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.Apartment
import android.widget.ImageView
import android.content.Intent
import com.example.room.listing.ApartmentDetailActivity
import android.content.ContentValues
import com.example.room.database.DatabaseHelper


class ApartmentUserAdapter(
    private val list: ArrayList<Apartment>,
    private val onClick: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentUserAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val btnDetail = view.findViewById<Button>(R.id.btnDetail)
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
        holder.tvPrice.text = "${apt.price} VND"

        val tvStatus = holder.itemView.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = apt.status

        if (apt.status == "Đã thuê") {
            tvStatus.setBackgroundResource(R.drawable.bg_status_rented)
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_status_available)
        }


        holder.btnDetail.setOnClickListener {
            val context = holder.itemView.context

            val dbHelper = DatabaseHelper(context)
            dbHelper.insertViewHistory(apt.id)

            val intent = Intent(context, ApartmentDetailActivity::class.java)

            intent.putExtra("apartment_id", apt.id)
            context.startActivity(intent)
        }

        val btnEdit = holder.itemView.findViewById<ImageView>(R.id.btnEdit)
        val btnDelete = holder.itemView.findViewById<ImageView>(R.id.btnDelete)

        btnEdit.visibility = View.GONE
        btnDelete.visibility = View.GONE
    }
}