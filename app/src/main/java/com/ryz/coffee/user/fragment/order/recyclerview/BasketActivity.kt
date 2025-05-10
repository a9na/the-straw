package com.ryz.coffee.user.fragment.order.recyclerview

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import atownsend.swipeopenhelper.SwipeOpenItemTouchHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class BasketActivity : AppCompatActivity(), BasketAdapter.ButtonCallbacks {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReferenceBasket: DatabaseReference
    private lateinit var databaseReferenceAddress: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    private lateinit var basketModel: ArrayList<BasketModel>
    private lateinit var basketAdapter: BasketAdapter

    private lateinit var imgBack: ImageView
    private lateinit var tvTtlPrice: TextView
    private lateinit var cardView: CardView
    private lateinit var btnCheckout: Button
    private lateinit var imgEmpty: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        recyclerView = findViewById(R.id.rvBasket)
        imgBack = findViewById(R.id.imgBack)
        tvTtlPrice = findViewById(R.id.tvTtlPrice)
        cardView = findViewById(R.id.cardView)
        btnCheckout = findViewById(R.id.btnCheckout)
        imgEmpty = findViewById(R.id.imgEmpty)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)

        mAuth = Firebase.auth

        recyclerView.layoutManager = LinearLayoutManager(this)
        basketModel = ArrayList()

        imgBack.setOnClickListener {
            finish()
        }

        btnCheckout.setOnClickListener {
            startActivity(Intent(this@BasketActivity, CheckoutActivity::class.java))
        }

        getDataBasket()
    }

    private fun getDataBasket() {
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReferenceBasket =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        databaseReferenceBasket.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    basketModel.clear()
                    snapshot.children.forEach {
                        val image: String? = it.child("image").getValue(String::class.java)
                        val title: String? = it.child("title").getValue(String::class.java)
                        val totalPrice: String? =
                            it.child("totalPrice").getValue(String::class.java)
                        val count: String? = it.child("count").getValue(String::class.java)
                        val price: String? = it.child("price").getValue(String::class.java)
                        val rating: String? = it.child("rating").getValue(String::class.java)
                        val category: String? =
                            it.child("category").getValue(String::class.java)
                        val check: String? = it.child("check").getValue(String::class.java)
                        val desc: String? = it.child("desc").getValue(String::class.java)

                        basketModel.add(BasketModel(count,
                            image,
                            totalPrice,
                            title,
                            price,
                            rating,
                            category,
                            check,
                            desc))

                        var ttl = 0
                        for (i in 0 until basketModel.size) {
                            if (basketModel[i].checked == "True") {
                                val splitPrice =
                                    basketModel[i].totalPrice.toString().split('.').joinToString("")
                                        .toInt()
                                ttl += splitPrice
                            }
                        }

                        tvTtlPrice.text = formatterCurrencyToIND(ttl)

                        var found = false
                        for (i in 0 until basketModel.size) {
                            if (basketModel[i].checked == "True") {
                                found = true
                            }
                        }
                        Log.d("Found1", "$found")
                        // jika tidak false
                        if (found) { // kondisi true
                            Log.d("Found2", "$found")
                            btnCheckout.setBackgroundColor(ContextCompat.getColor(this@BasketActivity,
                                R.color.primaryColor))
                            btnCheckout.setTextColor(Color.parseColor("#212121"))
                            btnCheckout.isEnabled = true
                        } else { // kondisi false
                            Log.d("Found3", "$found")
                            btnCheckout.setBackgroundColor(ContextCompat.getColor(this@BasketActivity,
                                R.color.grey))
                            btnCheckout.setTextColor(ContextCompat.getColor(this@BasketActivity,
                                R.color.white))
                            btnCheckout.isEnabled = false
                        }
                    }

                    basketAdapter = BasketAdapter(basketModel, this@BasketActivity)
                    val helper =
                        SwipeOpenItemTouchHelper(SwipeOpenItemTouchHelper.SimpleCallback(
                            SwipeOpenItemTouchHelper.START or SwipeOpenItemTouchHelper.END))
                    basketAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = basketAdapter
                    recyclerView.isNestedScrollingEnabled = false
                    helper.attachToRecyclerView(recyclerView)
                    helper.setCloseOnAction(true)

                } else {
                    imgEmpty.visibility = View.VISIBLE
                    tvTitle.visibility = View.VISIBLE
                    tvSubtitle.visibility = View.VISIBLE
                    cardView.visibility = View.INVISIBLE
                    basketModel.clear()
                    basketAdapter = BasketAdapter(basketModel, this@BasketActivity)
                    basketAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = basketAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun removePosition(pos: Int) {
        basketAdapter.removePosition(pos)
    }

    private fun formatterCurrencyToIND(price: Int): String {
        val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(price).replace("Rp", "")
            .replace(",00", "")
    }
}