package com.ryz.coffee.user.fragment.profile

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ryz.coffee.R
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class ChangeProfileActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var imgBack: ImageView
    private lateinit var imgProfile: ImageView
    private lateinit var tvChangeImage: TextView
    private lateinit var edUsername: EditText
    private lateinit var edEmail: EditText
    private lateinit var edFullName: EditText
    private lateinit var btnSave: Button
    private lateinit var edNumberPhone: EditText
    private lateinit var tvVerify: TextView
    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_change_profile)

        imgBack = findViewById(R.id.imgBack)
        imgProfile = findViewById(R.id.imgProfile)
        tvChangeImage = findViewById(R.id.tvChangeImage)
        edUsername = findViewById(R.id.edUsername)
        edEmail = findViewById(R.id.edEmail)
        edFullName = findViewById(R.id.edFullName)
        btnSave = findViewById(R.id.btnSave)
        edNumberPhone = findViewById(R.id.edNumberPhone)
        tvVerify = findViewById(R.id.tvVerify)

        mAuth = Firebase.auth

        imgBack.setOnClickListener {
            finish()
        }

        getInfoUser()
    }

    private val resultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imgProfile.setImageURI(it)
                imgUri = it
                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                    R.color.primaryColor))
                btnSave.setTextColor(Color.parseColor("#212121"))
                btnSave.isEnabled = true
            }
        }


    private fun getInfoUser() {
        val user = mAuth.currentUser!!
        val email = user.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReference = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val photo: String? =
                        snapshot.child("photo").getValue(String::class.java)
                    val username: String? = snapshot.child("username").getValue(String::class.java)
                    val emailUser: String? = snapshot.child("email").getValue(String::class.java)
                    val fullName: String? = snapshot.child("fullName").getValue(String::class.java)
                    val numberPhone: String? =
                        snapshot.child("numberPhone").getValue(String::class.java)

                    val imgUri: String = photo.toString()
                    Picasso.get().load(imgUri).into(imgProfile)

                    edFullName.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int,
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            if (p0.toString()
                                    .isNotEmpty() && p0.toString() != fullName.toString()
                            ) {
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.primaryColor))
                                btnSave.setTextColor(Color.parseColor("#212121"))
                                btnSave.isEnabled = true
                            } else { // kondisi false
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.grey))
                                btnSave.setTextColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.white))
                                btnSave.isEnabled = false
                            }
                        }

                        override fun afterTextChanged(p0: Editable?) {
                        }
                    })
                    edNumberPhone.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int,
                        ) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            if (edNumberPhone.text.toString().length in 11..13) {
                                if (p0.toString()
                                        .isNotEmpty() && p0.toString() != numberPhone.toString()
                                ) {
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                        R.color.primaryColor))
                                    btnSave.setTextColor(Color.parseColor("#212121"))
                                    btnSave.isEnabled = true
                                } else { // kondisi false
                                    btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                        R.color.grey))
                                    btnSave.setTextColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                        R.color.white))
                                    btnSave.isEnabled = false
                                }
                            } else if (edNumberPhone.text.toString().length !in 11..13 && p0.toString().isNotEmpty()) {
                                edNumberPhone.error = "Nomor tidak valid"
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.grey))
                                btnSave.setTextColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.white))
                                btnSave.isEnabled = false
                            }
                        }

                        override fun afterTextChanged(p0: Editable?) {
                        }
                    })
                    edUsername.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int,
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            if (p0.toString()
                                    .isNotEmpty() && p0.toString() != username.toString()
                            ) {
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.primaryColor))
                                btnSave.setTextColor(Color.parseColor("#212121"))
                                btnSave.isEnabled = true
                            } else { // kondisi false
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.grey))
                                btnSave.setTextColor(ContextCompat.getColor(this@ChangeProfileActivity,
                                    R.color.white))
                                btnSave.isEnabled = false
                            }
                        }

                        override fun afterTextChanged(p0: Editable?) {
                        }

                    })

                    edNumberPhone.setText(numberPhone).toString()
                    edNumberPhone.isEnabled = true

                    edFullName.setText(fullName).toString()
                    edFullName.isEnabled = true

                    if (username!!.contains("Belum diatur")) {
                        edUsername.isEnabled = true
                    } else {
                        edUsername.isEnabled = false
                        edUsername.setText(username).toString()
                    }

                    edEmail.setText(emailUser).toString()
                    edEmail.isEnabled = false

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        tvVerify.setOnClickListener {
            user.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this@ChangeProfileActivity,
                        "Kami telah mengirimkan email untuk melakukan verifikasi",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (user.isEmailVerified) {
            user.reload()
            tvVerify.visibility = View.INVISIBLE
            val map = HashMap<String, Any>()
            map["verify"] = "Verified"
            databaseReference.updateChildren(map)
        } else {
            tvVerify.visibility = View.VISIBLE
        }

        imgProfile.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        tvChangeImage.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val txtFullName = edFullName.text.toString()
            val txtNumberPhone = edNumberPhone.text.toString()
            val txtUsername = edUsername.text.toString()
            val map = HashMap<String, Any>()

            if (imgUri != null) {
                uploadToFirebase(imgUri!!)
            }

            if (txtFullName.isNotEmpty()) {
                map["fullName"] = txtFullName
                edFullName.isEnabled = false
            }

            if (txtNumberPhone.isNotEmpty()) {
                map["numberPhone"] = txtNumberPhone
                edNumberPhone.isEnabled = false
            }

            if (txtUsername.isNotEmpty()) {
                map["username"] = txtUsername
            }

            databaseReference.updateChildren(map)
        }
    }

    private fun uploadToFirebase(uri: Uri) {
        val user = mAuth.currentUser!!
        val email = user.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        storageReference = FirebaseStorage.getInstance().getReference("User/Profile/$cleanEmail")

        val fileRef: StorageReference =
            storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(
                uri))

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                val map = HashMap<String, Any>()
                map["photo"] = it.toString()
                databaseReference.updateChildren(map)
                Toast.makeText(this@ChangeProfileActivity,
                    "Foto berhasil diganti",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val cr: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))!!

    }
}