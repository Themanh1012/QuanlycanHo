package com.example.room.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.room.listing.AddPostActivity
import com.example.room.listing.ApartmentDetailActivity
import com.example.room.listing.ApartmentListActivity
import com.example.room.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.room.adapter.ApartmentUserAdapter
import com.example.room.database.DatabaseHelper
import com.example.room.model.Apartment
import android.content.Context
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.os.Handler
import android.os.Looper

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentUserAdapter
    private var list = ArrayList<Apartment>()
    private lateinit var tvGreeting: TextView
    private lateinit var edtSearch: EditText
    
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        updateGreeting()

        edtSearch = view.findViewById(R.id.edtSearch)
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { filterData(s.toString()) }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        })

        dbHelper = DatabaseHelper(requireContext())

        val sectionApartments = view.findViewById<View>(R.id.sectionApartments)

        view.findViewById<View>(R.id.tvViewAllApartments).setOnClickListener {
            startActivity(Intent(requireActivity(), ApartmentListActivity::class.java))
        }

        recyclerView = view.findViewById(R.id.rvFeatured)
        loadData()

        adapter = ApartmentUserAdapter(list) { apartment ->
            val intent = Intent(requireActivity(), ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apartment.id)
            startActivity(intent)
        }

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        return view
    }

    private fun updateGreeting() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("fullName", "Khách hàng")
        tvGreeting.text = "Xin chào, $fullName!"
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        loadData()
        adapter.notifyDataSetChanged()
    }

    private fun loadData() {
        list.clear()
        list.addAll(dbHelper.getAllApartments().take(5))
    }

    private fun filterData(keyword: String) {
        val allList = dbHelper.getAllApartments()
        val filtered = if (keyword.isEmpty()) {
            allList
        } else {
            allList.filter {
                it.title.contains(keyword, true) ||
                        it.address.contains(keyword, true)
            }
        }
        list.clear()
        list.addAll(filtered.take(5))
        adapter.notifyDataSetChanged()
    }
}
