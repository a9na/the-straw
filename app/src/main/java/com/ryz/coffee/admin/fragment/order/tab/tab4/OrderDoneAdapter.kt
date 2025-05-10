package com.ryz.coffee.admin.fragment.order.tab.tab4

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

class OrderDoneAdapter(private val orderDoneModel: ArrayList<OrderDoneModel>) :
    RecyclerView.Adapter<OrderDoneAdapter.ViewHolder>() {

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
        val orderDoneModelList = orderDoneModel[position]

        val imgUri: String? = orderDoneModelList.image
        Picasso.get().load(imgUri).into(holder.image)

        holder.title.text = orderDoneModelList.title
        holder.count.text = orderDoneModelList.count
        holder.price.text = orderDoneModelList.totalPrice
        holder.totalPayment.text = orderDoneModelList.totalPayment
        holder.childrenCount.text = orderDoneModelList.totalChildren
        holder.user.text = "Produk dari email : ${orderDoneModelList.email} telah diterima"
    }

    override fun getItemCount(): Int {
        return orderDoneModel.size
    }
}