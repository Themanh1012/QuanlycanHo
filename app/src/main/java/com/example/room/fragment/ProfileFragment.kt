package com.example.room.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.room.user.EditProfileActivity
import com.example.room.user.HistoryActivity
import com.example.room.R
import com.example.room.user.SettingsActivity
import com.example.room.auth.LoginActivity
import com.example.room.admin.AdminDashboardActivity

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var cardAdminPanel: CardView
    private lateinit var btnAdminDashboard: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Ánh xạ views
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserRole = view.findViewById(R.id.tvUserRole)
        cardAdminPanel = view.findViewById(R.id.cardAdminPanel)
        btnAdminDashboard = view.findViewById(R.id.btnAdminDashboard)

        // Lấy thông tin user từ SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userName = sharedPref.getString("FULL_NAME", "Khách hàng")
        val role = sharedPref.getInt("ROLE", 0)

        // Hiển thị thông tin user
        tvUserName.text = userName

        // Kiểm tra nếu là Admin
        if (isLoggedIn && role == 1) {
            tvUserRole.text = "Quản trị viên"
            cardAdminPanel.visibility = View.VISIBLE
        } else {
            tvUserRole.text = "Khách hàng"
            cardAdminPanel.visibility = View.GONE
        }

        // Nút Admin Dashboard
        btnAdminDashboard.setOnClickListener {
            val intent = Intent(requireActivity(), AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        // Nút Đăng xuất
        val btnLogout = view.findViewById<Button>(R.id.btnLogoutFromProfile)
        btnLogout.setOnClickListener {
            // Xóa session
            sharedPref.edit().clear().apply()

            // Chuyển về LoginActivity
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        val btnEdit = view.findViewById<View>(R.id.btnEditProfile)
        btnEdit?.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        val btnHistory = view.findViewById<View>(R.id.btnHistory)
        btnHistory?.setOnClickListener {
            startActivity(Intent(requireActivity(), HistoryActivity::class.java))
        }

        val btnSettings = view.findViewById<View>(R.id.btnSettings)
        btnSettings?.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }

        return view
    }
    override fun onResume() {
        super.onResume()

        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userName = sharedPref.getString("FULL_NAME", "Khách hàng")

        tvUserName.text = userName
    }

}