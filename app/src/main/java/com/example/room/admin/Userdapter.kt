package com.example.room.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.model.User

class UserAdapter(
    private var userList: ArrayList<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val tvUserFullName: TextView = itemView.findViewById(R.id.tvUserFullName)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnEditUser: ImageView = itemView.findViewById(R.id.btnEditUser)
        val btnDeleteUser: ImageView = itemView.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUserInitial.text = user.fullName.take(1).uppercase()
        holder.tvUserFullName.text = user.fullName
        holder.tvUsername.text = "@${user.username}"
        if (user.role == 1) {
            holder.tvUserRole.text = "Admin"
            holder.tvUserRole.setTextColor(holder.itemView.context.getColor(R.color.colorPrimary))
        } else {
            holder.tvUserRole.text = "Khách hàng"
            holder.tvUserRole.setTextColor(holder.itemView.context.getColor(R.color.colorAccent))
        }
        holder.btnEditUser.setOnClickListener { onEditClick(user) }
        holder.btnDeleteUser.setOnClickListener { onDeleteClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: ArrayList<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}