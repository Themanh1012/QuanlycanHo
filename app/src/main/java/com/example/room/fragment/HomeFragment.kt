package com.example.room.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    // 1. Danh sách Căn hộ Nổi bật
    private lateinit var rvFeatured: RecyclerView
    private lateinit var adapterFeatured: ApartmentUserAdapter
    private var listFeatured = ArrayList<Apartment>()

    // 2. Danh sách Căn hộ Kim Cương
    private lateinit var rvDiamond: RecyclerView
    private lateinit var adapterDiamond: ApartmentUserAdapter
    private var listDiamond = ArrayList<Apartment>()

    // 3. Danh sách Căn hộ VIP (Vàng/Bạc)
    private lateinit var rvVip: RecyclerView
    private lateinit var adapterVip: ApartmentUserAdapter
    private var listVip = ArrayList<Apartment>()

    // 4. Danh sách Căn hộ Giảm giá
    private lateinit var rvDiscount: RecyclerView
    private lateinit var adapterDiscount: ApartmentUserAdapter
    private var listDiscount = ArrayList<Apartment>()

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

        dbHelper = DatabaseHelper(requireContext())

        // --- TÌM KIẾM ---
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

        // --- BƯỚC 1: ÁNH XẠ ID RECYCLERVIEW ---
        rvFeatured = view.findViewById(R.id.rvFeatured)
        rvDiamond = view.findViewById(R.id.rvDiamond)
        rvVip = view.findViewById(R.id.rvVip)
        rvDiscount = view.findViewById(R.id.rvDiscount)

        // --- BƯỚC 2: KHỞI TẠO ADAPTER ---
        adapterFeatured = createAdapter(listFeatured)
        adapterDiamond = createAdapter(listDiamond)
        adapterVip = createAdapter(listVip)
        adapterDiscount = createAdapter(listDiscount)

        // --- BƯỚC 3: CÀI ĐẶT LAYOUT MANAGER & GẮN ADAPTER ---
        setupRecyclerView(rvFeatured, adapterFeatured)
        setupRecyclerView(rvDiamond, adapterDiamond)
        setupRecyclerView(rvVip, adapterVip)
        setupRecyclerView(rvDiscount, adapterDiscount)

        // Gắn sự kiện chuyển trang cho các nút "Xem tất cả"
        val viewAllListener = View.OnClickListener {
            startActivity(Intent(requireActivity(), ApartmentListActivity::class.java))
        }
        // --- GẮN SỰ KIỆN VÀ GỬI "MẬT MÃ" QUA APARTMENT LIST ---
        view.findViewById<View>(R.id.tvViewAllApartments).setOnClickListener {
            val intent = Intent(requireActivity(), ApartmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "ALL")
            startActivity(intent)
        }
        view.findViewById<View>(R.id.tvViewAllDiamond).setOnClickListener {
            val intent = Intent(requireActivity(), ApartmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "DIAMOND")
            startActivity(intent)
        }
        view.findViewById<View>(R.id.tvViewAllVip).setOnClickListener {
            val intent = Intent(requireActivity(), ApartmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "VIP")
            startActivity(intent)
        }
        view.findViewById<View>(R.id.tvViewAllDiscount).setOnClickListener {
            val intent = Intent(requireActivity(), ApartmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "DISCOUNT")
            startActivity(intent)
        }

        // Nạp dữ liệu lần đầu
        loadData()

        return view
    }

    // Hàm hỗ trợ tạo Adapter nhanh gọn
    private fun createAdapter(list: ArrayList<Apartment>): ApartmentUserAdapter {
        return ApartmentUserAdapter(list) { apartment ->
            val intent = Intent(requireActivity(), ApartmentDetailActivity::class.java)
            intent.putExtra("apartment_id", apartment.id)
            startActivity(intent)
        }
    }

    // Hàm hỗ trợ setup RecyclerView cuộn ngang
    private fun setupRecyclerView(rv: RecyclerView, adapter: ApartmentUserAdapter) {
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = adapter
    }

    private fun updateGreeting() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("fullName", "Khách hàng")
        tvGreeting.text = "Xin chào, $fullName!"
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        loadData() // Load lại dữ liệu để cập nhật nếu user vừa thả tim hoặc admin vừa thêm phòng
    }

    private fun loadData() {
        val allApts = dbHelper.getAllApartments()
        distributeData(allApts)
    }

    private fun filterData(keyword: String) {
        val allApts = dbHelper.getAllApartments()
        val filtered = if (keyword.isEmpty()) {
            allApts
        } else {
            allApts.filter {
                it.title.contains(keyword, true) ||
                        it.address.contains(keyword, true)
            }
        }
        distributeData(filtered)
    }

    // HÀM PHÂN LOẠI DỮ LIỆU THÔNG MINH THEO TAG (BADGE)
    private fun distributeData(sourceList: List<Apartment>) {
        // 1. Nổi bật (Lấy 5 căn mới nhất bất kỳ)
        listFeatured.clear()
        listFeatured.addAll(sourceList.take(5))

        // 2. Căn hộ Kim Cương (Badge = VIP KIM CƯƠNG)
        listDiamond.clear()
        listDiamond.addAll(sourceList.filter { it.badge == "VIP KIM CƯƠNG" })

        // 3. Căn hộ VIP (Badge = HẠNG VÀNG hoặc HẠNG BẠC)
        listVip.clear()
        listVip.addAll(sourceList.filter { it.badge == "HẠNG VÀNG" || it.badge == "HẠNG BẠC" })

        // 4. Giảm giá (Badge = GIẢM GIÁ HOT)
        listDiscount.clear()
        listDiscount.addAll(sourceList.filter { it.badge == "GIẢM GIÁ HOT" })

        // Thông báo cho tất cả Adapter cập nhật UI
        adapterFeatured.notifyDataSetChanged()
        adapterDiamond.notifyDataSetChanged()
        adapterVip.notifyDataSetChanged()
        adapterDiscount.notifyDataSetChanged()

        // 💡 Ẩn đi các mục nếu không có dữ liệu (Tuỳ chọn thêm để UI mượt hơn)
        view?.findViewById<View>(R.id.sectionDiamond)?.visibility = if (listDiamond.isEmpty()) View.GONE else View.VISIBLE
        view?.findViewById<View>(R.id.sectionVip)?.visibility = if (listVip.isEmpty()) View.GONE else View.VISIBLE
        view?.findViewById<View>(R.id.sectionDiscount)?.visibility = if (listDiscount.isEmpty()) View.GONE else View.VISIBLE
    }
}