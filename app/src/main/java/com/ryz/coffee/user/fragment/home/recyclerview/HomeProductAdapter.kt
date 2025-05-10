package com.ryz.coffee.user.fragment.home.recyclerview

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.order.ProductDetailActivity
import com.squareup.picasso.Picasso

class HomeProductAdapter(
    private var homeProductModel: ArrayList<HomeProductModel>,
) : RecyclerView.Adapter<HomeProductAdapter.ViewHolder>() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val imgCoffeeBeans: ImageView = itemView.findViewById(R.id.imgCoffeeBeans)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val imgFavorite: ImageView = itemView.findViewById(R.id.imgFavorite)
        val parentLayout: CardView = itemView.findViewById(R.id.parentLayout)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_home_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        var checkImgFavorite = true

        val mainModelList = homeProductModel[position]
        holder.tvRating.text = mainModelList.rating
        holder.tvCategory.text = mainModelList.category
        holder.tvTitle.text = mainModelList.title
        holder.tvPrice.text = mainModelList.price

        val imgUri: String? = mainModelList.image
        Picasso.get().load(imgUri).into(holder.imgCoffeeBeans)

        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductFavorite")

        holder.imgFavorite.setOnClickListener {
            if (checkImgFavorite) {
                addFavorite(holder.tvTitle.text.toString(), position)
                Log.d("Clicked", "Clicked Pertama True")
                Toast.makeText(it.context, "Item telah ditambahkan ke daftar favorit anda", Toast.LENGTH_SHORT).show()
                checkImgFavorite = false

            } else if (!checkImgFavorite) {
                removeFavorite(holder.tvTitle.text.toString())
                Log.d("Clicked", "Clicked Pertama False")
                Toast.makeText(it.context, "Item telah dihapus dari daftar favorit anda", Toast.LENGTH_SHORT).show()
                checkImgFavorite = true
            }
        }

        // hold value if login again
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val title: String? = dataSnapshot.child("title").getValue(String::class.java)
                    if (title?.contains(holder.tvTitle.text.toString())!!) {
                        holder.imgFavorite.setImageResource(R.drawable.like_selected_icon)
                        Log.d("Clicked", "Clicked Holder True")
                        checkImgFavorite = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        holder.parentLayout.setOnClickListener{
            val intent = Intent(it.context, ProductDetailActivity::class.java)
            intent.putExtra("image", imgUri.toString())
            intent.putExtra("title", mainModelList.title)
            intent.putExtra("rating", mainModelList.rating)
            intent.putExtra("price", mainModelList.price)
            intent.putExtra("category", mainModelList.category)
            intent.putExtra("desc", mainModelList.desc)
            it.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return homeProductModel.size
    }

    fun filterList(filteredList: ArrayList<HomeProductModel>) {
        homeProductModel = filteredList
        notifyDataSetChanged()
    }

    private fun removeFavorite(title: String) {
        val query: Query = databaseReference.ref.orderByChild("title")
            .equalTo(title)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    ds.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addFavorite(title: String, position: Int) {
        val query: Query = databaseReference.ref.orderByChild("title")
            .equalTo(title)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val map = HashMap<String, Any>()
                    val mainModelList = homeProductModel[position]
                    map["rating"] = mainModelList.rating.toString()
                    map["category"] = mainModelList.category.toString()
                    map["title"] = mainModelList.title.toString()
                    map["price"] = mainModelList.price.toString()
                    map["image"] = mainModelList.image.toString()
                    map["desc"] = mainModelList.desc.toString()
                    databaseReference.push().setValue(map)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
}