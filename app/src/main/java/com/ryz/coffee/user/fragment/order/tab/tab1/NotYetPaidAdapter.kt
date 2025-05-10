package com.ryz.coffee.user.fragment.order.tab.tab1

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class NotYetPaidAdapter(private val notYetPaidModel: ArrayList<NotYetPaidModel>) :
    RecyclerView.Adapter<NotYetPaidAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferencePaymentTime: DatabaseReference
    private lateinit var databaseReferenceAdmin: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var listener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun onItemClick(view: View, notYetPaidModel: NotYetPaidModel)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var tvCountChildren: TextView = itemView.findViewById(R.id.tvCountChildren)
        var tvTotalPayment: TextView = itemView.findViewById(R.id.tvTotalPayment)
        var btnPay: Button = itemView.findViewById(R.id.btnPay)
        var tvTimer: TextView = itemView.findViewById(R.id.tvTimer)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_not_yet_paid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        val notYetPaidList = notYetPaidModel[position]

        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")
        databaseReferencePaymentTime =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")

        val imgUri: String? = notYetPaidList.image
        Picasso.get().load(imgUri).into(holder.imageCoffee)

        holder.tvTitle.text = notYetPaidList.title
        holder.tvCount.text = notYetPaidList.count
        holder.tvPrice.text = notYetPaidList.totalPrice
        holder.tvTotalPayment.text = notYetPaidList.totalPayment
        holder.tvCountChildren.text = notYetPaidList.totalChildren
//        holder.cardViewClick.setOnClickListener {
//            listener?.onItemClick(it, notYetPaidList)
//        }

        holder.btnPay.setOnClickListener {
            Log.d("TestButton", "Test")
        }

        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")

        /*databaseReferencePaymentTime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val key = it.key.toString()
                        databaseReferencePaymentTime.child(key)
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val paymentTime: String? =
                                            snapshot.child("Info").child("paymentTime")
                                                .getValue(String::class.java)
                                        val currentDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm",
                                            Locale.getDefault()).format(Date())
                                        val orderId: String? =
                                            snapshot.child("Info").child("orderId")
                                                .getValue(String::class.java)
                                        val emailUser: String? =
                                            snapshot.child("Info").child("email")
                                                .getValue(String::class.java)
                                        holder.tvTimer.text = "Bayar sebelum $paymentTime"

                                        if (paymentTime.equals(currentDateTime.toString())) {
                                            if (orderId.equals(notYetPaidList.orderId)  && emailUser.equals(notYetPaidList.email)) {
                                                val map = HashMap<String, Any>()
                                                map["statusOrder"] = "Dibatalkan"
                                                databaseReferencePaymentTime.child(key)
                                                    .child("Info")
                                                    .updateChildren(map)
                                            }
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

                                                        if (orderIdAdmin.equals(notYetPaidList.orderId)  && emailUser.equals(notYetPaidList.email)) {
                                                            val map = HashMap<String, Any>()
                                                            map["statusOrder"] = "Dibatalkan"
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

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/



        holder.btnPay.setOnClickListener { btn ->
            val dialogBuilder =
                AlertDialog.Builder(btn.context,
                    R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            val dialogView =
                LayoutInflater.from(btn.context)
                    .inflate(R.layout.dialog_custom_payment, null)
            dialogBuilder.setView(dialogView)
            val noPayment: TextView =
                dialogView.findViewById(R.id.tvNoRek)

            when (notYetPaidList.paymentMethod) {
                "Bank BRI" -> {
                    noPayment.text = "1234-02-482792-23-9"
                }
                "Bank Mandiri" -> {
                    noPayment.text = "567-12-2847239-1"
                }
                "Bank BNI" -> {
                    noPayment.text = "910-555-1431"
                }
            }
            dialogBuilder.setPositiveButton("Sudah bayar") { _, _ ->
                databaseReference.addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (ds in snapshot.children) {
                                    val key = ds.key.toString()
                                    val orderId: String? =
                                        ds.child("Info")
                                            .child("orderId")
                                            .getValue(String::class.java)
                                    val emailUser: String? =
                                        ds.child("Info").child("email").getValue(String::class.java)
                                    if (orderId.toString() == notYetPaidList.orderId && emailUser.toString() == notYetPaidList.email) {
                                        val map =
                                            HashMap<String, Any>()
                                        map["statusOrder"] =
                                            "Diproses"
                                        databaseReference.child(key)
                                            .child("Info")
                                            .updateChildren(map)
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

                                                    if (emailAdmin.toString() == notYetPaidList.email
                                                        && orderIdAdmin.toString() == notYetPaidList.orderId
                                                    ) {
                                                        val map = HashMap<String, Any>()
                                                        map["statusOrder"] = "Diproses"
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
                Toast.makeText(btn.context,
                    "Produk Anda sedang kami lakukan proses selanjutnya",
                    Toast.LENGTH_SHORT).show()
            }

            dialogBuilder.setNegativeButton("Belum bayar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return notYetPaidModel.size
    }
}