package com.example.room.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.room.AddPostActivity
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


        val sectionApartments = view.findViewById<View>(R.id.sectionApartments)
        val sectionMotels = view.findViewById<View>(R.id.sectionMotels)


        val btnNavApartment = view.findViewById<View>(R.id.btnNavApartment)
        btnNavApartment?.setOnClickListener {

            sectionApartments?.visibility = View.VISIBLE
            sectionMotels?.visibility = View.GONE
        }


        val btnNavMotel = view.findViewById<View>(R.id.btnNavMotel)
        btnNavMotel?.setOnClickListener {
            sectionApartments?.visibility = View.GONE
            sectionMotels?.visibility = View.VISIBLE
        }


        val tvViewAllApt = view.findViewById<View>(R.id.tvViewAllApartments)
        tvViewAllApt?.setOnClickListener {
            startActivity(Intent(requireActivity(), ApartmentListActivity::class.java))
        }


        val tvViewAllMotelsHome = view.findViewById<View>(R.id.tvViewAllMotelsHome)
        tvViewAllMotelsHome?.setOnClickListener {
            startActivity(Intent(requireActivity(), MotelListActivity::class.java)) // Đã sửa tên!
        }




        val cardApartment1 = view.findViewById<CardView>(R.id.cardApartment1)
        if(cardApartment1 != null) {
            cardApartment1.setOnClickListener {
                val intent = Intent(requireActivity(), ApartmentDetailActivity::class.java)
                startActivity(intent)
            }
        }


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