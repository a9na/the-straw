package com.ryz.coffee.user.fragment.order.tab.tab2

import android.app.AlertDialog
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.squareup.picasso.Picasso


class PackedAdapter(private val packedModel: ArrayList<PackedModel>) :
    RecyclerView.Adapter<PackedAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var listener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun onItemClick(view: View, notYetPaidModel: PackedModel)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var tvCountChildren: TextView = itemView.findViewById(R.id.tvCountChildren)
        var tvTotalPayment: TextView = itemView.findViewById(R.id.tvTotalPayment)
        var tvInfo: TextView = itemView.findViewById(R.id.tvInfo)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_packed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")

        val packedList = packedModel[position]
        val imgUri: String? = packedList.image
        Picasso.get().load(imgUri).into(holder.imageCoffee)

        holder.tvTitle.text = packedList.title
        holder.tvCount.text = packedList.count
        holder.tvPrice.text = packedList.totalPrice
        holder.tvTotalPayment.text = packedList.totalPayment
        holder.tvCountChildren.text = packedList.totalChildren
        holder.tvInfo.text = "Produk Anda Sedang ${packedList.statusOrder}"
//        holder.cardViewClick.setOnClickListener {
//            listener?.onItemClick(it, notYetPaidList)
//        }
    }

    override fun getItemCount(): Int {
        return packedModel.size
    }
}