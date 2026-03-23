package com.example.quanlycanho.admin

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlycanho.R
import com.example.quanlycanho.adapter.UserAdapter
import com.example.quanlycanho.database.DatabaseHelper
import com.example.quanlycanho.model.User

class ManageUserActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var etSearch: EditText
    private lateinit var ivBack: ImageView

    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        databaseHelper = DatabaseHelper(this)

        initViews()
        loadUsers()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        etSearch = findViewById(R.id.etSearch)
        ivBack = findViewById(R.id.ivBack)

        adapter = UserAdapter(
            userList,
            onDeleteClick = { user -> deleteUser(user) },
            onItemClick = { user -> viewUserDetails(user) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadUsers() {
        userList.clear()
        userList.addAll(databaseHelper.getAllUsers())
        adapter.updateList(userList)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            userList
        } else {
            ArrayList(userList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            })
        }
        adapter.updateList(filteredList)
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc muốn xóa người dùng \"${user.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                databaseHelper.deleteUser(user.id)
                loadUsers()
                Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun viewUserDetails(user: User) {
        AlertDialog.Builder(this)
            .setTitle(user.name)
            .setMessage("""
                Email: ${user.email}
                Số điện thoại: ${user.phone}
                Vai trò: ${if (user.role == "admin") "Quản trị viên" else "Người dùng"}
            """.trimIndent())
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}