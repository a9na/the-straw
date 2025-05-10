package com.ryz.coffee.user.fragment.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.favorite.recyclerview.FavoriteAdapter
import com.ryz.coffee.user.fragment.favorite.recyclerview.FavoriteModel

class FavoriteFragment : Fragment() {

    private lateinit var databaseReferenceFavorite: DatabaseReference
    private lateinit var recyclerViewFavorite: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var favoriteModel: ArrayList<FavoriteModel>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var imgEmpty: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerViewFavorite= view.findViewById(R.id.rvFavorite)
        imgEmpty = view.findViewById(R.id.imgEmpty)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)

        mAuth = Firebase.auth

        recyclerViewFavorite.layoutManager = LinearLayoutManager(context)

        favoriteModel = ArrayList()

        getFavoriteData()

        return view
    }

    private fun getFavoriteData() {
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReferenceFavorite = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductFavorite")
        databaseReferenceFavorite.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    favoriteModel.clear()
                    for (dataSnapshot in snapshot.children) {
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
                        favoriteModel.add(FavoriteModel(rating, image, title, category, price, desc))
                    }
                    favoriteAdapter = FavoriteAdapter(favoriteModel)
                    favoriteAdapter.notifyDataSetChanged()
                    recyclerViewFavorite.setHasFixedSize(true)
                    recyclerViewFavorite.adapter = favoriteAdapter
                    recyclerViewFavorite.isNestedScrollingEnabled = false
                } else {
                    imgEmpty.visibility = View.VISIBLE
                    tvTitle.visibility = View.VISIBLE
                    tvSubtitle.visibility = View.VISIBLE
                    favoriteModel.clear()
                    favoriteAdapter = FavoriteAdapter(favoriteModel)
                    favoriteAdapter.notifyDataSetChanged()
                    recyclerViewFavorite.setHasFixedSize(true)
                    recyclerViewFavorite.adapter = favoriteAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}