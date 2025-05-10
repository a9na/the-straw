package com.ryz.coffee.user.fragment.favorite.recyclerview

import android.content.Intent
import android.os.Handler
import android.os.Looper
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

class FavoriteAdapter(private val favoriteModel: ArrayList<FavoriteModel>) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val imgCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val imgFavorite: ImageView = itemView.findViewById(R.id.imgFavorite)
        val parentLayout: CardView = itemView.findViewById(R.id.parentLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mAuth = Firebase.auth
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductFavorite")

        val favoriteModelList = favoriteModel[position]
        holder.tvRating.text = favoriteModelList.rating
        holder.tvCategory.text = favoriteModelList.category
        holder.tvTitle.text = favoriteModelList.title
        holder.tvPrice.text = favoriteModelList.price

        val imgUri: String? = favoriteModelList.image
        Picasso.get().load(imgUri).into(holder.imgCoffee)

        holder.imgFavorite.setOnClickListener {
            holder.imgFavorite.setImageResource(R.drawable.like_unselected_icon)
            Handler(Looper.getMainLooper()).postDelayed({
                removeFavorite(holder)
            },1000)
            Toast.makeText(it.context, "Item telah dihapus dari daftar favorit anda", Toast.LENGTH_SHORT).show()

        }

        holder.parentLayout.setOnClickListener {
            val intent = Intent(it.context, ProductDetailActivity::class.java)
            intent.putExtra("image", imgUri.toString())
            intent.putExtra("title", favoriteModelList.title)
            intent.putExtra("rating", favoriteModelList.rating)
            intent.putExtra("price", favoriteModelList.price)
            intent.putExtra("category", favoriteModelList.category)
            intent.putExtra("desc", favoriteModelList.desc)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return favoriteModel.size
    }

    private fun removeFavorite(holder: ViewHolder) {
        val query: Query = databaseReference.ref.orderByChild("title")
            .equalTo(holder.tvTitle.text.toString())
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
}