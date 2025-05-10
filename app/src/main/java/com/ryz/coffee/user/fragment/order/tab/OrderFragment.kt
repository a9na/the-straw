package com.ryz.coffee.user.fragment.order.tab

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.order.recyclerview.BasketActivity

class OrderFragment : Fragment() {

    private lateinit var tvCount: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var frCount: FrameLayout
    private lateinit var imgBasket: ImageView

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerTabLayout: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        tvCount = view.findViewById(R.id.tvCount)
        frCount = view.findViewById(R.id.frCount)
        imgBasket = view.findViewById(R.id.imgBasket)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPagerTabLayout = view.findViewById(R.id.viewPagerTabLayout)


        mAuth = Firebase.auth

        imgBasket.setOnClickListener {
            startActivity(Intent(it.context, BasketActivity::class.java))
        }

        val adapterViewPager = FragmentAdapter(activity?.supportFragmentManager!!, lifecycle)

        viewPagerTabLayout.apply {
            adapter = adapterViewPager
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(tabLayout, viewPagerTabLayout) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Belum dibayar"
                }
                1 -> {
                    tab.text = "Dikemas"
                }
                2 -> {
                    tab.text = "Dikirim"
                }
                3 -> {
                    tab.text = "Selesai"
                }
                4 -> {
                    tab.text = "Dibatalkan"
                }
            }
        }.attach()

        getCountItem()
        return view
    }

    private fun getCountItem() {
        // untuk mendapatkan email user
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    frCount.visibility = View.VISIBLE
                    val countItem: String = snapshot.childrenCount.toString()
                    tvCount.text = countItem
                } else {
                    frCount.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}