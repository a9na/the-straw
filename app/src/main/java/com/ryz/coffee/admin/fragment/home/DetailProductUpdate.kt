package com.ryz.coffee.admin.fragment.home

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ryz.coffee.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DetailProductUpdate : AppCompatActivity() {
    private lateinit var btnSave: Button
    private lateinit var imgBack: ImageView
    private lateinit var imgProduct: ImageView
    private lateinit var tvUploadImage: TextView
    private lateinit var edProductName: EditText
    private lateinit var edProductDesc: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRecommend: Spinner
    private lateinit var edProductRating: EditText
    private lateinit var edProductPrice: EditText

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var databaseReferenceCategory: DatabaseReference
    private var imgUri: Uri? = null

    private lateinit var bottomSheetDialogAddCategory: BottomSheetDialog
    private lateinit var bottomSheetBehaviorAddCategory: BottomSheetBehavior<View>
    private lateinit var btnAddCategory: Button
    private lateinit var edAddCategory: EditText

    private lateinit var categoryModel: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product_update)

        btnSave = findViewById(R.id.btnSave)
        imgBack = findViewById(R.id.imgBack)
        imgProduct = findViewById(R.id.imgProduct)
        tvUploadImage = findViewById(R.id.tvUploadImage)
        edProductName = findViewById(R.id.edProductName)
        edProductDesc = findViewById(R.id.edProductDesc)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerRecommend = findViewById(R.id.spinnerRecommend)
        edProductRating = findViewById(R.id.edProductRating)
        edProductPrice = findViewById(R.id.edProductPrice)

        imgBack.setOnClickListener {
            finish()
        }
        pushData()

    }

    private val resultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imgProduct.setImageURI(it)
                imgUri = it
                btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                    R.color.primaryColor))
                btnSave.setTextColor(Color.parseColor("#212121"))
                btnSave.isEnabled = true
            }
        }

    private fun bottomSheetAddCategory() {

        var child = 0
        spinnerCategory.setSelection(0)

        bottomSheetDialogAddCategory = BottomSheetDialog(this, R.style.BottomSheetTheme)

        databaseReferenceCategory =
            FirebaseDatabase.getInstance().getReference("Product/ProductCategory")

        val view = View.inflate(this@DetailProductUpdate, R.layout.bottom_sheet_add_category, null)

        val displayMetrics = this.resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.70).toInt()

        bottomSheetDialogAddCategory.setContentView(view)

        bottomSheetBehaviorAddCategory = BottomSheetBehavior.from(
            view.parent as View
        ).apply {
            peekHeight = maxHeight
        }
        bottomSheetDialogAddCategory.show()

        btnAddCategory = bottomSheetDialogAddCategory.findViewById(R.id.btnAddCategory)!!
        edAddCategory = bottomSheetDialogAddCategory.findViewById(R.id.edAddProduct)!!

        databaseReferenceCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    child = snapshot.childrenCount.toInt()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        btnAddCategory.setOnClickListener {

            var isEmptyField = false

            if (TextUtils.isEmpty(edAddCategory.text.toString())) {
                isEmptyField = true
                edAddCategory.error = "Tidak boleh kosong"
            }

            if (!isEmptyField) {

                val map = HashMap<String, Any>()

                val query = databaseReferenceCategory.ref.orderByChild("category")
                    .equalTo(edAddCategory.text.toString())
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            map["category"] = edAddCategory.text.toString()
                            map["id"] = "${child + 1}"
                            databaseReferenceCategory.child("${child + 1}").updateChildren(map)
                            Toast.makeText(this@DetailProductUpdate,
                                "Kategori berhasil ditambahkan",
                                Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this@DetailProductUpdate,
                                "Kategori sudah ada",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
    }

    private fun pushData() {

        // initialization categoryModel
        categoryModel = ArrayList()

        // get data from intent ProductAdapter
        val intentImage: String? = intent.getStringExtra("image")
        val intentTitle: String? = intent.getStringExtra("title")
        val intentDesc: String? = intent.getStringExtra("desc")
        val intentCategory: String? = intent.getStringExtra("category")
        val intentRating: String? = intent.getStringExtra("rating")
        val intentPrice: String? = intent.getStringExtra("price")
        val intentId: String? = intent.getStringExtra("id")

        // convert intentprice to remove dot
        val splitPrice = intentPrice.toString().split('.').joinToString("")

        // load image from intent
        Picasso.get().load(intentImage).into(imgProduct)
        // set edit text with value from intent ProductAdapter
        edProductName.setText(intentTitle.toString())
        edProductRating.setText(intentRating.toString())
        edProductPrice.setText(intentPrice.toString())
        edProductPrice.setText(splitPrice)
        edProductRating.setText(intentRating.toString())
        edProductDesc.setText(intentDesc.toString())

        // initialization array Recommend recommend with value
        val arrayRecommend = arrayOf("Apakah produk rekomendasi?", "Iya", "Tidak")
        // initialization adapter spinner Recommend
        val recommendAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayRecommend)
        // set adapter spinner Recommend
        spinnerRecommend.adapter = recommendAdapter
        // to get value selected from spinner
        spinnerRecommend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // condition to check selected item
                if (p0?.getItemAtPosition(p2).toString() == "Apakah produk rekomendasi?") {
                    // if value same with selected set button disable
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                        R.color.grey))
                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                        R.color.white))
                    btnSave.isEnabled = false
                } else {
                    // if value not same with selected set button enable
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                        R.color.primaryColor))
                    btnSave.setTextColor(Color.parseColor("#212121"))
                    btnSave.isEnabled = true
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // initialization database to get value from reference
        databaseReferenceCategory =
            FirebaseDatabase.getInstance().getReference("Product/ProductCategory")

        databaseReferenceCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear array if new value
                categoryModel.clear()
                // add value to position 0
                categoryModel.add("Pilih kategori")
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        // get value from firebase
                        val category: String? = it.child("category").getValue(String::class.java)
                        // show value to spinner if category not same with "Semua" and "Rekomendasi"
                        if (category.toString() != "Semua" && category.toString() != "Rekomendasi") {
                            // add value to array
                            categoryModel.add(category!!)
                        }
                    }
                    // add value to last position
                    categoryModel.add("Tambah kategori baru")
                    // show value to adapter
                    val arrayAdapter = ArrayAdapter(this@DetailProductUpdate,
                        android.R.layout.simple_spinner_dropdown_item,
                        categoryModel)
                    // show list spinner
                    spinnerCategory.adapter = arrayAdapter

                    // to set value category with value from intent ProductAdapter
                    if (intentCategory != null) {
                        // convert value intent category to get position from spinner recommend
                        val spinnerPos = arrayAdapter.getPosition(intentCategory.toString())
                        // set selected spinner with position spinnerPos
                        spinnerCategory.setSelection(spinnerPos)
                    }

                    // to get value selected from spinner
                    spinnerCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long,
                            ) {
                                // condition if spinner selected with value "Tambah kategori baru"
                                if (p0?.getItemAtPosition(p2)
                                        .toString() == "Tambah kategori baru"
                                ) {
                                    // show bottom sheet to add new category
                                    bottomSheetAddCategory()
                                }
                                // condition if selected with value "Pilih kategori" or "Tambah kategori baru"
                                if (p0?.getItemAtPosition(p2)
                                        .toString() == "Pilih kategori" || p0?.getItemAtPosition(p2)
                                        .toString() == "Tambah kategori baru"
                                ) {
                                    // set button save to disable
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                } else {
                                    // set button save to enable
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                }
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                            }

                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // convert value intentId to String value
        if (intentId != null) {
            val id: String = if (intentId == "2") {
                "Iya"
            } else {
                "Tidak"
            }
            // set position with value
            val spinnerPos = recommendAdapter.getPosition(id)
            // set selected spinner with value from spinnerPos
            spinnerRecommend.setSelection(spinnerPos)
        }

        // initialization database to get value from ref
        databaseReference = FirebaseDatabase.getInstance().getReference("Product/ProductData")

        // to check edit text
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val title: String? = it.child("title").getValue(String::class.java)
                        Log.d("JUDUL", "$title")
                        val desc: String? = it.child("desc").getValue(String::class.java)
                        val rating: String? = it.child("rating").getValue(String::class.java)
                        val price: String? = it.child("price").getValue(String::class.java)
                        val spPrice = price.toString().split('.').joinToString("")

                        edProductName.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                                if (p0.toString()
                                        .isNotEmpty() && p0.toString() != title.toString()
                                ) {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                } else { // kondisi false
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                }
                            }

                            override fun afterTextChanged(p0: Editable?) {
                            }

                        })
                        edProductDesc.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                                if (p0.toString()
                                        .isNotEmpty() && p0.toString() != desc.toString()
                                ) {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                } else { // kondisi false
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                }
                            }

                            override fun afterTextChanged(p0: Editable?) {
                            }
                        })

                        edProductRating.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                                if (p0.toString()
                                        .isNotEmpty() && p0.toString() != rating.toString()
                                ) {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                } else { // kondisi false
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                }
                            }

                            override fun afterTextChanged(p0: Editable?) {
                            }
                        })
                        edProductPrice.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int,
                            ) {
                                if (p0.toString().isNotEmpty() && p0.toString() != spPrice) {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                } else { // kondisi false
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@DetailProductUpdate,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                }
                            }

                            override fun afterTextChanged(p0: Editable?) {
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // onclick imageview to open gallery
        imgProduct.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        // onclick textview to open gallery
        tvUploadImage.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        // button to save value
        btnSave.setOnClickListener {
            // initialization hasmap to input value
            val map = HashMap<String, Any>()
            var isEmptyField = false

            val productName = edProductName.text.toString()
            val productDesc = edProductDesc.text.toString()
            val productRating = edProductRating.text.toString()
            val productPrice = edProductPrice.text.toString()

             if (spinnerRecommend.selectedItem.toString() == "Apakah produk rekomendasi?") {
                 isEmptyField = true
                 val txtError = spinnerRecommend.selectedView as TextView
                 txtError.error = "Silahkan dipilih"
                 txtError.requestFocus()
             }

             if (spinnerCategory.selectedItem.toString() == "Pilih kategori") {
                 isEmptyField = true
                 val txtError = spinnerCategory.selectedView as TextView
                 txtError.error = "Silahkan dipilih"
                 txtError.requestFocus()
             }

            val query = databaseReference.ref.orderByChild("title").equalTo(intentTitle)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.forEach { ds ->
                            val key = ds.key.toString()

                            if (imgUri != null) {
                                uploadToFirebase(imgUri!!, intentTitle.toString())
                            }

                            if (productName.isNotEmpty()) {
                                map["title"] = productName
                            }

                            if (productDesc.isNotEmpty()) {
                                map["desc"] = productDesc
                            }

                            if (productPrice.isNotEmpty()) {
                                map["price"] = formatterCurrencyToIDR(Integer.parseInt(productPrice))
                            }

                            if (productRating.isNotEmpty()) {
                                map["rating"] = productRating
                            }

                            if (spinnerCategory.selectedItem.toString() != "Pilih kategori" || spinnerCategory.selectedItem.toString() != "Tambah kategori baru") {
                                map["category"] = spinnerCategory.selectedItem.toString()
                            }

                            if (spinnerRecommend.selectedItem.toString() == "Iya") {
                                map["id"] = "2"
                            } else {
                                map["id"] = "0"
                            }

                            if (!isEmptyField) {
                                databaseReference.child(key).updateChildren(map)
                                Toast.makeText(this@DetailProductUpdate,
                                    "Data produk berhasil diupdate",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        }
    }

    private fun uploadToFirebase(uri: Uri, intentTitle: String) {

        storageReference = FirebaseStorage.getInstance().getReference("Product/ProductData")

        val fileRef: StorageReference =
            storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(
                uri))

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                val query = databaseReference.ref.orderByChild("title").equalTo(intentTitle)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            snapshot.children.forEach { ds ->
                                val key = ds.key.toString()
                                val map = HashMap<String, Any>()
                                map["image"] = it.toString()
                                databaseReference.child(key).updateChildren(map)
                                Toast.makeText(this@DetailProductUpdate,
                                    "Foto berhasil diganti",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val cr: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))!!
    }

    // method untuk memformat harga item ke dalam bentuk IDR
    private fun formatterCurrencyToIDR(price: Int): String {
        val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(price).replace("Rp", "")
            .replace(",00", "")
    }
}