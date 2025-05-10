package com.ryz.coffee.user.fragment.order.tab.tab5

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.squareup.picasso.Picasso


class CanceledAdapter(private val canceledModel: ArrayList<CanceledModel>) :
    RecyclerView.Adapter<CanceledAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var listener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun onItemClick(view: View, notYetPaidModel: CanceledModel)
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
            .inflate(R.layout.recyclerview_canceled, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = Firebase.auth

        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())
        databaseReference =
            FirebaseDatabase.getInstance().getReference("User/$cleanEmail/ProductOrder")

        val canceledList = canceledModel[position]
        val imgUri: String? = canceledList.image
        Picasso.get().load(imgUri).into(holder.imageCoffee)

        holder.tvTitle.text = canceledList.title
        holder.tvCount.text = canceledList.count
        holder.tvPrice.text = canceledList.totalPrice
        holder.tvTotalPayment.text = canceledList.totalPayment
        holder.tvCountChildren.text = canceledList.totalChildren
//        holder.cardViewClick.setOnClickListener {
//            listener?.onItemClick(it, notYetPaidList)
//        }
    }

    override fun getItemCount(): Int {
        return canceledModel.size
    }
}