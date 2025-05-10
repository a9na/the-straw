package com.ryz.coffee.admin.fragment.home

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ryz.coffee.R
import com.ryz.coffee.admin.fragment.home.recyclerview.BannerAdapter
import com.ryz.coffee.admin.fragment.home.recyclerview.BannerModel

class AddBannerActivity : AppCompatActivity() {

    private lateinit var imgBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var imgBanner: ImageView
    private lateinit var tvUploadImage: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imgUri: Uri? = null

    private lateinit var bannerModel: ArrayList<BannerModel>
    private lateinit var bannerAdapter: BannerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_banner)

        imgBack = findViewById(R.id.imgBack)
        btnSave = findViewById(R.id.btnSave)
        imgBanner = findViewById(R.id.imgBanner)
        tvUploadImage = findViewById(R.id.tvUploadImage)
        recyclerView = findViewById(R.id.rvBanner)

        imgBack.setOnClickListener {
            finish()
        }

        bannerModel = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(this)

        pushDataBanner()
    }

    private val resultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imgBanner.setImageURI(it)
                imgUri = it
                btnSave.setBackgroundColor(ContextCompat.getColor(this@AddBannerActivity,
                    R.color.primaryColor))
                btnSave.setTextColor(Color.parseColor("#212121"))
                btnSave.isEnabled = true
            }
        }

    private fun pushDataBanner() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Product/ProductBanner")

        imgBanner.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        tvUploadImage.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            if (imgUri != null) {
                uploadToFirebase(imgUri!!)
            }
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bannerModel.clear()
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val image: String? = it.child("image").getValue(String::class.java)
                        val id: String? = it.child("id").getValue(String::class.java)

                        bannerModel.add(BannerModel(image, id))
                    }
                    bannerAdapter = BannerAdapter(bannerModel)
                    bannerAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = bannerAdapter
                    recyclerView.isNestedScrollingEnabled = false
                } else {
                    bannerModel.clear()
                    bannerAdapter = BannerAdapter(bannerModel)
                    bannerAdapter.notifyDataSetChanged()
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = bannerAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun uploadToFirebase(uri: Uri) {
        var i = 0
        storageReference = FirebaseStorage.getInstance().getReference("Product/ProductBanner")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { ds ->
                        val id: String? = ds.child("id").getValue(String::class.java)
                        i = id?.toInt()!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        val fileRef: StorageReference =
            storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(
                uri))

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                val map = HashMap<String, Any>()
                map["image"] = it.toString()
                map["id"] = "${i + 1}"
                databaseReference.child("${i + 1}").updateChildren(map)
                Toast.makeText(this@AddBannerActivity,
                    "Foto banner berhasil di upload",
                    Toast.LENGTH_SHORT).show()
                imgBanner.setImageResource(0)
                btnSave.setBackgroundColor(ContextCompat.getColor(this@AddBannerActivity,
                    R.color.grey))
                btnSave.setTextColor(ContextCompat.getColor(this@AddBannerActivity,
                    R.color.white))
                btnSave.isEnabled = false
            }
        }

    }

    private fun getFileExtension(uri: Uri): String {
        val cr: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))!!
    }
}