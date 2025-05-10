package com.ryz.coffee.user.fragment.order.tab.tab3

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


class SentAdapter(private val sentModel: ArrayList<SentModel>) :
    RecyclerView.Adapter<SentAdapter.ViewHolder>() {
    private lateinit var databaseReferenceUser: DatabaseReference
    private lateinit var databaseReferenceAdmin: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var listener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun onItemClick(view: View, notYetPaidModel: SentModel)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var tvCountChildren: TextView = itemView.findViewById(R.id.tvCountChildren)
        var tvTotalPayment: TextView = itemView.findViewById(R.id.tvTotalPayment)
        var btnSent: Button = itemView.findViewById(R.id.btnSent)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_sent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReferenceUser =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")
        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")

        val sentList = sentModel[position]
        val imgUri: String? = sentList.image
        Picasso.get().load(imgUri).into(holder.imageCoffee)

        holder.tvTitle.text = sentList.title
        holder.tvCount.text = sentList.count
        holder.tvPrice.text = sentList.totalPrice
        holder.tvTotalPayment.text = sentList.totalPayment
        holder.tvCountChildren.text = sentList.totalChildren
        holder.btnSent.setOnClickListener {
            databaseReferenceUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val key = ds.key.toString()
                            val orderId: String? =
                                ds.child("Info").child("orderId").getValue(String::class.java)
                            val emailUser: String? =
                                ds.child("Info").child("email").getValue(String::class.java)
                            if (orderId.toString() == sentList.orderId && emailUser.toString() == sentList.email) {
                                val map = HashMap<String, Any>()
                                map["statusOrder"] = "Selesai"
                                 databaseReferenceUser.child(key).child("Info").updateChildren(map)

                            }

                            databaseReferenceAdmin.child("$emailUser").addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.children.forEach { ds ->
                                            val key1 = ds.key.toString()
                                            val orderIdAdmin: String? =
                                                ds.child("Info").child("orderId")
                                                    .getValue(String::class.java)
                                            val emailAdmin: String? =
                                                ds.child("Info").child("email")
                                                    .getValue(String::class.java)
                                            Log.d("EmailAdmin", "$emailAdmin")

                                            if (emailAdmin.toString() == sentList.email
                                                && orderIdAdmin.toString() == sentList.orderId
                                            ) {
                                                val map = HashMap<String, Any>()
                                                map["statusOrder"] = "Selesai"
                                                databaseReferenceAdmin.child("$emailUser").child(key1).child("Info").updateChildren(map)
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

                override fun onCancelled(error: DatabaseError) {
                }
            })
            Toast.makeText(it.context, "Produk telah Anda terima", Toast.LENGTH_SHORT).show()
        }
//        holder.cardViewClick.setOnClickListener {
//            listener?.onItemClick(it, notYetPaidList)
//        }
    }

    override fun getItemCount(): Int {
        return sentModel.size
    }
}