package com.ryz.coffee.user.fragment.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.favorite.recyclerview.FavoriteModel
import com.ryz.coffee.user.fragment.order.recyclerview.CheckoutActivity
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProductDetailActivity : AppCompatActivity() {

    // inisialisasi atribut dari layout
    private lateinit var imgCoffee: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvPrice: TextView
    private lateinit var imgBack: ImageView
    private lateinit var imgFavorite: ImageView
    private lateinit var frBasket: FrameLayout
    private lateinit var btnOrder: Button
    private lateinit var tvDesc: TextView

    // inisialisasi atribut untuk decrement atau increment jumlah item produk
    private lateinit var frPlus: FrameLayout
    private lateinit var tvCount: TextView
    private lateinit var frMinus: FrameLayout

    // inisialisasi untuk mengambil data dari array favorite
    private lateinit var favoriteModel: ArrayList<FavoriteModel>

    // inisialisasi untuk mengambil data dari firebase
    private lateinit var databaseReferenceFavorite: DatabaseReference
    private lateinit var databaseReferenceBasket: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // inisialisasi atribut dari layout home detail
        imgBack = findViewById(R.id.imgBack)
        imgCoffee = findViewById(R.id.imgCoffee)
        tvTitle = findViewById(R.id.tvTitle)
        tvRating = findViewById(R.id.tvRating)
        tvPrice = findViewById(R.id.tvPrice)
        tvDesc = findViewById(R.id.tvDesc)
        imgFavorite = findViewById(R.id.imgFavorite)
        frBasket = findViewById(R.id.frBasket)
        frPlus = findViewById(R.id.frPlus)
        tvCount = findViewById(R.id.tvCount)
        frMinus = findViewById(R.id.frMinus)
        btnOrder = findViewById(R.id.btnOrder)

        // inisialisasi model list
        favoriteModel = ArrayList()

        // mengambil data dari Home Adapter
        // mengambil nilai dengan menggunakan getString
        val imgCoffeeIntent: String? = intent.getStringExtra("image")
        val tvTitleIntent: String? = intent.getStringExtra("title")
        val tvRatingIntent: String? = intent.getStringExtra("rating")
        val tvPriceIntent: String? = intent.getStringExtra("price")
        val tvCategoryIntent: String? = intent.getStringExtra("category")
        val tvDescIntent: String? = intent.getStringExtra("desc")
        Log.d("Category", "$tvCategoryIntent")

        // mengeset output ketika value udah diambil
        Picasso.get().load(imgCoffeeIntent).into(imgCoffee)
        tvTitle.text = tvTitleIntent
        tvRating.text = tvRatingIntent
        tvPrice.text = tvPriceIntent
        tvDesc.text = tvDescIntent

        // onlick back ke home adapter
        imgBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            val intent = Intent(this@ProductDetailActivity, CheckoutActivity::class.java)
            intent.putExtra("image", imgCoffeeIntent.toString())
            intent.putExtra("title", tvTitle.text.toString())
            Log.d("TitleDetail", "${tvTitle.text}")
            intent.putExtra("totalPrice", formatterCurrencyToIND(tvPriceIntent.toString(), Integer.parseInt(tvCount.text.toString())))
            intent.putExtra("count", tvCount.text.toString())
            startActivity(intent)
        }

        getSetFavorite(imgCoffeeIntent.toString(), tvTitleIntent.toString(), tvRatingIntent.toString(), tvPriceIntent.toString(), tvCategoryIntent.toString(), tvDescIntent.toString())
        getCountProduct(imgCoffeeIntent.toString(), tvTitleIntent.toString(), tvRatingIntent.toString(), tvPriceIntent.toString(), tvCategoryIntent.toString(), tvDescIntent.toString())
    }

    private fun getSetFavorite(
        imageIntent: String,
        titleIntent: String,
        ratingIntent: String,
        priceIntent: String,
        categoryIntent: String,
        descIntent: String, ) {

        // inisialisasi auth
        mAuth = Firebase.auth

        // untuk mendapatkan email user
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference realtime database
        // untuk mendapatkan data produk yang udah menjadi favorite sebelumnya
        databaseReferenceFavorite =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductFavorite")

        // ========================= MENGESET ITEM FAVORITE ========================= //
        // kondisi awal bernilai true
        var checkedFavorite = true
        // onlick untuk mengeset item sebagai favorite atau tidak
        imgFavorite.setOnClickListener {
            // jika true maka set value check nya menjadi false
            // kondisi ini mengeset gambar favorite menjadi selected
            // selain itu, mengambil data dengan membandingkan title yang sudah terdapat sebelumnya pada realtime database
            // jika title tersebut sudah ada maka tidak usah dimasukan sebagai data baru
            // jika title tersebut tidak ada maka tambahkan data baru dengan value yang sudah ditentukan
            checkedFavorite = if (checkedFavorite) {
                imgFavorite.setImageResource(R.drawable.like_selected_icon)
                val query: Query =
                    databaseReferenceFavorite.ref.orderByChild("title").equalTo(titleIntent)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            val map = HashMap<String, Any>()
                            map["image"] = imageIntent
                            map["title"] = titleIntent
                            map["rating"] = ratingIntent
                            map["price"] = priceIntent
                            map["category"] = categoryIntent
                            map["desc"] = descIntent
                            databaseReferenceFavorite.push().setValue(map)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                Toast.makeText(it.context, "Item telah ditambahkan ke daftar favorit anda", Toast.LENGTH_SHORT).show()
                false
            } else {
                // jika false maka set value check bernilai true
                // mengeset gambar favorite menjadi unselected
                // jika title tersebut terdapat dalam realtime database maka dilakukan proses hapus child yang bersangkutan
                imgFavorite.setImageResource(R.drawable.like_unselected_icon)
                val query: Query =
                    databaseReferenceFavorite.ref.orderByChild("title").equalTo(titleIntent)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            dataSnapshot.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                Toast.makeText(it.context, "Item telah dihapus dari daftar favorit anda", Toast.LENGTH_SHORT).show()
                true
            }
        }

        // addValueEvenetListener berguna untuk menyimpan data yang udah terdapat dalam realtime database
        // jika memasukan data baru menggunakan fungsi ini maka akan terjadi laoping
        // namun ketika menggunakan ListenerForSingleValueEvent ketika activity di finish maka data tersebut hilang
        // jadi diperlukan method ini untuk menghold atau menyimpan data ketika activity tersebut di finish
        // dnegan mengambil data title yang udah ada didalam database tersebut
        // dibandingkan value nya dengan title yang value nya dikirim dari home Adapter
        // jika sama maka set gambar menjadi selected dan set check menjadi false
        databaseReferenceFavorite.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val title: String? = dataSnapshot.child("title").getValue(String::class.java)
                    if (title!!.contains(titleIntent)) {
                        imgFavorite.setImageResource(R.drawable.like_selected_icon)
                        checkedFavorite = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // ========================= END MENGESET ITEM FAVORITE ========================= //
    }

    private fun getCountProduct(
        imageIntent: String,
        titleIntent: String,
        ratingIntent: String,
        priceIntent: String,
        categoryIntent: String,
        descIntent: String
    ) {
        // inisialisasi auth
        mAuth = Firebase.auth

        // untuk mendapatkan email user
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference realtime database
        // untuk menyimpan data produk yang di masukan ke dalam keranjang
        databaseReferenceBasket =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        // ========================= MENGESET COUNT ITEM PRODUCT ========================= //

        // inisialisasi counter dengan nilai awal 1
        var counterTemp = 1
        // set text count awal sebagai nilai dari counter
        tvCount.text = counterTemp.toString()

        // onclick tombol minus
        frMinus.setOnClickListener {
            // jika counter sudah bernilai kurang dari atau sama dengan 1 maka set nilai counter nya tetap menjadi 1
            // set text count nya menjadi countertemp
            // set text price item sesuai dengan jumlah countnya
            if (counterTemp <= 1) {
                counterTemp = 1
                tvCount.text = counterTemp.toString()
                tvPrice.text = formatterCurrencyToIND(priceIntent, counterTemp)
            } else {
                // jika kondisi diatas tidak terpenuhi
                // ketika count lebih dari 1 maka dapat dilakukan kalkulasi dengan mengurangkannya dengan -1
                // set text count nya menjadi countertemp
                // set text price item sesuai dengan jumlah countnya
                counterTemp -= 1
                tvCount.text = counterTemp.toString()
                tvPrice.text = formatterCurrencyToIND(priceIntent, counterTemp)
            }
        }

        // onclick tombol plus
        frPlus.setOnClickListener {
            // ketika di tekan maka counter nya +1
            // set text count nya menjadi countertemp
            // set text price item sesuai dengan jumlah countnya
            counterTemp += 1
            tvCount.text = counterTemp.toString()
            tvPrice.text = formatterCurrencyToIND(priceIntent, counterTemp)
        }

        // inisialisasi nilai count sama dengan nilai text pada count tersebut
        var count = Integer.parseInt(tvCount.text.toString())
        Log.d("CountDetail", "$count")

        // ketika item dimasukan ke dalam keranjang
        // dibandingkan terlebih dahulu title tersebut apakah sudah terdapat dalam realtime database atau belum
        // jika title tersebut sudah ada maka hanya update nilai count dan price nya saja
        // jika belum lakukan push data yang sudah ditentukan
        frBasket.setOnClickListener {
            val query: Query =
                databaseReferenceBasket.ref.orderByChild("title").equalTo(titleIntent)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        count = Integer.parseInt(tvCount.text.toString())
                        val map = HashMap<String, Any>()
                        map["title"] = titleIntent
                        map["totalPrice"] = formatterCurrencyToIND(priceIntent, count)
                        map["image"] = imageIntent
                        map["count"] = count.toString()
                        map["price"] = priceIntent
                        map["rating"] = ratingIntent
                        map["category"] = categoryIntent
                        map["desc"] = descIntent
                        map["check"] = "False"
                        databaseReferenceBasket.push().setValue(map)
                        Toast.makeText(this@ProductDetailActivity,
                            "Item telah ditambahkan ke daftar keranjang anda",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        snapshot.children.forEach {
                            count += Integer.parseInt(tvCount.text.toString())
                            val key = it.ref.key.toString()
                            val map = HashMap<String, Any>()
                            map["count"] = count.toString()
                            map["totalPrice"] = formatterCurrencyToIND(priceIntent, count)
                            databaseReferenceBasket.child(key).updateChildren(map)
                            Toast.makeText(this@ProductDetailActivity,
                                "Jumlah produk ini telah di update di keranjang anda",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        // addValueEventListener untuk ngehold value ketika activity di finish()
        // addListenerForSingleValueEvent untuk mempush data namun tidak terjadi looping secara terus menerus
        // namun ketika activity di finish() maka valuenya akan kembali seperti sebelumnya
        // mengambil data title nya
        // ketika titlenya sama dengan yang di pilih maka nilai count nya sama dengan nilai count yang sudah ada di database tersebut
        // misal count awal 3
        // ketika ditambahkan lagi item yang sama ke dalam keranjang
        // maka count yang awalnya 3 ditambah dengan count yang sesuai dengan jumlah count item yang akan dimasukan lagi ke dalam keranjang
        // sehingga count tidak diulang dari 1 lagi
        databaseReferenceBasket.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val counter: String? = it.child("count").getValue(String::class.java)
                        val title: String? = it.child("title").getValue(String::class.java)

                        if (title?.contains(titleIntent)!!) {
                            count = counter?.toInt()!!
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        // ========================= END MENGESET COUNT ITEM PRODUCT ========================= //
    }

    // ========================= KONVERSI MATA UANG ========================= //

    // sumber stackoverflow
    // konversi digit mata uang menjadi K, M, B, T dibelakangnya
    /*private val c = charArrayOf('K', 'M', 'B', 'T')

    private fun formatterCurrency(price: Int, iteration: Int): String? {
        val calc = price.toLong() / 100 / 10.0
        val round = calc * 10 % 10 == 0.0
        return (if (calc < 1000)
            (if (calc > 99.9 || round || !round && calc > 9.99)
                calc.toInt() * 10 / 10 else calc.toString() + ""
                    ).toString() + "" + c[iteration] else formatterCurrency(calc.toInt(),
            iteration + 1))
    }*/

    // method untuk mengkonversi currency ke dalam indonesia
    // namun ketika dilakukan konversi ke dalam bentuk IND terdapat RP dan ,00 di belakangnya
    // method ini untuk menghilangkan hal tersebut
    private fun formatterCurrencyToIND(textSplit: String, count: Int): String {
        val splitPrice = textSplit.split('.').joinToString("")
        val price = Integer.parseInt(splitPrice) * count
        val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(price).replace("Rp", "")
            .replace(",00", "")
    }

    // ========================= END KONVERSI MATA UANG ========================= //
}