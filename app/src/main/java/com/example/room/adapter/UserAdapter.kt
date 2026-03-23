package com.example.room.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.User

class UserAdapter(
    private var userList: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit,
    private val onChangePasswordClick: (User) -> Unit,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val tvUserFullName: TextView = itemView.findViewById(R.id.tvUserFullName)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnEditUser: ImageView = itemView.findViewById(R.id.btnEditUser)
        val btnDeleteUser: ImageView = itemView.findViewById(R.id.btnDeleteUser)
        val btnChangePassword: ImageView = itemView.findViewById(R.id.btnChangePassword)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.tvUserInitial.text = user.fullName.firstOrNull()?.uppercase() ?: "U"
        holder.tvUserFullName.text = user.fullName
        holder.tvUsername.text = "@${user.username}"

        val roleText = when(user.role) {
            1 -> "Admin"
            2 -> "Khách hàng"
            else -> "User"
        }
        holder.tvUserRole.text = roleText

        holder.btnEditUser.setOnClickListener { onEditClick(user) }
        holder.btnDeleteUser.setOnClickListener { onDeleteClick(user) }
        holder.btnChangePassword.setOnClickListener { onChangePasswordClick(user) }
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}