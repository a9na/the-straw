package com.ryz.coffee.admin.fragment.home.recyclerview

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ryz.coffee.R
import com.squareup.picasso.Picasso

class BannerAdapter(private val bannerModel: ArrayList<BannerModel>) :
    RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    private lateinit var databaseReference: DatabaseReference

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgBanner: ImageView = itemView.findViewById(R.id.imgBanner)
        var cardViewBanner: CardView = itemView.findViewById(R.id.cardViewBanner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_add_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        databaseReference =
            FirebaseDatabase.getInstance().getReference("Product/ProductBanner")

        val bannerModelList = bannerModel[position]
        val imgUri: String? = bannerModelList.image
        Picasso.get().load(imgUri).into(holder.imgBanner)

        holder.cardViewBanner.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
                .setTitle("Hapus")
                .setMessage("Apakah Anda yakin untuk menghapus?")
                .setPositiveButton("Iya") { _, _ ->
                    removeBanner(bannerModelList.id.toString())
                    Toast.makeText(it.context,
                        "Banner telah dihapus",
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
        return bannerModel.size
    }

    private fun removeBanner(id: String) {
        val query: Query = databaseReference.ref.orderByChild("id")
            .equalTo(id)
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