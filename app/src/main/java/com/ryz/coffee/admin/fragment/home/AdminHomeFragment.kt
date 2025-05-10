package com.ryz.coffee.admin.fragment.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import atownsend.swipeopenhelper.SwipeOpenItemTouchHelper
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.admin.fragment.home.recyclerview.ProductAdapter
import com.ryz.coffee.admin.fragment.home.recyclerview.ProductModel
import com.ryz.coffee.onboarding.OnBoardingWithLoginActivity

class AdminHomeFragment : Fragment(), ProductAdapter.ButtonCallbacks {

    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var productModel: ArrayList<ProductModel>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var addProduct: FloatingActionButton
    private lateinit var addBanner: FloatingActionButton
    private lateinit var imgLogout: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        recyclerView = view.findViewById(R.id.rvAdminProduct)
        addProduct = view.findViewById(R.id.addProduct)
        addBanner = view.findViewById(R.id.addBanner)
        imgLogout = view.findViewById(R.id.imgLogout)

        mAuth = Firebase.auth
        recyclerView.layoutManager = LinearLayoutManager(context)
        productModel = ArrayList()

        addProduct.setOnClickListener {
            startActivity(Intent(it.context, AddProductActivity::class.java))
        }

        addBanner.setOnClickListener {
            startActivity(Intent(it.context, AddBannerActivity::class.java))
        }

        imgLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(
                    context,
                    OnBoardingWithLoginActivity::class.java
                )
            )
        }

        getDataProduct()
        return view
    }

    private fun getDataProduct() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Product/ProductData")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    productModel.clear()
                    snapshot.children.forEach {
                        val category: String? = it.child("category").getValue(String::class.java)
                        val id: String? = it.child("id").getValue(String::class.java)
                        val image: String? = it.child("image").getValue(String::class.java)
                        val desc: String? = it.child("desc").getValue(String::class.java)
                        val price: String? = it.child("price").getValue(String::class.java)
                        val rating: String? = it.child("rating").getValue(String::class.java)
                        val title: String? = it.child("title").getValue(String::class.java)

                        productModel.add(ProductModel(category, id, image, price, rating, title, desc))
                    }
                    productModel.sortBy {
                        it.category
                    }
                    productAdapter = ProductAdapter(productModel, this@AdminHomeFragment)
                    val helper = SwipeOpenItemTouchHelper(SwipeOpenItemTouchHelper.SimpleCallback(
                        SwipeOpenItemTouchHelper.START or SwipeOpenItemTouchHelper.END))
                    productAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = productAdapter
                    recyclerView.isNestedScrollingEnabled = false
                    helper.attachToRecyclerView(recyclerView)
                    helper.setCloseOnAction(true)
                } else {
                    productModel.clear()
                    productAdapter = ProductAdapter(productModel, this@AdminHomeFragment)
                    productAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = productAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun removePosition(pos: Int) {
        productAdapter.removePosition(pos)
    }
}