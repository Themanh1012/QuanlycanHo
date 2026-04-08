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
import com.example.room.listing.ApartmentListActivity

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

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserRole = view.findViewById(R.id.tvUserRole)
        cardAdminPanel = view.findViewById(R.id.cardAdminPanel)
        btnAdminDashboard = view.findViewById(R.id.btnAdminDashboard)

        updateUserInfo()

        btnAdminDashboard.setOnClickListener {
            val intent = Intent(requireActivity(), AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        val btnLogout = view.findViewById<Button>(R.id.btnLogoutFromProfile)
        btnLogout.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            startActivity(Intent(requireActivity(), EditProfileActivity::class.java))
        }

        view.findViewById<View>(R.id.btnRentedApartments)?.setOnClickListener {
            val intent = Intent(requireActivity(), ApartmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "RENTED")
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btnHistory)?.setOnClickListener {
            startActivity(Intent(requireActivity(), HistoryActivity::class.java))
        }

        view.findViewById<View>(R.id.btnSettings)?.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }

        return view
    }

    private fun updateUserInfo() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val fullName = sharedPref.getString("fullName", "Khách hàng")
        val role = sharedPref.getInt("role", 0)

        tvUserName.text = fullName

        if (isLoggedIn && role == 1) {
            tvUserRole.text = "Quản trị viên"
            cardAdminPanel.visibility = View.VISIBLE
        } else {
            tvUserRole.text = "Khách hàng"
            cardAdminPanel.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }
}
