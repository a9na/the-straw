package com.ryz.coffee.user.fragment.home.recyclerview

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ryz.coffee.R

class HomeCategoryAdapter(
    private var homeCategoryModel: ArrayList<HomeCategoryModel>,
    private var updateRecyclerView: UpdateRecyclerView,
) :
    RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder>() {

    private lateinit var databaseReferenceMain: DatabaseReference
    private var selectedItem = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val cardViewCategory: CardView = itemView.findViewById(R.id.cardViewCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_home_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val secondModelList = homeCategoryModel[position]
        holder.tvCategory.text = secondModelList.category

        holder.cardViewCategory.setOnClickListener {
            var check = false
            selectedItem = position
            notifyDataSetChanged()

            val pos: Int = holder.layoutPosition

            if (pos != RecyclerView.NO_POSITION) {
                val clickedItem: HomeCategoryModel = homeCategoryModel[pos]

                val items: ArrayList<HomeProductModel> = ArrayList()
                databaseReferenceMain =
                    FirebaseDatabase.getInstance().getReference("Product/ProductData")
                databaseReferenceMain.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            items.clear()
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

                                if (clickedItem.category!!.contains(category!!)) {
                                    Log.d("Check", "True")
                                    check = true
                                    if (check) {
                                        items.add(HomeProductModel(id, rating,
                                            image,
                                            title,
                                            category,
                                            price,
                                            desc))
                                        updateRecyclerView.callback(items)
                                    }
                                } else if (clickedItem.category!!.contains("Semua")) {
                                    items.add(HomeProductModel(id, rating,
                                        image,
                                        title,
                                        category,
                                        price,
                                        desc))
                                    updateRecyclerView.callback(items)

                                } else if (clickedItem.id!!.contains(id!!) && clickedItem.category!!.contains(
                                        "Rekomendasi")
                                ) {
                                    check = true
                                    if (check) {
                                        items.add(HomeProductModel(id, rating,
                                            image,
                                            title,
                                            category,
                                            price,
                                            desc))
                                        updateRecyclerView.callback(items)
                                    }
                                } else if (!check && clickedItem.category != category) {
                                    items.clear()
                                    updateRecyclerView.callback(items)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }
        if (selectedItem == position) {
            holder.tvCategory.setTextColor(Color.parseColor("#FF000000"))
            holder.cardViewCategory.setCardBackgroundColor(Color.parseColor("#E3B37A"))
        } else {
            holder.tvCategory.setTextColor(Color.parseColor("#AAAAAA"))
            holder.cardViewCategory.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        }
    }

    override fun getItemCount(): Int {
        return homeCategoryModel.size
    }
}