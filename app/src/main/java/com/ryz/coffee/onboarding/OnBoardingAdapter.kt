package com.ryz.coffee.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.ryz.coffee.R

class OnBoardingAdapter(
    private var context: Context,
    private var onBoardingDataList: List<OnboardingData>
) : PagerAdapter() {

    // mendapatkan panjang list untuk menampilkan data onboarding
    override fun getCount(): Int {
        // menggunakan fungsi size untuk mengukur panjang list
        return onBoardingDataList.size
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    // method untuk menginisialisasi layout yang digunakana
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // menggunakan library LayoutInflater untuk mengambil layout yang mau dipakai
        val view = LayoutInflater.from(context).inflate(R.layout.viewpager, null)

        // mengambil atribut yang digunakan dalam layout tersebut
        val imageView: ImageView = view.findViewById(R.id.imgInfo)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val desc: TextView = view.findViewById(R.id.tvDesc)

        // mengeset output yang dimana datanya diambil dari list
        imageView.setImageResource(onBoardingDataList[position].ImageUrl!!)
        title.text = onBoardingDataList[position].title
        desc.text = onBoardingDataList[position].desc

        container.addView(view)
        return view
    }
}