package com.example.room

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Bắt sự kiện nút Đăng xuất
        val btnLogout = view.findViewById<Button>(R.id.btnLogoutFromProfile)
        btnLogout.setOnClickListener {
            // Chuyển về màn hình Login
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            // Đóng MainActivity chứa các fragment này lại
            requireActivity().finish()
        }

        val btnEdit = view.findViewById<View>(R.id.btnEditProfile)
        if (btnEdit != null) {
            btnEdit.setOnClickListener {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                startActivity(intent)
            }
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
}