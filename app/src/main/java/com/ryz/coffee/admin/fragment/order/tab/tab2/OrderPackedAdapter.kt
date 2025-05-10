package com.ryz.coffee.admin.fragment.order.tab.tab2

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

class OrderPackedAdapter(private val orderPackedModel: ArrayList<OrderPackedModel>) :
    RecyclerView.Adapter<OrderPackedAdapter.ViewHolder>() {

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
        var btnProcess: Button = itemView.findViewById(R.id.btnProcess)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_order_packed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference("User")
        val orderPackedModelList = orderPackedModel[position]

        val imgUri: String? = orderPackedModelList.image
        Picasso.get().load(imgUri).into(holder.image)

        holder.title.text = orderPackedModelList.title
        holder.count.text = orderPackedModelList.count
        holder.price.text = orderPackedModelList.totalPrice
        holder.totalPayment.text = orderPackedModelList.totalPayment
        holder.childrenCount.text = orderPackedModelList.totalChildren
        holder.user.text = "Produk dari email : ${orderPackedModelList.email}"
        holder.btnProcess.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
                .setTitle("Proses")
                .setMessage("Apakah Anda yakin untuk memproses barang ini?")
                .setPositiveButton("Iya") { _, _ ->

                    databaseReferenceAdmin.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.children.forEach { ds ->
                                    val key1 = ds.key.toString()
                                    if (ds.exists()) {
                                        ds.children.forEach { dataSnapshot ->
                                            val key2 = dataSnapshot.key.toString()
                                            val orderId: String? =
                                                dataSnapshot.child("Info").child("orderId")
                                                    .getValue(String::class.java)
                                            val email: String? =
                                                dataSnapshot.child("Info").child("email")
                                                    .getValue(String::class.java)

                                            if (email.toString() == orderPackedModelList.email
                                                && orderId.toString() == orderPackedModelList.orderId
                                            ) {
                                                val map = HashMap<String, Any>()
                                                map["statusOrder"] = "Dikirim"
                                                databaseReferenceAdmin.child(key1).child(key2)
                                                    .child("Info").updateChildren(map)
                                            }

                                            databaseReferenceUser.child("$email/ProductOrder")
                                                .addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                for (dsUser in snapshot.children) {
                                                                    val key = dsUser.key.toString()
                                                                    val orderIdUser: String? =
                                                                        dsUser.child("Info")
                                                                            .child("orderId")
                                                                            .getValue(String::class.java)
                                                                    val emailUser: String? =
                                                                        dsUser.child("Info")
                                                                            .child("email")
                                                                            .getValue(String::class.java)
                                                                    if (emailUser.toString() == orderPackedModelList.email
                                                                        && orderIdUser.toString() == orderPackedModelList.orderId
                                                                    ) {
                                                                        val map =
                                                                            HashMap<String, Any>()
                                                                        map["statusOrder"] =
                                                                            "Dikirim"
                                                                        databaseReferenceUser.child(
                                                                            "$email/ProductOrder")
                                                                            .child(key)
                                                                            .child("Info")
                                                                            .updateChildren(map)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                        }
                                                    })
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                    Toast.makeText(it.context,
                        "Yay, Anda telah melakukan proses pengemasan",
                        Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return orderPackedModel.size
    }
}