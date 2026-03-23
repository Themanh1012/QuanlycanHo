package com.example.quanlycanho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlycanho.R
import com.example.quanlycanho.model.usser

class UserAdapter(
    private var userList: List<User>,
    private val onDeleteClick: (User) -> Unit,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.tvName.text = user.name
        holder.tvEmail.text = user.email

        holder.tvRole.text = if (user.role == "admin") "Admin" else "User"
        holder.tvRole.setBackgroundResource(
            if (user.role == "admin") R.drawable.bg_role_badge_admin
            else R.drawable.bg_role_badge_user
        )

        holder.ivDelete.setOnClickListener { onDeleteClick(user) }
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}