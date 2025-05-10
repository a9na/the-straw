package com.ryz.coffee.user.fragment.order.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class CheckoutAdapter(private val checkoutModel: ArrayList<CheckoutModel>) :
    RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    private lateinit var databaseReferenceOrder: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvTtlPrice: TextView = itemView.findViewById(R.id.tvTtlPrice)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var imgCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_checkout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mAuth = Firebase.auth

        // untuk mendapatkan email user
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference realtime database
        // untuk mendapatkan data produk yang udah menjadi favorite sebelumnya
        databaseReferenceOrder =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        val orderList = checkoutModel[position]
        val imgUri: String? = orderList.image
        Picasso.get().load(imgUri).into(holder.imgCoffee)

        holder.tvTitle.text = orderList.title
        holder.tvTtlPrice.text = orderList.totalPrice
        holder.tvCount.text = orderList.count

        val counter = Integer.parseInt(holder.tvCount.text.toString())

        holder.tvCount.text = counter.toString()

    }

    override fun getItemCount(): Int {
        return checkoutModel.size
    }
}