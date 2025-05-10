package com.ryz.coffee.admin.fragment.order.tab.tab2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ryz.coffee.R

class OrderPacked : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var orderPackedModel: ArrayList<OrderPackedModel>
    private lateinit var orderPackedAdapter: OrderPackedAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_packed, container, false)

        recyclerView = view.findViewById(R.id.rvOrderPacked)

        recyclerView.layoutManager = LinearLayoutManager(context)

        orderPackedModel = ArrayList()

        getDataOrder()

        return view
    }

    private fun getDataOrder() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderPackedModel.clear()
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val key1 = it.key.toString()
                        Log.d("Key1", key1)
                        if (it.exists()) {
                            it.children.forEach { ds ->
                                val key2 = ds.key.toString()
                                Log.d("Key2", key2)
                                databaseReference.child(key1).child(key2).addValueEventListener(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val email: String? = snapshot.child("Info").child("email")
                                            .getValue(String::class.java)
                                        Log.d("Email", "$email")
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

                                        for (dataSnapshot in snapshot.children) {
                                            val kunci = dataSnapshot.key.toString()
                                            val count: String? =
                                                dataSnapshot.child("count").getValue(String::class.java)
                                            val image: String? =
                                                dataSnapshot.child("image").getValue(String::class.java)
                                            val title: String? =
                                                dataSnapshot.child("title").getValue(String::class.java)
                                            Log.d("TitleAlready", "$title")
                                            val totalPrice: String? =
                                                dataSnapshot.child("totalPrice").getValue(String::class.java)
                                            val orderId: String? =
                                                dataSnapshot.child("orderId").getValue(String::class.java)

                                            val childrenCount: String = ds.childrenCount.toString()
                                            Log.d("ChildrenCount", childrenCount)
                                            val cc = (childrenCount.toInt() - 1).toString()

                                            if (kunci != "Info") {
                                                if (statusOrder.toString() == "Dikemas") {
                                                    if (cc.toInt() > 1) {
                                                        orderPackedModel.add(OrderPackedModel(email,
                                                            count,
                                                            image,
                                                            orderId,
                                                            title,
                                                            totalPrice,
                                                            address,
                                                            paymentMethod,
                                                            statusOrder,
                                                            subTotalDelivery,
                                                            subTotalProduct,
                                                            totalPayment,
                                                            cc))
                                                        break
                                                    } else {
                                                        orderPackedModel.add(OrderPackedModel(email,
                                                            count,
                                                            image,
                                                            orderId,
                                                            title,
                                                            totalPrice,
                                                            address,
                                                            paymentMethod,
                                                            statusOrder,
                                                            subTotalDelivery,
                                                            subTotalProduct,
                                                            totalPayment,
                                                            cc))
                                                    }
                                                }
                                            }
                                        }
                                        orderPackedAdapter = OrderPackedAdapter(orderPackedModel)
                                        orderPackedAdapter.notifyDataSetChanged()
                                        recyclerView.setHasFixedSize(true)
                                        recyclerView.adapter = orderPackedAdapter
                                        recyclerView.isNestedScrollingEnabled = false
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
    }
}