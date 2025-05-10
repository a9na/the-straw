package com.ryz.coffee.user.fragment.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod

import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.profile.listview.ProfileAdapter
import com.ryz.coffee.user.fragment.profile.listview.ProfileModel
import com.ryz.coffee.user.fragment.profile.listview.UtilityScroll
import com.ryz.coffee.onboarding.OnBoardingWithLoginActivity
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var btnLogout: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvNumberPhone: TextView

    private lateinit var lvAccount: ListView
    private lateinit var lvAdapter: ProfileAdapter
    private lateinit var lvAccountModel: ArrayList<ProfileModel>

    private lateinit var lvAbout: ListView
    private lateinit var lvAboutAdapter: ProfileAdapter
    private lateinit var lvAboutModel: ArrayList<ProfileModel>

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var edOldPassword: EditText
    private lateinit var edNewPassword: EditText
    private lateinit var edNewPasswordAgain: EditText
    private lateinit var btnChangePassword: Button

    companion object {
        private const val TAG = "SIGN_OUT_TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnLogout = view.findViewById(R.id.btnLogout)
        imgProfile = view.findViewById(R.id.imgProfile)
        tvName = view.findViewById(R.id.tvName)
        tvNumberPhone = view.findViewById(R.id.tvNumberPhone)
        lvAccount = view.findViewById(R.id.lvAccount)
        lvAbout = view.findViewById(R.id.lvAbout)

        mAuth = Firebase.auth
        bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)

        btnLogout.setOnClickListener {
            mAuth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
                when (result.signInProvider) {
                    "facebook.com" -> {
                        signOutFacebook()
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t: Logout dari Facebook")
                    }
                    "google.com" -> {

                        signOutGoogle()
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t: Logout dari Google")
                    }
                    else -> {
                        signOutEmail()
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t: Logout dari Email")
                    }
                }
            }
        }

        // call method to get Data User
        getDataUser()
        // show list menu account
        showListAccount()
        // show list menu about
        showListAbout()

        return view
    }

    private fun showListAccount() {
        lvAccountModel = ArrayList()
        lvAccountModel.add(ProfileModel("Ubah profil", R.drawable.profile_icon))
        lvAccountModel.add(ProfileModel("Alamat saya", R.drawable.address_icon))
        lvAccountModel.add(ProfileModel("Ubah kata sandi", R.drawable.password_icon))
        lvAdapter = ProfileAdapter(lvAccountModel, requireContext())
        lvAdapter.notifyDataSetChanged()
        lvAccount.adapter = lvAdapter
        UtilityScroll.setListViewHeightBasedOnChildren(lvAccount)
        lvAccount.isNestedScrollingEnabled = false

        lvAccount.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    startActivity(Intent(context, ChangeProfileActivity::class.java))
                }
                1 -> {
                    startActivity(Intent(context, AddressActivity::class.java))
                }
                2 -> {
                    //show bottom sheet dialog to change password
                    bottomSheetDialogChangePassword()
                }
            }
        }
    }

    private fun showListAbout() {
        lvAboutModel = ArrayList()
        lvAboutModel.add(ProfileModel("Syarat dan ketentuan", R.drawable.term_icon))
        lvAboutModel.add(ProfileModel("Kebijakan pribadi", R.drawable.privacy_icon))
        lvAboutModel.add(ProfileModel("Pengembang", R.drawable.developer_icon))
        lvAboutAdapter = ProfileAdapter(lvAboutModel, requireContext())
        lvAboutAdapter.notifyDataSetChanged()
        lvAbout.adapter = lvAboutAdapter
        UtilityScroll.setListViewHeightBasedOnChildren(lvAbout)
        lvAbout.isNestedScrollingEnabled = false


        lvAbout.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                Toast.makeText(context, "Kamu menekan s&k", Toast.LENGTH_SHORT).show()
            } else if (position == 1) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.privacypolicies.com/live/3828ffe1-64b3-48d5-ba65-ec6451e3b9d3")))
            }
        }
    }

    private fun bottomSheetDialogChangePassword() {
        val view = View.inflate(requireContext(), R.layout.bottom_sheet_change_password, null)

        val displayMetrics = this.resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.70).toInt()

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setContentView(view)

        bottomSheetBehavior = BottomSheetBehavior.from(
            view.parent as View
        ).apply {
            peekHeight = maxHeight
        }
        bottomSheetDialog.show()

        btnChangePassword = bottomSheetDialog.findViewById(R.id.btnChangePassword)!!

        edOldPassword = bottomSheetDialog.findViewById(R.id.edOldPassword)!!
        edNewPassword = bottomSheetDialog.findViewById(R.id.edNewPassword)!!
        edNewPasswordAgain = bottomSheetDialog.findViewById(R.id.edNewPasswordAgain)!!

        btnChangePassword.setOnClickListener {
            var isEmptyField = false

            val txtOldPassword = edOldPassword.text.toString()
            val txtNewPassword = edNewPassword.text.toString()
            val txtNewPasswordAgain = edNewPasswordAgain.text.toString()

            if (TextUtils.isEmpty(txtOldPassword)) {
                isEmptyField = true
                edOldPassword.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(txtNewPassword)) {
                isEmptyField = true
                edNewPassword.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(txtNewPasswordAgain)) {
                isEmptyField = true
                edNewPasswordAgain.error = "Tidak boleh kosong"
            }

            if (!isEmptyField) {
                when {
                    txtNewPassword.length < 6 -> {
                        Toast.makeText(context,
                            "Password terlalu pendek",
                            Toast.LENGTH_SHORT).show()
                    }
                    txtNewPassword == txtNewPasswordAgain -> {
                        changePassword(txtOldPassword, txtNewPassword)
                    }
                    else -> {
                        Toast.makeText(context,
                            "Password tidak sama",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun changePassword(txtOldPassword: String, txtNewPassword: String) {
        val user = mAuth.currentUser

        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, txtOldPassword)

            user.reauthenticate(credential).addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    user.updatePassword(txtNewPassword).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context,
                                "Password berhasil diganti",
                                Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                bottomSheetDialog.dismiss()
                            }, 2000)
                        }
                    }

                } else {
                    Toast.makeText(context,
                        "Re-Authentication gagal",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signOutFacebook() {
        FirebaseAuth.getInstance().signOut()
        if (AccessToken.getCurrentAccessToken() == null) {
            return
        }
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE
        ) {
            LoginManager.getInstance().logOut()
            startActivity(
                Intent(
                    context,
                    OnBoardingWithLoginActivity::class.java
                )
            )
        }.executeAsync()
    }

    private fun signOutGoogle() {
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(
                context,
                OnBoardingWithLoginActivity::class.java
            )
        )
    }

    private fun signOutEmail() {
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(
                context,
                OnBoardingWithLoginActivity::class.java
            )
        )
    }

    private fun getDataUser() {
        val email = mAuth.currentUser!!.email
        val cleanEmail = email!!.split('.').joinToString(','.toString())

        databaseReference = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val photoProfile: String? = snapshot.child("photo").getValue(String::class.java)
                    val username: String? = snapshot.child("username").getValue(String::class.java)
                    val numberPhone: String? = snapshot.child("numberPhone").getValue(String::class.java)

                    val imgUri: String = photoProfile.toString()
                    Picasso.get().load(imgUri).into(imgProfile)
                    tvName.text = username
                    if (numberPhone != null) {
                        tvNumberPhone.text = numberPhone
                    } else {
                        tvNumberPhone.text = "Nomor handphone belum diatur"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}