package com.example.room.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.UserAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.User

class ManageUserActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var etSearch: EditText
    private lateinit var ivBack: ImageView

    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)

        databaseHelper = DatabaseHelper(this)

        initViews()
        loadUsers()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewUsers)
        etSearch = findViewById(R.id.etSearch)
        ivBack = findViewById(R.id.ivBack)

        adapter = UserAdapter(
            userList,
            onEditClick = { user -> editUser(user) },
            onDeleteClick = { user -> deleteUser(user) },
            onChangePasswordClick = { user -> changePassword(user) },
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
                it.fullName.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true)
            })
        }
        adapter.updateList(filteredList)
    }

    private fun editUser(user: User) {
        val intent = Intent(this, AddEditUserActivity::class.java)
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc muốn xóa người dùng \"${user.fullName}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                databaseHelper.deleteUser(user.id)
                loadUsers()
                Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun changePassword(user: User) {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
    }

    private fun viewUserDetails(user: User) {
        val roleText = when(user.role) {
            1 -> "Quản trị viên"
            2 -> "Khách hàng"
            else -> "Không xác định"
        }

        val options = arrayOf("Xem chi tiết", "Chỉnh sửa", "Đổi mật khẩu", "Xóa")

        AlertDialog.Builder(this)
            .setTitle(user.fullName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUserDetails(user, roleText)
                    1 -> editUser(user)
                    2 -> changePassword(user)
                    3 -> deleteUser(user)
                }
            }
            .show()
    }

    private fun showUserDetails(user: User, roleText: String) {
        AlertDialog.Builder(this)
            .setTitle(user.fullName)
            .setMessage("""
                Username: ${user.username}
                Họ tên: ${user.fullName}
                Vai trò: $roleText
            """.trimIndent())
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}
