package com.example.room.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.SavedAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.listing.ApartmentDetailActivity
import com.example.room.model.Apartment

class SavedFragment : Fragment() {

    private lateinit var rvSaved: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SavedAdapter
    private var savedList = ArrayList<Apartment>()
    private var userId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        dbHelper = DatabaseHelper(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        rvSaved = view.findViewById(R.id.rvSaved)
        rvSaved.layoutManager = LinearLayoutManager(requireContext())

        adapter = SavedAdapter(savedList,
            onItemClick = { apartment ->
                val intent = Intent(requireActivity(), ApartmentDetailActivity::class.java)
                intent.putExtra("apartment_id", apartment.id)
                startActivity(intent)
            },
            onUnsaveClick = { apartment ->
                // DIỆT BÓNG MA: Truyền đúng thứ tự id căn hộ và id user
                dbHelper.unsaveApartment(apartment.id, userId)
                Toast.makeText(context, "Đã bỏ lưu căn hộ", Toast.LENGTH_SHORT).show()
                loadData() // Nạp lại tức thì
            }
        )

        rvSaved.adapter = adapter
        return view
    }

    // Mỗi khi chuyển từ Home sang tab Saved, hàm này tự động Refresh dữ liệu
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        // FIX LỖI Ở ĐÂY: Dùng đúng tên hàm getSavedApts của project sếp
        val listFromDb = dbHelper.getSavedApartments(userId)
        savedList.clear()
        savedList.addAll(listFromDb)
        adapter.notifyDataSetChanged()
    }
}