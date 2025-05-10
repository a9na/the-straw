package com.ryz.coffee.user.fragment.order.recyclerview

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.user.UserActivity
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.profile.AddressActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CheckoutActivity : AppCompatActivity() {

    // inisialisasi variabel untuk mendapatkan dari firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReferenceBasket: DatabaseReference
    private lateinit var databaseReferenceAddress: DatabaseReference
    private lateinit var databaseReferenceOrder: DatabaseReference
    private lateinit var databaseReferenceAdmin: DatabaseReference

    // inisialisasi adapter dan model untuk menampilkan data ke dalam recyclerview
    private lateinit var checkoutModel: ArrayList<CheckoutModel>
    private lateinit var checkoutAdapter: CheckoutAdapter

    // inisialisasi atribut dari layout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvSubtotalProduct: TextView
    private lateinit var tvSubtotalDelivery: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var imgBack: ImageView
    private lateinit var tvAddress: TextView
    private lateinit var edAddress: EditText
    private lateinit var tvEdit: TextView
    private lateinit var spinnerMethodPayment: Spinner
    private lateinit var btnOrder: Button
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // mendapatkan id dari layout tersebut
        recyclerView = findViewById(R.id.rvOrder)
        tvSubtotalProduct = findViewById(R.id.tvSubtotalProduct)
        tvSubtotalDelivery = findViewById(R.id.tvSubTotalDelivery)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        tvTotalPayment = findViewById(R.id.tvTotalPayment)
        imgBack = findViewById(R.id.imgBack)
        tvAddress = findViewById(R.id.tvAddress)
        edAddress = findViewById(R.id.edAddress)
        tvEdit = findViewById(R.id.tvEdit)
        spinnerMethodPayment = findViewById(R.id.spinnerMethodPayment)
        btnOrder = findViewById(R.id.btnOrder)
        cardView = findViewById(R.id.cardView)

        val listBank = arrayOf("Pilih metode pembayaran", "Bank BRI", "Bank BNI", "Bank Mandiri")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listBank)
        spinnerMethodPayment.adapter = arrayAdapter
        spinnerMethodPayment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerMethodPayment.getItemAtPosition(spinnerMethodPayment.selectedItemPosition)
                        .toString() == "Pilih metode pembayaran"
                ) {
                    Log.d("Spinner", "Test")
                    btnOrder.setBackgroundColor(ContextCompat.getColor(this@CheckoutActivity,
                        R.color.grey))
                    btnOrder.setTextColor(ContextCompat.getColor(this@CheckoutActivity,
                        R.color.white))
                    btnOrder.isEnabled = false
                } else {
                    btnOrder.setBackgroundColor(ContextCompat.getColor(this@CheckoutActivity,
                        R.color.primaryColor))
                    btnOrder.setTextColor(Color.parseColor("#212121"))
                    btnOrder.isEnabled = true
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        imgBack.setOnClickListener {
            finish()
        }

        tvEdit.setOnClickListener {
            startActivity(Intent(this@CheckoutActivity, AddressActivity::class.java))
        }

        // inisialisasi awal var mAuth
        mAuth = Firebase.auth

        // mengeset recyclerview menjadi layout default (HORIZONTAL)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // inisialisasi model
        checkoutModel = ArrayList()
        // mendapatkan data dari item produk

        getAddress(object : MyCallback {
            override fun onCallback(delivery: Int) {
                getDataItem(delivery)
            }
        })
    }


    private fun getAddress(myCallback: MyCallback) {
        // inisialisasi untuk mendapatkan email dari user yang sedang login
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference dari database
        databaseReferenceAddress =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")


        databaseReferenceAddress.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkoutModel.clear()
                if (snapshot.hasChild("address")) {
                    val address: String? = snapshot.child("address").getValue(String::class.java)
                    edAddress.setText(address).toString()
                    edAddress.isEnabled = false
                    tvSubtotalDelivery.text = "50.000"
                    myCallback.onCallback(tvSubtotalDelivery.text.toString().split('.')
                        .joinToString("").toInt())
                } else {
                    val builder = AlertDialog.Builder(this@CheckoutActivity)
                        .setTitle("Info")
                        .setMessage("Harap mengatur alamat terlebih dahulu sebelum melakukan pemesanan\n\nIngin mengatur alamat sekarang?")
                        .setPositiveButton("Iya") { _, _ ->
                            startActivity(Intent(this@CheckoutActivity,
                                AddressActivity::class.java))
                        }
                        .setNegativeButton("Tidak") { _, _ ->
                            finish()
                        }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                    tvSubtotalDelivery.text = "0"
                    myCallback.onCallback(tvSubtotalDelivery.text.toString().split('.')
                        .joinToString("").toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getDataItem(delivery: Int) {

        // inisialisasi untuk mendapatkan email dari user yang sedang login
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference dari database
        databaseReferenceBasket =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        // pass data dari product detail activity //
        val imgIntent: String? = intent.getStringExtra("image")
        val titleIntent: String? = intent.getStringExtra("title")
        val totalPriceIntent: String? = intent.getStringExtra("totalPrice")
        val countIntent: String? = intent.getStringExtra("count")

        Log.d("Atribut",
            "$imgIntent. $titleIntent, $totalPriceIntent, $countIntent")
        ////////////////

        /*
         jika data dari imgIntent hasilnya adalah null
         maka tampilkan data yang berasal dari basket activity
         */
        if (imgIntent == null) {
            // listener untuk membaca data dari firebase
            databaseReferenceBasket.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    checkoutModel.clear()
                    // jika data tidak kosong
                    if (snapshot.exists()) {
                        snapshot.children.forEach {
                            // mengambil data dari firebase
                            val image: String? = it.child("image").getValue(String::class.java)
                            val title: String? = it.child("title").getValue(String::class.java)
                            val totalPrice: String? =
                                it.child("totalPrice").getValue(String::class.java)
                            val count: String? = it.child("count").getValue(String::class.java)
                            val check: String? = it.child("check").getValue(String::class.java)
                            //////////////////

                            /*
                            kondisi untuk menampilkan data ke dalam recyclerview
                            jika kondisi check yang diambil dari firebase bernilai "True"
                             */
                            if (check == "True") {
                                // tampilkan data ke dalam recyclerview
                                checkoutModel.add(CheckoutModel(title,
                                    totalPrice,
                                    count,
                                    image,
                                    check))

                                /*
                                 kalkulasi untuk mendapatkan hasil untuk total payment
                                 hasil kalkulasi tergantung dari hasil item/produk yang memiliki value check = "True"
                                 */
                                var ttl = 0 // var untuk menampung hasil total payment
                                // dilakukan looping dengna menghitung panjang array tersebut
                                for (i in 0 until checkoutModel.size) {
                                    // dilakukan kondisi jika atribut checked pada array tersebut bernilai "True"
                                    if (checkoutModel[i].checked == "True") {
                                        /*
                                         split value yang awalnya berupa string menjadi ke integer
                                         misal : 120.000 -> 120000
                                         */
                                        val splitPrice =
                                            checkoutModel[i].totalPrice.toString().split('.')
                                                .joinToString("")
                                                .toInt()
                                        /*
                                         hasil split di jumlah sesuai panjangnya array
                                         misal :
                                         ttl = ttl -> memiliki value 0 ditambah dengan harga item tersebut
                                         0 = 0 + 1200000
                                         120000 = 120000 + 150000
                                         */
                                        ttl += splitPrice
                                    }
                                }
                                // tampilkan total tersebut ke dalam textview total payment
                                tvSubtotalProduct.text = formatterCurrencyToIDR(ttl)
                                val result = delivery + ttl
                                tvTotalPrice.text = formatterCurrencyToIDR(result)
                                tvTotalPayment.text = formatterCurrencyToIDR(result)
                            }
                            pushDataModel()
                        }
                    }
                    // menampilkan recyclerview dengna mengambil data dari model
                    checkoutAdapter = CheckoutAdapter(checkoutModel)
                    // ketika mengalami perubahan ukuran
                    checkoutAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    // tampilkan recyclerview adapter
                    recyclerView.adapter = checkoutAdapter
                    // agar scrollingnya smooth
                    recyclerView.isNestedScrollingEnabled = false
                    // fungsi untuk kalkulasi mendapatkan ukuran divider / jarak antar item produk
                    recyclerView.addItemDecoration(DividerItemDecoration(this@CheckoutActivity,
                        DividerItemDecoration.VERTICAL).also {
                        with(ShapeDrawable(RectShape())) {
                            intrinsicHeight = (resources.displayMetrics.density * 1).toInt()
                            alpha = 0
                            it.setDrawable(this)
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
//
//            kondisi ini jika hasil imgIntent tidak null
//            jadi ketika di klik tombol pesan sekarang maka data yang di kirimkan akan masuk ke dalam kondisi ini
//

//             masukan data ke dalam array
//             dengnan memberikan nilai false pada atribut array checkedItem
//             maka data item yang sudah di check / check bernilai "True" pada menu keranjang
//             tidak bentrok datanya ketika tombol pesan sekarang di klik
            checkoutModel.add(CheckoutModel(titleIntent,
                totalPriceIntent,
                countIntent,
                imgIntent,
                "False"))

            // menampilkan recyclerview dengna mengambil data dari model
            checkoutAdapter = CheckoutAdapter(checkoutModel)
            // ketika mengalami perubahan ukuran
            checkoutAdapter.notifyDataSetChanged()
            recyclerView.setHasFixedSize(true)
            // tampilkan recyclerview adapter
            recyclerView.adapter = checkoutAdapter
            // agar scrollingnya smooth
            recyclerView.isNestedScrollingEnabled = false
            // fungsi untuk kalkulasi mendapatkan ukuran divider / jarak antar item produk
            recyclerView.addItemDecoration(DividerItemDecoration(this@CheckoutActivity,
                DividerItemDecoration.VERTICAL).also {
                with(ShapeDrawable(RectShape())) {
                    intrinsicHeight = (resources.displayMetrics.density * 1).toInt()
                    alpha = 0
                    it.setDrawable(this)
                }
            })
            // menampilkan total payment sesuai dengan total price yang dikirimkan
            tvSubtotalProduct.text = totalPriceIntent.toString()

            val ttl = tvSubtotalProduct.text.toString().split('.').joinToString("").toInt()
            val result = delivery + ttl
            tvTotalPrice.text = formatterCurrencyToIDR(result)
            tvTotalPayment.text = formatterCurrencyToIDR(result)

            pushDataIntent(titleIntent, totalPriceIntent, countIntent, imgIntent)
        }
    }

    // method untuk memformat harga item ke dalam bentuk IDR
    private fun formatterCurrencyToIDR(price: Int): String {
        val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(price).replace("Rp", "")
            .replace(",00", "")
    }

    private fun pushDataIntent(
        titleIntent: String?,
        totalPriceIntent: String?,
        countIntent: String?,
        imgIntent: String?,
    ) {
        // inisialisasi untuk mendapatkan email dari user yang sedang login
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference dari database
        databaseReferenceOrder =
            FirebaseDatabase.getInstance()
                .getReference("User/$cleanEmail/ProductOrder")

        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")

        var a = 0
        databaseReferenceOrder.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    a = snapshot.childrenCount.toInt()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        btnOrder.setOnClickListener {
            val map = HashMap<String, Any>()
            val m = HashMap<String, Any>()
            val intent = Intent(this@CheckoutActivity, UserActivity::class.java)
            intent.putExtra("selected", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val now: Calendar = Calendar.getInstance()
            now.add(Calendar.MINUTE, 30)

            val df = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val time: String = df.format(now.time)

            map["orderId"] = "${a + 1}"
            map["title"] = titleIntent.toString()
            map["totalPrice"] = totalPriceIntent.toString()
            map["count"] = countIntent.toString()
            map["image"] = imgIntent.toString()
            databaseReferenceOrder.child("Order ke ${a + 1}").push().setValue(map)
            databaseReferenceAdmin.child(cleanEmail).child("Order ke ${a + 1}").push().setValue(map)

            m["email"] = cleanEmail
            m["paymentTime"] = time
            m["orderId"] = "${a + 1}"
            m["address"] = edAddress.text.toString()
            m["statusOrder"] = "Belum bayar"
            m["paymentMethod"] = spinnerMethodPayment.selectedItem.toString()
            m["subTotalProduct"] = tvSubtotalProduct.text.toString()
            m["subTotalDelivery"] = tvSubtotalDelivery.text.toString()
            m["totalPayment"] = tvTotalPayment.text.toString()
            databaseReferenceOrder.child("Order ke ${a + 1}").child("Info").setValue(m)
            databaseReferenceAdmin.child(cleanEmail).child("Order ke ${a + 1}").child("Info").setValue(m)

            startActivity(intent)
        }
    }

    private fun pushDataModel() {
        // inisialisasi untuk mendapatkan email dari user yang sedang login
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference dari database
        databaseReferenceOrder =
            FirebaseDatabase.getInstance()
                .getReference("User/$cleanEmail/ProductOrder")
        databaseReferenceAdmin = FirebaseDatabase.getInstance().getReference("Admin/ProductOrder")

        var a = 0
        databaseReferenceOrder.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    a = snapshot.childrenCount.toInt()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        btnOrder.setOnClickListener {
            val map = HashMap<String, Any>()
            val m = HashMap<String, Any>()
            val intent = Intent(this@CheckoutActivity, UserActivity::class.java)
            intent.putExtra("selected", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val now: Calendar = Calendar.getInstance()
            now.add(Calendar.MINUTE, 30)

            val df = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val time: String = df.format(now.time)

            for (i in 0 until checkoutModel.size) {
                if (checkoutModel[i].checked == "True") {
                    map["orderId"] = "${a + 1}"
                    map["title"] = checkoutModel[i].title.toString()
                    map["totalPrice"] = checkoutModel[i].totalPrice.toString()
                    map["count"] = checkoutModel[i].count.toString()
                    map["image"] = checkoutModel[i].image.toString()
                    removeBasket(checkoutModel[i].title.toString())
                }
                databaseReferenceOrder.child("Order ke ${a + 1}").push().setValue(map)
                databaseReferenceAdmin.child(cleanEmail).child("Order ke ${a + 1}").push().setValue(map)
            }
            m["email"] = cleanEmail
            m["paymentTime"] = time
            m["orderId"] = "${a + 1}"
            m["address"] = edAddress.text.toString()
            m["statusOrder"] = "Belum bayar"
            m["paymentMethod"] = spinnerMethodPayment.selectedItem.toString()
            m["subTotalProduct"] = tvSubtotalProduct.text.toString()
            m["subTotalDelivery"] = tvSubtotalDelivery.text.toString()
            m["totalPayment"] = tvTotalPayment.text.toString()
            databaseReferenceOrder.child("Order ke ${a + 1}").child("Info").setValue(m)
            databaseReferenceAdmin.child(cleanEmail).child("Order ke ${a + 1}").child("Info").setValue(m)
            startActivity(intent)
        }
    }

    private fun removeBasket(title: String) {
        val query: Query = databaseReferenceBasket.ref.orderByChild("title")
            .equalTo(title)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    ds.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

interface MyCallback {
    fun onCallback(delivery: Int)
}



