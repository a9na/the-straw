package com.ryz.coffee.admin.fragment.home

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
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
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddProductActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_add_product)

        btnSave = findViewById(R.id.btnSave)
        imgBack = findViewById(R.id.imgBack)
        imgProduct = findViewById(R.id.imgProduct)
        tvUploadImage = findViewById(R.id.tvUploadImage)
        edProductName = findViewById(R.id.edProductName)
        edProductDesc = findViewById(R.id.edProductDesc)
        spinnerRecommend = findViewById(R.id.spinnerRecommend)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        edProductRating = findViewById(R.id.edProductRating)
        edProductPrice = findViewById(R.id.edProductPrice)

        imgBack.setOnClickListener {
            finish()
        }

        val arrayRecommend = arrayOf("Apakah produk rekomendasi?", "Iya", "Tidak")
        val recommendAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayRecommend)
        spinnerRecommend.adapter = recommendAdapter
        spinnerRecommend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p0?.getItemAtPosition(p2).toString() == "Apakah produk rekomendasi?") {
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.grey))
                    btnSave.setTextColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.white))
                    btnSave.isEnabled = false
                } else {
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.primaryColor))
                    btnSave.setTextColor(Color.parseColor("#212121"))
                    btnSave.isEnabled = true
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        bottomSheetDialogAddCategory = BottomSheetDialog(this, R.style.BottomSheetTheme)

        categoryModel = ArrayList()

        databaseReferenceCategory =
            FirebaseDatabase.getInstance().getReference("Product/ProductCategory")

        databaseReferenceCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryModel.clear()
                categoryModel.add("Pilih kategori")
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val category: String? = it.child("category").getValue(String::class.java)
                        if (category.toString() != "Semua" && category.toString() != "Rekomendasi") {
                            categoryModel.add(category!!)
                        }
                    }
                    categoryModel.add("Tambah kategori baru")
                    val arrayAdapter = ArrayAdapter(this@AddProductActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        categoryModel)
                    spinnerCategory.adapter = arrayAdapter

                    spinnerCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long,
                            ) {
                                if (p0?.getItemAtPosition(p2)
                                        .toString() == "Tambah kategori baru"
                                ) {
                                    bottomSheetAddCategory()

                                }

                                if (p0?.getItemAtPosition(p2).toString() == "Pilih kategori") {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@AddProductActivity,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                } else {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
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

        pushData()
    }

    private fun bottomSheetAddCategory() {

        spinnerCategory.setSelection(0)

        var child = 0

        databaseReferenceCategory =
            FirebaseDatabase.getInstance().getReference("Product/ProductCategory")

        val view = View.inflate(this@AddProductActivity, R.layout.bottom_sheet_add_category, null)

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
                            Toast.makeText(this@AddProductActivity,
                                "Kategori berhasil ditambahkan",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AddProductActivity,
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

    private val resultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imgProduct.setImageURI(it)
                imgUri = it
                btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                    R.color.primaryColor))
                btnSave.setTextColor(Color.parseColor("#212121"))
                btnSave.isEnabled = true
            }
        }

    private fun pushData() {
        var i = 0
        databaseReference = FirebaseDatabase.getInstance().getReference("Product/ProductData")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    i = snapshot.childrenCount.toInt()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.primaryColor))
                    btnSave.setTextColor(Color.parseColor("#212121"))
                    btnSave.isEnabled = true
                } else { // kondisi false
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.grey))
                    btnSave.setTextColor(ContextCompat.getColor(this@AddProductActivity,
                        R.color.white))
                    btnSave.isEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }

        edProductName.addTextChangedListener(textWatcher)
        edProductDesc.addTextChangedListener(textWatcher)
        edProductPrice.addTextChangedListener(textWatcher)
        edProductRating.addTextChangedListener(textWatcher)

        imgProduct.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        tvUploadImage.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val map = HashMap<String, Any>()

            val productName = edProductName.text.toString()
            val productDesc = edProductDesc.text.toString()
            val productRating = edProductRating.text.toString()
            val productPrice = edProductPrice.text.toString()

            var isEmptyField = false

            if (TextUtils.isEmpty(productName)) {
                isEmptyField = true
                edProductName.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(productDesc)) {
                isEmptyField = true
                edProductDesc.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(productRating)) {
                isEmptyField = true
                edProductRating.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(productPrice)) {
                isEmptyField = true
                edProductPrice.error = "Tidak boleh kosong"
            }

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

            if (imgUri != null && !isEmptyField) {

                storageReference = FirebaseStorage.getInstance().getReference("Product/ProductData")

                val fileRef: StorageReference =
                    storageReference.child(System.currentTimeMillis()
                        .toString() + "." + getFileExtension(
                        imgUri!!))

                fileRef.putFile(imgUri!!).addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener {

                        if (spinnerRecommend.selectedItem.toString() == "Iya") {
                            map["id"] = "2"
                        } else {
                            map["id"] = "0"
                        }

                        map["image"] = it.toString()
                        map["title"] = productName
                        map["desc"] = productDesc
                        map["rating"] = productRating
                        map["category"] = spinnerCategory.selectedItem.toString()
                        map["price"] = formatterCurrencyToIDR(Integer.parseInt(productPrice))
                        databaseReference.push().updateChildren(map)
                        Toast.makeText(this@AddProductActivity,
                            "Data produk berhasil di tambahkan",
                            Toast.LENGTH_SHORT).show()

                        edProductName.text.clear()
                        edProductDesc.text.clear()
                        edProductPrice.text.clear()
                        edProductRating.text.clear()
                        imgProduct.setImageResource(0)
                        spinnerCategory.setSelection(0)
                        spinnerRecommend.setSelection(0)
                    }
                }
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