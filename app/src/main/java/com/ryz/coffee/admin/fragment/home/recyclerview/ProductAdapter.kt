package com.ryz.coffee.admin.fragment.home.recyclerview

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import atownsend.swipeopenhelper.BaseSwipeOpenViewHolder
import com.google.firebase.database.*
import com.ryz.coffee.R
import com.ryz.coffee.admin.fragment.home.DetailProductUpdate
import com.squareup.picasso.Picasso

class ProductAdapter(
    private val productModel: ArrayList<ProductModel>,
    private var callback: ButtonCallbacks,
) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    interface ButtonCallbacks {
        fun removePosition(pos: Int)
    }

    private lateinit var databaseReference: DatabaseReference

    class ViewHolder(itemView: View) : BaseSwipeOpenViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvRating: TextView = itemView.findViewById(R.id.tvRating)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var parentLayout: CardView = itemView.findViewById(R.id.parentLayout)
        var tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        override fun getSwipeView(): View {
            return parentLayout
        }

        override fun getEndHiddenViewSize(): Float {

            return 0.0.toFloat()
        }

        override fun getStartHiddenViewSize(): Float {
            return tvDelete.measuredWidth.toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_admin_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productModel.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Product/ProductData")

        val productModelList = productModel[position]
        val imgUri: String? = productModelList.image
        Picasso.get().load(imgUri).into(holder.image)
        holder.tvCategory.text = productModelList.category
        holder.tvTitle.text = productModelList.title
        holder.tvRating.text = productModelList.rating
        holder.tvPrice.text = productModelList.price
        holder.tvDelete.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
                .setTitle("Hapus")
                .setMessage("Apakah Anda yakin untuk mengapus?")
                .setPositiveButton("Iya") { _, _ ->
                    callback.removePosition(position)
                    removeDataProduct(holder.tvTitle.text.toString())
                    Toast.makeText(it.context, "Item telah dihapus dari daftar keranjang anda", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
        holder.parentLayout.setOnClickListener {
            val intent = Intent(it.context, DetailProductUpdate::class.java)
            intent.putExtra("image", imgUri.toString())
            intent.putExtra("id", productModelList.id)
            intent.putExtra("title", productModelList.title)
            intent.putExtra("desc", productModelList.desc)
            intent.putExtra("category", productModelList.category)
            intent.putExtra("price", productModelList.price)
            intent.putExtra("rating", productModelList.rating)
            it.context.startActivity(intent)
        }
    }

    fun removePosition(pos: Int) {
        productModel.removeAt(pos)
        notifyItemRemoved(pos)
    }

    private fun removeDataProduct(title: String) {
        val query: Query = databaseReference.ref.orderByChild("title")
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