package com.example.room.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.R
import com.example.room.adapter.SavedAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment

class SavedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var list = ArrayList<Apartment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        recyclerView = view.findViewById(R.id.rvSaved)
        dbHelper = DatabaseHelper(requireContext())

        loadData()

        adapter = SavedAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        loadData()
        adapter.notifyDataSetChanged()
    }

    private fun loadData() {
        // Lấy userId từ SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        list.clear()
        list.addAll(dbHelper.getSavedApartments(userId))
    }
}