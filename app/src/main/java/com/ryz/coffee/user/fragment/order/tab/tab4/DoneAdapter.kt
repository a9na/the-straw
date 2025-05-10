package com.ryz.coffee.user.fragment.order.tab.tab4

import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.squareup.picasso.Picasso


class DoneAdapter(private val doneModel: ArrayList<DoneModel>) :
    RecyclerView.Adapter<DoneAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var listener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun onItemClick(view: View, notYetPaidModel: DoneModel)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageCoffee: ImageView = itemView.findViewById(R.id.imgCoffee)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCount: TextView = itemView.findViewById(R.id.tvCount)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var tvCountChildren: TextView = itemView.findViewById(R.id.tvCountChildren)
        var tvTotalPayment: TextView = itemView.findViewById(R.id.tvTotalPayment)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_done, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")

        val doneList = doneModel[position]
        val imgUri: String? = doneList.image
        Picasso.get().load(imgUri).into(holder.imageCoffee)

        holder.tvTitle.text = doneList.title
        holder.tvCount.text = doneList.count
        holder.tvPrice.text = doneList.totalPrice
        holder.tvTotalPayment.text = doneList.totalPayment
        holder.tvCountChildren.text = doneList.totalChildren
//        holder.cardViewClick.setOnClickListener {
//            listener?.onItemClick(it, notYetPaidList)
//        }
    }

    override fun getItemCount(): Int {
        return doneModel.size
    }
}