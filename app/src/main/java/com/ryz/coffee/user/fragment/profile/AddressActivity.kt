package com.ryz.coffee.user.fragment.profile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import java.util.*
import kotlin.collections.HashMap

class AddressActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var edAddress: EditText
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var btnSave: Button
    private lateinit var imgBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        btnSave = findViewById(R.id.btnSave)
        edAddress = findViewById(R.id.edAddress)
        imgBack = findViewById(R.id.imgBack)

        mAuth = Firebase.auth

        imgBack.setOnClickListener {
            finish()
        }

        saveDataLocation()
    }

    private fun getLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@AddressActivity)

        val task = fusedLocationProviderClient.lastLocation
        if (ContextCompat.checkSelfPermission(this@AddressActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@AddressActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@AddressActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100)
        }

        task.addOnSuccessListener {
            Log.d("Kondisi", "$it")
            if (it != null) {
                Log.d("Kondisi", "1")
                val geocoder = Geocoder(this@AddressActivity, Locale.getDefault())
                val address: List<Address> =
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val add = address[0].getAddressLine(0)
                edAddress.setText(add).toString()
            }
        }
    }

    private fun saveDataLocation() {

        val user = mAuth.currentUser!!
        val email = user.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReference = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("address")) {
                    val address: String? =
                        snapshot.child("address").getValue(String::class.java)

                    edAddress.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int,
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            if (p0.toString()
                                    .isNotEmpty() && p0.toString() != address.toString()
                            ) {
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@AddressActivity,
                                    R.color.primaryColor))
                                btnSave.setTextColor(Color.parseColor("#212121"))
                                btnSave.isEnabled = true
                            } else { // kondisi false
                                btnSave.setBackgroundColor(ContextCompat.getColor(this@AddressActivity,
                                    R.color.grey))
                                btnSave.setTextColor(ContextCompat.getColor(this@AddressActivity,
                                    R.color.white))
                                btnSave.isEnabled = false
                            }
                        }

                        override fun afterTextChanged(p0: Editable?) {
                        }
                    })

                    edAddress.setText(address).toString()
                    edAddress.isEnabled = true
                } else {
                    getLocation()
                    btnSave.setBackgroundColor(ContextCompat.getColor(this@AddressActivity,
                        R.color.primaryColor))
                    btnSave.setTextColor(Color.parseColor("#212121"))
                    btnSave.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        btnSave.setOnClickListener {
            val map = HashMap<String, Any>()
            map["address"] = edAddress.text.toString()
            databaseReference.updateChildren(map)
            edAddress.isEnabled = false
            Toast.makeText(this@AddressActivity, "Data berhasil di simpan", Toast.LENGTH_SHORT).show()
        }
    }
}