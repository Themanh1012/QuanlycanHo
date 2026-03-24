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

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentUserAdapter
    private var list = ArrayList<Apartment>()

    private lateinit var edtSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("FULL_NAME", "User")

        tvGreeting.text = "Xin chào, $fullName!"

        //thanh tìm kiếm
        edtSearch = view.findViewById(R.id.edtSearch)

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
        })

        dbHelper = DatabaseHelper(requireContext())

        // ====== SECTION SWITCH ======
        val sectionApartments = view.findViewById<View>(R.id.sectionApartments)
        val sectionMotels = view.findViewById<View>(R.id.sectionMotels)

        view.findViewById<View>(R.id.btnNavApartment).setOnClickListener {
            sectionApartments.visibility = View.VISIBLE
            sectionMotels.visibility = View.GONE
        }

        view.findViewById<View>(R.id.btnNavMotel).setOnClickListener {
            sectionApartments.visibility = View.GONE
            sectionMotels.visibility = View.VISIBLE
        }

        // ====== VIEW ALL ======
        view.findViewById<View>(R.id.tvViewAllApartments).setOnClickListener {
            startActivity(Intent(requireActivity(), ApartmentListActivity::class.java))
        }

        view.findViewById<View>(R.id.tvViewAllMotelsHome).setOnClickListener {
            startActivity(Intent(requireActivity(), MotelListActivity::class.java))
        }

        // ====== ADD POST ======
        view.findViewById<View>(R.id.btnNavAddPost).setOnClickListener {
            startActivity(Intent(requireActivity(), AddPostActivity::class.java))
        }

        // ====== 🔥 RECYCLER VIEW ======
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

    override fun onResume() {
        super.onResume()
        loadData()
        adapter.notifyDataSetChanged()
    }


    private fun loadData() {
        list.clear()
        list.addAll(dbHelper.getAllApartments().take(5)) // lấy 5 cái nổi bật
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