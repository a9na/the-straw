package com.ryz.coffee.admin.fragment.order.tab.tab3

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ryz.coffee.R
import com.squareup.picasso.Picasso

class OrderSentAdapter(private val orderSentModel: ArrayList<OrderSentModel>) :
    RecyclerView.Adapter<OrderSentAdapter.ViewHolder>() {

    private lateinit var databaseReferenceAdmin: DatabaseReference
    private lateinit var databaseReferenceUser: DatabaseReference

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image: ImageView = itemView.findViewById(R.id.imgCoffee)
        var title: TextView = itemView.findViewById(R.id.tvTitle)
        var count: TextView = itemView.findViewById(R.id.tvCount)
        var price: TextView = itemView.findViewById(R.id.tvPrice)
        var childrenCount: TextView = itemView.findViewById(R.id.tvCountChildren)
        var totalPayment: TextView = itemView.findViewById(R.id.tvTotalPayment)
        var user: TextView = itemView.findViewById(R.id.tvUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_order_sent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference("User")
        val orderSentModelList = orderSentModel[position]

        val imgUri: String? = orderSentModelList.image
        Picasso.get().load(imgUri).into(holder.image)

        holder.title.text = orderSentModelList.title
        holder.count.text = orderSentModelList.count
        holder.price.text = orderSentModelList.totalPrice
        holder.totalPayment.text = orderSentModelList.totalPayment
        holder.childrenCount.text = orderSentModelList.totalChildren
        holder.user.text = "Produk dari email : ${orderSentModelList.email} telah dikirimkan"
    }

    override fun getItemCount(): Int {
        return orderSentModel.size
    }
}