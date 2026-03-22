package com.example.room.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.room.listing.AddPostActivity
import com.example.room.listing.ApartmentDetailActivity
import com.example.room.listing.ApartmentListActivity
import com.example.room.listing.MotelListActivity
import com.example.room.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // ==========================================
        // 1. LOGIC ẨN/HIỆN TAB (CĂN HỘ <-> PHÒNG TRỌ)
        // ==========================================

        // Tìm 2 khối giao diện to đùng mà ta đã đặt ID bên file XML
        val sectionApartments = view.findViewById<View>(R.id.sectionApartments)
        val sectionMotels = view.findViewById<View>(R.id.sectionMotels)

        // Nút Căn hộ (hình tròn)
        val btnNavApartment = view.findViewById<View>(R.id.btnNavApartment)
        btnNavApartment?.setOnClickListener {
            // Khi bấm vào: Mở khối Căn hộ lên, Giấu khối Phòng trọ đi
            sectionApartments?.visibility = View.VISIBLE
            sectionMotels?.visibility = View.GONE
        }

        // Nút Phòng trọ (hình tròn)
        val btnNavMotel = view.findViewById<View>(R.id.btnNavMotel)
        btnNavMotel?.setOnClickListener {
            // Khi bấm vào: Giấu khối Căn hộ đi, Mở khối Phòng trọ lên
            sectionApartments?.visibility = View.GONE
            sectionMotels?.visibility = View.VISIBLE
        }

        // ==========================================
        // 2. LOGIC NÚT "XEM TẤT CẢ" (MỞ RA TRANG MỚI)
        // ==========================================

        // Nút "Xem tất cả" của phần Căn hộ
        val tvViewAllApt = view.findViewById<View>(R.id.tvViewAllApartments)
        tvViewAllApt?.setOnClickListener {
            startActivity(Intent(requireActivity(), ApartmentListActivity::class.java))
        }

        // Nút "Xem tất cả" của phần Phòng trọ (mới thêm vào XML)
        val tvViewAllMotelsHome = view.findViewById<View>(R.id.tvViewAllMotelsHome)
        tvViewAllMotelsHome?.setOnClickListener {
            startActivity(Intent(requireActivity(), MotelListActivity::class.java)) // Đã sửa tên!
        }

        // ==========================================
        // 3. LOGIC CÁC NÚT KHÁC (GIỮ NGUYÊN CỦA BẠN)
        // ==========================================

        // Bấm vào 1 cái thẻ Căn hộ cụ thể để xem chi tiết
        val cardApartment1 = view.findViewById<CardView>(R.id.cardApartment1)
        if(cardApartment1 != null) {
            cardApartment1.setOnClickListener {
                val intent = Intent(requireActivity(), ApartmentDetailActivity::class.java)
                startActivity(intent)
            }
        }

        // Bấm vào nút "Đăng tin"
        val btnAddPost = view.findViewById<View>(R.id.btnNavAddPost)
        if (btnAddPost != null) {
            btnAddPost.setOnClickListener {
                val intent = Intent(requireActivity(), AddPostActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }
}