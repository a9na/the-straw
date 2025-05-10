package com.ryz.coffee.user.fragment.home.recyclerview

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ryz.coffee.R
import com.squareup.picasso.Picasso


class HomeBannerAdapter(private val homeBannerModel: ArrayList<HomeBannerModel>) :
    RecyclerView.Adapter<HomeBannerAdapter.ViewHolder>() {

    private var posAttached:Int = 0
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBanner: ImageView = itemView.findViewById(R.id.imgBanner)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_home_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bannerModelList = homeBannerModel[position]
        val imgUri: String? = bannerModelList.image
        Picasso.get().load(imgUri).into(holder.imgBanner)
    }

    override fun getItemCount(): Int {
        return homeBannerModel.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        posAttached = holder.layoutPosition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager: RecyclerView.LayoutManager? = recyclerView.layoutManager
    }

    fun getPosAttached(): Int {
        return posAttached
    }
}