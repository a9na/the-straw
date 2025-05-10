package com.ryz.coffee.user.fragment.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.home.recyclerview.*
import com.ryz.coffee.user.fragment.profile.ChangeProfileActivity
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment(), UpdateRecyclerView {

    private lateinit var databaseReferenceProduct: DatabaseReference
    private lateinit var databaseReferenceCategory: DatabaseReference
    private lateinit var databaseReferenceBanner: DatabaseReference
    private lateinit var databaseReferenceUser: DatabaseReference

    private lateinit var mAuth: FirebaseAuth

    private lateinit var recyclerViewProduct: RecyclerView
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var recyclerViewBanner: RecyclerView

    private lateinit var homeCategoryAdapter: HomeCategoryAdapter
    private lateinit var homeCategoryModel: ArrayList<HomeCategoryModel>

    private lateinit var homeProductAdapter: HomeProductAdapter
    private lateinit var homeProductModel: ArrayList<HomeProductModel>

    private lateinit var homeBannerAdapter: HomeBannerAdapter
    private lateinit var homeBannerModel: ArrayList<HomeBannerModel>

    private lateinit var edSearch: EditText
    private lateinit var imgProfile: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerViewProduct = view.findViewById(R.id.rvProduct)
        recyclerViewCategory = view.findViewById(R.id.rvOtherCoffee)
        recyclerViewBanner = view.findViewById(R.id.rvBanner)
        edSearch = view.findViewById(R.id.edSearch)
        imgProfile = view.findViewById(R.id.imgProfile)

        mAuth = Firebase.auth

        recyclerViewProduct.layoutManager =
            GridLayoutManager(context, 2)

        recyclerViewCategory.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // snap recyclerview to stop scrolling
        val helperProduct: SnapHelper = GravitySnapHelper(Gravity.START)
        helperProduct.attachToRecyclerView(recyclerViewProduct)

        homeProductModel = ArrayList()
        homeCategoryModel = ArrayList()
        homeBannerModel = ArrayList()

        // retrieve data banner from firebase
        getDataBanner()

        // retrieve data product  from firebase
        getDataProduct()

        // retrieve data category from firebase
        getDataCategory()

        getDataUser()

        return view
    }

    private fun getDataBanner() {
        databaseReferenceBanner =
            FirebaseDatabase.getInstance().getReference("Product/ProductBanner")
        databaseReferenceBanner.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                homeBannerModel.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val image: String? =
                            dataSnapshot.child("image").getValue(String::class.java)
                        homeBannerModel.add(HomeBannerModel(image))
                    }
                    homeBannerAdapter = HomeBannerAdapter(homeBannerModel)

                    val linearLayout =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    recyclerViewBanner.layoutManager = linearLayout

                    /*val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            if (linearLayout.findLastCompletelyVisibleItemPosition() < (homeBannerAdapter.itemCount - 1)) {
                                linearLayout.smoothScrollToPosition(recyclerViewBanner,
                                    RecyclerView.State(),
                                    linearLayout.findLastCompletelyVisibleItemPosition() + 1)
                                Log.d("Scroll", "Scrolling")
                            } else {
                                linearLayout.smoothScrollToPosition(recyclerViewBanner,
                                    RecyclerView.State(),
                                    0)
                                Log.d("Scroll", "Not Scrolling")
                            }
                        }
                    }, 0, 4000)*/
                    homeBannerAdapter.notifyDataSetChanged()
                    recyclerViewBanner.setHasFixedSize(true)
                    recyclerViewBanner.adapter = homeBannerAdapter
                    recyclerViewBanner.isNestedScrollingEnabled = false
                    val snapHelper = GravitySnapHelper(Gravity.CENTER)
                    snapHelper.attachToRecyclerView(recyclerViewBanner)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getDataCategory() {
        databaseReferenceCategory =
            FirebaseDatabase.getInstance().getReference("Product/ProductCategory")
        databaseReferenceCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    homeCategoryModel.clear()
                    for (dataSnapshot in snapshot.children) {
                        val id: String? =
                            dataSnapshot.child("id").getValue(String::class.java)
                        val category: String? =
                            dataSnapshot.child("category").getValue(String::class.java)
                        homeCategoryModel.add(HomeCategoryModel(id, category))
                    }
                    homeCategoryAdapter = HomeCategoryAdapter(homeCategoryModel, this@HomeFragment)
                    homeCategoryAdapter.notifyDataSetChanged()
                    recyclerViewCategory.setHasFixedSize(true)
                    recyclerViewCategory.adapter = homeCategoryAdapter
                    recyclerViewCategory.isNestedScrollingEnabled = false
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getDataProduct() {
        databaseReferenceProduct =
            FirebaseDatabase.getInstance().getReference("Product/ProductData")
        databaseReferenceProduct.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        homeProductModel.clear()
                        for (dataSnapshot in snapshot.children) {
                            val id: String? =
                                dataSnapshot.child("id").getValue(String::class.java)
                            val rating: String? =
                                dataSnapshot.child("rating").getValue(String::class.java)
                            val image: String? =
                                dataSnapshot.child("image").getValue(String::class.java)
                            val title: String? =
                                dataSnapshot.child("title").getValue(String::class.java)
                            val category: String? =
                                dataSnapshot.child("category").getValue(String::class.java)
                            val price: String? =
                                dataSnapshot.child("price").getValue(String::class.java)
                            val desc: String? = dataSnapshot.child("desc").getValue(String::class.java)
                            homeProductModel.add(HomeProductModel(id,
                                rating,
                                image,
                                title,
                                category,
                                price,
                                desc))
                        }
                        homeProductAdapter = HomeProductAdapter(homeProductModel)
                        homeProductAdapter.notifyDataSetChanged()
                        recyclerViewProduct.setHasFixedSize(true)
                        recyclerViewProduct.adapter = homeProductAdapter
                        recyclerViewProduct.isNestedScrollingEnabled = false

                        edSearch.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {

                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                            }

                            override fun afterTextChanged(p0: Editable?) {
                                val filteredList: ArrayList<HomeProductModel> = ArrayList()

                                for (item in homeProductModel) {
                                    if (item.title!!.lowercase()
                                            .contains(p0.toString()
                                                .lowercase()) or (item.category!!.lowercase()
                                            .contains(p0.toString().lowercase()))
                                    )
                                        filteredList.add(item)
                                }
                                homeProductAdapter.filterList(filteredList)
                            }
                        })

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getDataUser() {
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")

        databaseReferenceUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val photoProfile: String? = snapshot.child("photo").getValue(String::class.java)
                    val imgUri: String = photoProfile.toString()
                    Picasso.get().load(imgUri).into(imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        imgProfile.setOnClickListener {
            startActivity(Intent(it.context, ChangeProfileActivity::class.java))
        }
    }

    override fun callback(items: ArrayList<HomeProductModel>) {
        homeProductAdapter = HomeProductAdapter(items)
        homeProductAdapter.notifyDataSetChanged()
        recyclerViewProduct.setHasFixedSize(true)
        recyclerViewProduct.adapter = homeProductAdapter
    }
}