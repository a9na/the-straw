package com.ryz.coffee.user.fragment.profile.listview

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ryz.coffee.R
import java.util.ArrayList


class ProfileAdapter(
    private val profileModel: ArrayList<ProfileModel>,
    private val context: Context,
) : BaseAdapter() {
    override fun getCount(): Int {
        return profileModel.size
    }

    override fun getItem(position: Int): Any {
        return profileModel[position]
    }

    override fun getItemId(position: Int): Long {
        return profileModel.indexOf(getItem(position)).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        var convert = convertView

        val layoutInflater: LayoutInflater =
            context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convert == null) {
            convert = layoutInflater.inflate(R.layout.listview_profile, parent, false)
            holder = ViewHolder()

            holder.imgIcon = convert.findViewById(R.id.imgIcon)
            holder.tvChangeProfile = convert.findViewById(R.id.tvChangeProfile)

            val profileModelList = profileModel[position]

            holder.imgIcon.setImageResource(profileModelList.image!!)
            holder.tvChangeProfile.text = profileModelList.title

        }

        return convert!!
    }
}

private class ViewHolder {
    lateinit var imgIcon: ImageView
    lateinit var tvChangeProfile: TextView

}
