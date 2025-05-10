package com.ryz.coffee.user.fragment.order.recyclerview

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import atownsend.swipeopenhelper.BaseSwipeOpenViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.order.ProductDetailActivity
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

class BasketAdapter(
    private var basketModel: ArrayList<BasketModel>,
    private var callback: ButtonCallbacks,
) :
    RecyclerView.Adapter<BasketAdapter.ViewHolder>() {

    interface ButtonCallbacks {
        fun removePosition(pos: Int)
    }

    private lateinit var databaseReferenceBasket: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(itemView: View) : BaseSwipeOpenViewHolder(itemView) {
        var imgCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvTtlPrice: TextView = itemView.findViewById(R.id.tvTtlPrice)
        var frPlus: FrameLayout = itemView.findViewById(R.id.frPlus)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var frMinus: FrameLayout = itemView.findViewById(R.id.frMinus)
        var parentLayout: CardView = itemView.findViewById(R.id.parentLayout)
        var tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        var checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        override fun getSwipeView(): View {
            return parentLayout
        }

        override fun getEndHiddenViewSize(): Float {
            return tvDelete.measuredWidth.toFloat()
        }

        override fun getStartHiddenViewSize(): Float {
            return 0.0.toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_basket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        // untuk mendapatkan email user
        val email = mAuth.currentUser!!.email

        // untuk menghilangkan titik pada email
        // firebase tidak bisa diberikan nama child apabila terdapat titik
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        // inisialisasi reference realtime database
        // untuk mendapatkan data produk yang udah menjadi favorite sebelumnya
        databaseReferenceBasket =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductBasket")

        val basketModelList = basketModel[position]
        val imgUri: String? = basketModelList.image
        Picasso.get().load(imgUri).into(holder.imgCoffee)

        holder.tvTitle.text = basketModelList.title
        holder.tvTtlPrice.text = basketModelList.totalPrice
        holder.tvCount.text = basketModelList.count

        var counter = Integer.parseInt(holder.tvCount.text.toString())
        val splitPrice = holder.tvTtlPrice.text.toString().split('.').joinToString("")
        val price = Integer.parseInt(splitPrice) / counter
        Log.d("Price", "$price")

        holder.frPlus.setOnClickListener {
            counter += 1
            holder.tvCount.text = counter.toString()
            holder.tvTtlPrice.text = formatterCurrencyToIND(price.toString(), counter)
            updateCount(holder.tvTitle.text.toString(), price, counter)
        }

        holder.frMinus.setOnClickListener {
            if (counter <= 1) {
                val builder = AlertDialog.Builder(it.context)
                    .setTitle("Hapus")
                    .setMessage("Apakah Anda yakin untuk menghapus?")
                    .setPositiveButton("Iya") { _, _ ->
                        removeBasket(holder.tvTitle.text.toString())
                        Toast.makeText(it.context, "Item telah dihapus dari daftar keranjang anda", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Tidak") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            } else {
                counter -= 1
                holder.tvCount.text = counter.toString()
                holder.tvTtlPrice.text = formatterCurrencyToIND(price.toString(), counter)
                updateCount(holder.tvTitle.text.toString(), price, counter)
            }
        }

        holder.parentLayout.setOnClickListener {
            val intent = Intent(it.context, ProductDetailActivity::class.java)
            intent.putExtra("image", imgUri.toString())
            intent.putExtra("title", basketModelList.title)
            intent.putExtra("price", basketModelList.price)
            intent.putExtra("rating", basketModelList.rating)
            intent.putExtra("category", basketModelList.category)
            intent.putExtra("desc", basketModelList.desc)
            it.context.startActivity(intent)
        }

        holder.tvDelete.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
                .setTitle("Hapus")
                .setMessage("Apakah Anda yakin untuk menghapus?")
                .setPositiveButton("Iya") { _, _ ->
                    callback.removePosition(position)
                    removeBasket(holder.tvTitle.text.toString())
                    Toast.makeText(it.context, "Item telah dihapus dari daftar keranjang anda", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.isChecked = basketModel[holder.layoutPosition].checked?.lowercase().toBoolean()

        holder.checkBox.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked) {
                basketModel[holder.layoutPosition].checked = "True"
                checkedItem(holder.tvTitle.text.toString(), "True")
                Log.d("Check", "Checked ${holder.tvTitle.text}")

            } else {
                basketModel[holder.layoutPosition].checked = "False"
                checkedItem(holder.tvTitle.text.toString(), "False")
                Log.d("Check", "Unchecked ${holder.tvTitle.text}")
            }
        }

    }

    override fun getItemCount(): Int {
        return basketModel.size
    }

    fun removePosition(pos: Int) {
        basketModel.removeAt(pos)
        notifyItemRemoved(pos)
    }

    private fun checkedItem(title: String, checkStat: String) {
        val query = databaseReferenceBasket.ref.orderByChild("title")
            .equalTo(title)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.child("check").ref.setValue(checkStat)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun formatterCurrencyToIND(textSplit: String, count: Int): String {
        val splitPrice = textSplit.split('.').joinToString("")
        val totalPrice = Integer.parseInt(splitPrice) * count
        val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(totalPrice).replace("Rp", "")
            .replace(",00", "")
    }

    private fun updateCount(title: String, price: Int, count: Int) {
        val query: Query = databaseReferenceBasket.ref.orderByChild("title").equalTo(title)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val key = it.ref.key.toString()
                        val map = HashMap<String, Any>()
                        map["count"] = count.toString()
                        map["totalPrice"] = formatterCurrencyToIND(price.toString(), count)
                        databaseReferenceBasket.child(key).updateChildren(map)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
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