package com.ryz.coffee.user.fragment.order.tab.tab1

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R


class NotYetPaidFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var notYetPaidModel: ArrayList<NotYetPaidModel>
    private lateinit var notYetPaidAdapter: NotYetPaidAdapter

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_not_yet_paid, container, false)

        recyclerView = view.findViewById(R.id.rvNotYetPaid)

        // mengeset recyclerview menjadi layout default (HORIZONTAL)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mAuth = Firebase.auth

        notYetPaidModel = ArrayList()

        getNotYetPaidData()


        return view
    }

    private fun getNotYetPaidData() {
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notYetPaidModel.clear()
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val key = it.key.toString()
                        databaseReference.child(key).addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val address: String? =
                                    snapshot.child("Info").child("address")
                                        .getValue(String::class.java)
                                val paymentMethod: String? =
                                    snapshot.child("Info").child("paymentMethod")
                                        .getValue(String::class.java)
                                val statusOrder: String? =
                                    snapshot.child("Info").child("statusOrder")
                                        .getValue(String::class.java)
                                val subTotalDelivery: String? =
                                    snapshot.child("Info").child("subTotalDelivery")
                                        .getValue(String::class.java)
                                val subTotalProduct: String? =
                                    snapshot.child("Info").child("subTotalProduct")
                                        .getValue(String::class.java)
                                val totalPayment: String? =
                                    snapshot.child("Info").child("totalPayment")
                                        .getValue(String::class.java)
                                val emailUser: String? = snapshot.child("Info").child("email")
                                    .getValue(String::class.java)

                                for (ds in snapshot.children) {
                                    val kunci = ds.key.toString()
                                    Log.d("KUNCI", "${ds.key}")
                                    val count: String? =
                                        ds.child("count").getValue(String::class.java)
                                    val image: String? =
                                        ds.child("image").getValue(String::class.java)
                                    val title: String? =
                                        ds.child("title").getValue(String::class.java)
                                    val totalPrice: String? =
                                        ds.child("totalPrice").getValue(String::class.java)
                                    val orderId: String? =
                                        ds.child("orderId").getValue(String::class.java)

                                    val childrenCount: String = it.childrenCount.toString()
                                    Log.d("total", "$totalPayment")
                                    val cc = (childrenCount.toInt() - 1).toString()

                                    if (kunci != "Info") {
                                        if (statusOrder.toString() == "Belum bayar") {
                                            if (cc.toInt() > 1) {
                                                notYetPaidModel.add(NotYetPaidModel(title,
                                                    image,
                                                    count,
                                                    totalPrice,
                                                    orderId,
                                                    address,
                                                    paymentMethod,
                                                    statusOrder,
                                                    subTotalProduct,
                                                    subTotalDelivery,
                                                    totalPayment,
                                                    cc,emailUser))
                                                break
                                            } else {
                                                notYetPaidModel.add(NotYetPaidModel(title,
                                                    image,
                                                    count,
                                                    totalPrice,
                                                    orderId,
                                                    address,
                                                    paymentMethod,
                                                    statusOrder,
                                                    subTotalProduct,
                                                    subTotalDelivery,
                                                    totalPayment,
                                                    cc,emailUser))
                                            }
                                        }
                                    }
                                }
                                notYetPaidAdapter = NotYetPaidAdapter(notYetPaidModel)
                                //                  notYetPaidAdapter.listener = this@NotYetPaidFragment
                                notYetPaidAdapter.notifyDataSetChanged()
                                recyclerView.setHasFixedSize(true)
                                recyclerView.adapter = notYetPaidAdapter
                                recyclerView.isNestedScrollingEnabled = false
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                } else {
                    notYetPaidModel.clear()
                    notYetPaidAdapter = NotYetPaidAdapter(notYetPaidModel)
                    notYetPaidAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = notYetPaidAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

//    override fun onItemClick(view: View, notYetPaidModel: NotYetPaidModel) {
//        Toast.makeText(context, "$notYetPaidModel", Toast.LENGTH_SHORT).show()
//    }
}