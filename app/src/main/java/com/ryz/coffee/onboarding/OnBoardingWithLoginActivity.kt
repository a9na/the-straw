package com.ryz.coffee.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.user.UserActivity
import com.ryz.coffee.R
import com.ryz.coffee.admin.AdminActivity
import com.ryz.coffee.login.ForgotPasswordActivity
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

class OnBoardingWithLoginActivity : AppCompatActivity() {

    // initialization signIn
    private lateinit var mAuth: FirebaseAuth
    private var callbackManager: CallbackManager? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    // initialization view pager
    private var onBoardingAdapter: OnBoardingAdapter? = null
    private var onBoardingViewPager: ViewPager? = null
    private var tabLayoutIndicator: TabLayout? = null

    // initialization button login
    private lateinit var btnLoginWithGoogle: Button
    private lateinit var btnLoginWithFacebook: Button
    private lateinit var btnLoginWithEmail: Button

    // initialization atribut for bottom sheet
    // initialization atribut for signin bottomSheetDialog
    private lateinit var btnSignInEmail: Button
    private lateinit var btnSignInGoogle: Button
    private lateinit var btnSignInFacebook: Button
    private lateinit var bottomSheetDialogSignIn: BottomSheetDialog
    private lateinit var bottomSheetBehaviorSignIn: BottomSheetBehavior<View>
    private lateinit var edEmailSignIn: EditText
    private lateinit var edPasswordSignIn: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var tvForgot: TextView
    private lateinit var tvSignUp: TextView

    // initialization atribut for signup bottomSheetDialog
    private lateinit var bottomSheetDialogSignUp: BottomSheetDialog
    private lateinit var bottomSheetBehaviorSignUp: BottomSheetBehavior<View>
    private lateinit var btnSignUpEmail: Button
    private lateinit var edEmailSignUp: EditText
    private lateinit var edNumberPhone: EditText
    private lateinit var edPasswordSignUp: EditText
    private lateinit var edPasswordAgainSignUp: EditText
    private lateinit var cbAgreement: CheckBox
    private lateinit var tvSignIn: TextView

    // initialization atribut shared pref
    private var mypreference = "mypref"
    private var emailpref = "emailkey"
    private var passwordpref = "passwordkey"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var shareEditor: SharedPreferences.Editor

    //save data user
    private lateinit var databaseReference: DatabaseReference

    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboardingwithlogin)

        // untuk mengecek versi SDK saat ini
        // jika versi lebih dari atau sama dengan android M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
            // transparent navigation bar
            // window.navigationBarColor = Color.TRANSPARENT
            // set color statusbar dan navbar nya jadi warna hitam
            // fix color white on white statusbar
            // statusbar dan navbar diset transparent sehingga ketika berwarna putih maka akan jadi hitam iconnya
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // set color statusbar dan navbar jadi berwarna grey
            // icon berwarna putih karena versi di bawah android M belum support untuk hal itu
            window.statusBarColor = ContextCompat.getColor(this, R.color.grey)
        }

        // tabLayout untuk indicator viewpager
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator)
        // sigin with google account
        btnLoginWithGoogle = findViewById(R.id.btnLoginWithGoogle)
        // signin with facebook
        btnLoginWithFacebook = findViewById(R.id.btnLoginWithFacebook)
        // signin with email
        btnLoginWithEmail = findViewById(R.id.btnLoginWithEmail)

        // menampilkan data ke dalam onboarding
        val onBoardingData: MutableList<OnboardingData> = ArrayList()
        onBoardingData.add(
            OnboardingData(
                "Telusuri Kedai Kopi",
                "Telusuri aplikasi kedai kopi kami dan temukan produk favorit Anda",
                R.drawable.coffee_bg,
            )
        )
        onBoardingData.add(
            OnboardingData(
                "Pesan biji kopi",
                "Sekarang anda dapat memesan biji kopi pilihan anda kapanpun dari handphone kamu",
                R.drawable.shooping_bg,
            )
        )
        onBoardingData.add(
            OnboardingData(
                "Pengiriman cepat",
                "Pengiriman cepat ke rumah, kantor, dan di mana pun Anda berada",
                R.drawable.delivery_bg,
            )
        )
        // set data untuk viewpager nya
        setOnboardingViewPagerAdapter(onBoardingData)

        // inisialisasi style untuk bottom sheet dialog
        bottomSheetDialogSignIn = BottomSheetDialog(this, R.style.BottomSheetTheme)
        bottomSheetDialogSignUp = BottomSheetDialog(this, R.style.BottomSheetTheme)

        // firebase auth
        mAuth = Firebase.auth

        // konfigurasi google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail() // hanya perlu email dari akun google
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // facebook login
        AppEventsLogger.activateApp(this)
        callbackManager = CallbackManager.Factory.create()
        FacebookSdk.sdkInitialize(this)

        // call method to get hash key or secret key
        printHashKey()

        // check status login user
        checkUser()

        // button onClick signIn with Google
        btnLoginWithGoogle.setOnClickListener {
            signInGoogle()
        }

        // button onClick signIn with facebook
        btnLoginWithFacebook.setOnClickListener {
            signInFacebook()
        }

        // button onClick sigIn with email
        btnLoginWithEmail.setOnClickListener {
            bottomSheetDialogSignInWithEmail()
        }
    }

    private var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode== Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                Log.d(TAG, "onActivityResult \t\t\t\t\t\t\t\t: Google intent Result")
                try {
                    // login berhasil
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // login gagal
                    Log.d(TAG, "onActivityResult \t\t\t\t\t\t\t\t: ${e.message}")
                }
            }
        }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d(TAG, "onActivityResult \t\t\t\t\t\t\t\t: Google intent Result")
            try {
                // login berhasil
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // login gagal
                Log.d(TAG, "onActivityResult \t\t\t\t\t\t\t\t: ${e.message}")
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        // disable dialog if move activity
        // dismiss bottom sheet dialog ketika activity berpindah
        if (bottomSheetDialogSignIn.isShowing or bottomSheetDialogSignUp.isShowing) {
            bottomSheetDialogSignIn.dismiss()
            bottomSheetDialogSignUp.dismiss()
        }
    }

    // method untuk menyimpan data pengguna ke dalam realtime database
    private fun saveDataUser(email: String) {
        // mengubah email user dari titik menjadi koma
        // dalam realtime database penamaanya tidak bisa menggunakan titik
        val cleanEmail = email.split('.').joinToString(','.toString())
        val firebaseUser = mAuth.currentUser
        val photo = firebaseUser!!.photoUrl.toString()
        val username = firebaseUser.displayName.toString()
        // menampung ukuran dari photo yang digunakan
        var result = photo

        // inisialisasi reference realtime database yang digunakan
        databaseReference = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")

        // menampung nilai untuk dimasukan ke dalam realtime database
        val map = HashMap<String, Any>()

        // looping untuk mendapatkan platform login yang digunakan oleh user
        for (user in firebaseUser.providerData) {
            when (user.providerId) {
                "facebook.com" -> {
                    result = "$photo?type=large"
                }
                "google.com" -> {
                    result = photo.replace("s96-c", "s400-c")
                }
            }
        }

        map["email"] = email
        map["username"] = username
        map["photo"] = result
        map["verify"] = "Not verified"
        databaseReference.setValue(map)
    }

    // method untuk menyimpan data user yang pertama kali registrasi
    // method ini khusus untuk user yang mendaftar langsung pada aplikasi tersebut
    private fun saveDataNewRegistration(email: String) {
        // mengubah email user dari titik menjadi koma
        // dalam realtime database penamaanya tidak bisa menggunakan titik
        val cleanEmail = email.split('.').joinToString(','.toString())

        databaseReference = FirebaseDatabase.getInstance().getReference("User/$cleanEmail/Data")

        // menampung nilai untuk dimasukan ke dalam realtime database
        val map = HashMap<String, Any>()
        map["email"] = email
        map["username"] = "Belum diatur"
        map["numberPhone"] = edNumberPhone.text.toString()
        map["verify"] = "Not verified"
        databaseReference.setValue(map)
    }

    // method set color statusbar and navigationbar
    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    // method checkuser sekarang yang sedang login
    // dengan melakukan looping untuk mendapatkan info platform login yang sedang digunakan user
    private fun checkUser() {
        val firebaseUser = mAuth.currentUser
        if (firebaseUser != null) {
            for (user in firebaseUser.providerData) {
                when (user.providerId) {
                    "facebook.com" -> {
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t\t: Login menggunakan Facebook")
                        signIn()
                    }
                    "google.com" -> {
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t\t: Login menggunakan Google")
                        signIn()
                    }
                    "password" -> {
                        if (firebaseUser.email == "admin@gmail.com") {
                            signInAdmin()
                        } else {
                            signIn()
                        }
                        Log.d(TAG, "Status \t\t\t\t\t\t\t\t\t\t\t: Login menggunakan Email")
                    }
                }
            }
        }
    }

    // method signIn
    // user masuk ke dalam Main activity jika sebelumnya sudah login
    private fun signIn() {
        // start activity
        startActivity(
            Intent(
                this@OnBoardingWithLoginActivity,
                UserActivity::class.java
            )
        )
        finish()
    }

    private fun signInAdmin() {
        // start activity
        startActivity(
            Intent(
                this@OnBoardingWithLoginActivity,
                AdminActivity::class.java
            )
        )
        finish()
    }

    // ===================== START GOOGLE SIGN IN ===================== //

    // login with google account
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
        resultLauncher.launch(signInIntent)
    }

    // method to check status login with Google signIn
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // jika berhasil login
                // inisialisasi untuk mendapatkan data user login
                val user = mAuth.currentUser
                val uid = user!!.uid
                val email = user.email

                Log.d(TAG, "Login menggunakan Google Account \t\t\t\t: Berhasil")
                Log.d(TAG, "Google Account UID \t\t\t\t\t\t\t\t: $uid")
                Log.d(TAG, "Google Account Email \t\t\t\t\t\t\t: $email")

                // jika email tidak null
                if (email != null) {
                    // jika pengguna baru maka simpan data user terlebih dahulu ke dalam realtime database
                    if (authResult.additionalUserInfo!!.isNewUser) {
                        Log.d(TAG, "Login sebagai pengguna baru : $email")
                        saveDataUser(email.toString())
                        signIn()
                    } else {
                        // jika pengguna lama langsung berpindah activity saja
                        Log.d(TAG, "Login sebagai pengguna lama : $email")
                        signIn()
                    }
                } else {
                    // jika null maka tampilkan toast
                    Toast.makeText(this@OnBoardingWithLoginActivity,
                        "Login gagal email anda null",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "Login menggunakan Google Account \t\t\t\t: Login gagal ${e.message}"
                )
            }
    }

    // ===================== END GOOGLE SIGN IN ===================== //

    // ===================== START FACEBOOK SIGN IN ===================== //

    // method to get Hash Key Facebook login
    private fun printHashKey() {
        try {
            val info =
                packageManager.getPackageInfo("com.ryz.coffee", PackageManager.GET_SIGNATURES)
            for (signatures in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signatures.toByteArray())
                Log.d("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

    // check facebook app is installed or not
    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // login with Facebook account
    private fun signInFacebook() {
        val installed = appInstalledOrNot("com.facebook.katana")

        if (installed) {
            LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_ONLY
            Log.d(TAG, "Login Menggunakan Aplikasi facebook")
        } else {
            LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
            Log.d(TAG, "Login Menggunakan Webview Facebook")
        }

        LoginManager.getInstance().apply {
            logInWithReadPermissions(
                this@OnBoardingWithLoginActivity,
                mutableListOf("email", "public_profile")
            )
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    firebaseAuthWithFacebook(result!!.accessToken)
                    Log.d(
                        TAG,
                        "Login menggunakan Facebook Account \t\t\t\t: Berhasil dengan kode -> $result"
                    )
                }

                override fun onCancel() {
                    Log.d(TAG, "Login menggunakan Facebook Account \t\t\t\t: Cancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.d(
                        TAG,
                        "Login menggunakan Facebook Account \t\t\t\t: Error dengan kode -> $error"
                    )
                }
            })
        }
    }

    // method to check status login for Facebook signIn
    private fun firebaseAuthWithFacebook(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val profile = Profile.getCurrentProfile()
                val user = mAuth.currentUser
                val email = user!!.email
                Log.d(
                    TAG,
                    "Facebook Account Email \t\t\t\t\t\t\t: ${profile.name}"
                )

                // jika email tidak null
                if (email != null) {
                    // jika pengguna baru maka simpan data user terlebih dahulu ke dalam realtime database
                    if (authResult.additionalUserInfo!!.isNewUser) {
                        Log.d(TAG, "Login sebagai pengguna baru : $email")
                        saveDataUser(email.toString())
                        signIn()
                    } else {
                        // jika pengguna lama langsung berpindah activity saja
                        Log.d(TAG, "Login sebagai pengguna lama : $email")
                        signIn()
                    }
                } else {
                    LoginManager.getInstance().logOut()
                    // jika null maka tampilkan toast
                    Toast.makeText(this@OnBoardingWithLoginActivity,
                        "Login gagal email anda null",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "Login Facebook Account : Login gagal ${e.message}"
                )
            }
    }

    // ===================== END FACEBOOK SIGN IN ===================== //

    // ===================== START EMAIL SIGN IN ===================== //

    // bottom sheet dialog to show login menu
    private fun bottomSheetDialogSignInWithEmail() {
        sharedPreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE)
        shareEditor = sharedPreferences.edit()

        val view = View.inflate(this, R.layout.bottom_sheet_signin_with_email, null)

        // kalkulasi peek height bottomsheet dialog
        val displayMetrics = this.resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.60).toInt()

        // set contentview
        //val params = ViewGroup.LayoutParams(MATCH_PARENT, maxHeight)

        bottomSheetDialogSignIn.setCanceledOnTouchOutside(false)
        bottomSheetDialogSignIn.setContentView(view)

        bottomSheetBehaviorSignIn = BottomSheetBehavior.from(
            view.parent as View
        ).apply {
            peekHeight = maxHeight
        }

        bottomSheetDialogSignIn.show()

        // ketika bottom sheet dialog dismiss maka set mode menjadi collapsed
        bottomSheetDialogSignIn.setOnDismissListener {
            bottomSheetDialogSignIn.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // initialization button to sign In
        btnSignInEmail = bottomSheetDialogSignIn.findViewById(R.id.btnSignInEmail)!!
        btnSignInFacebook = bottomSheetDialogSignIn.findViewById(R.id.btnSignInFacebook)!!
        btnSignInGoogle = bottomSheetDialogSignIn.findViewById(R.id.btnSignInGoogle)!!

        edEmailSignIn = bottomSheetDialogSignIn.findViewById(R.id.edEmailSignIn)!!
        edPasswordSignIn = bottomSheetDialogSignIn.findViewById(R.id.edPasswordSignIn)!!
        tvForgot = bottomSheetDialogSignIn.findViewById(R.id.tvForgot)!!
        cbRemember = bottomSheetDialogSignIn.findViewById(R.id.cbRemember)!!

        tvSignUp = bottomSheetDialogSignIn.findViewById(R.id.tvSignUp)!!

        btnSignInEmail.setOnClickListener {
            var isEmptyField = false
            val txtEmail = edEmailSignIn.text.toString()
            val txtPassword = edPasswordSignIn.text.toString()

            if (TextUtils.isEmpty(txtEmail)) {
                isEmptyField = true
                edEmailSignIn.error = "Tidak boleh kosong"
            }
            if (TextUtils.isEmpty(txtPassword)) {
                isEmptyField = true
                edPasswordSignIn.error = "Tidak boleh kosong"
            }

            if (!isEmptyField) {
                signInEmail(txtEmail, txtPassword)

                // checkbox if checked
                // ketika checkbox di centang maka akan menyimpan data
                if (cbRemember.isChecked) {
                    shareEditor.putBoolean("saveLogin", true)
                    shareEditor.putString(emailpref, txtEmail)
                    shareEditor.putString(passwordpref, txtPassword)
                    shareEditor.apply()
                    Log.d("SAVE_DATA", "Data telah tersimpan")
                } else { // ketika tidak di centang maka data akan di clear
                    shareEditor.clear()
                    shareEditor.commit()
                    Log.d("SAVE_DATA", "Data tidak tersimpan")
                }
            }
        }

        btnSignInGoogle.setOnClickListener {
            signInGoogle()
        }

        btnSignInFacebook.setOnClickListener {
            signInFacebook()
        }

        tvForgot.setOnClickListener {
            startActivity(Intent(this@OnBoardingWithLoginActivity,
                ForgotPasswordActivity::class.java))
        }

        tvSignUp.setOnClickListener {
            bottomSheetDialogSignIn.dismiss()
            bottomSheetDialogSignUpWithEmail()
        }

        // call method to save input from user
        // mengambil data dari sharepref putBoolean saveLogin
        // nilai yang di save pada saveLogin bernilai true
        val loginSave = sharedPreferences.getBoolean("saveLogin", false)
        // jika loginSave bernilai true maka data akan terus disimpan
        if (loginSave) {
            saveData()
            cbRemember.isChecked = true
            Log.d("SAVE_DATA", "Inputan tersimpan")
        } else {
            Log.d("SAVE_DATA", "Inputan kosong")
        }
    }

    private fun bottomSheetDialogSignUpWithEmail() {
        val view = View.inflate(this, R.layout.bottom_sheet_signup_with_email, null)

        // kalkulasi peek height bottomsheet dialog
        val displayMetrics = this.resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.77).toInt()

        // set contentview
        //val params = ViewGroup.LayoutParams(MATCH_PARENT, maxHeight)

        bottomSheetDialogSignUp.setCanceledOnTouchOutside(false)
        bottomSheetDialogSignUp.setContentView(view)

        bottomSheetBehaviorSignUp = BottomSheetBehavior.from(
            view.parent as View
        ).apply {
            peekHeight = maxHeight
        }

        bottomSheetDialogSignUp.show()

        // ketika bottom sheet dialog dismiss maka set mode menjadi collapsed
        bottomSheetDialogSignUp.setOnDismissListener {
            bottomSheetDialogSignUp.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        btnSignUpEmail = bottomSheetDialogSignUp.findViewById(R.id.btnSignUpEmail)!!
        edEmailSignUp = bottomSheetDialogSignUp.findViewById(R.id.edEmailSignUp)!!
        edNumberPhone = bottomSheetDialogSignUp.findViewById(R.id.edNumberPhone)!!
        edPasswordSignUp = bottomSheetDialogSignUp.findViewById(R.id.edPasswordSignUp)!!
        edPasswordAgainSignUp = bottomSheetDialogSignUp.findViewById(R.id.edPasswordAgainSignUp)!!
        cbAgreement = bottomSheetDialogSignUp.findViewById(R.id.cbAgreement)!!
        tvSignIn = bottomSheetDialogSignUp.findViewById(R.id.tvSignIn)!!

        btnSignUpEmail.setOnClickListener {
            var isEmptyField = false
            val txtEmail = edEmailSignUp.text.toString()
            val txtNumberPhone = edNumberPhone.text.toString()
            val txtPassword = edPasswordSignUp.text.toString()
            val txtPasswordAgain = edPasswordAgainSignUp.text.toString()

            if (TextUtils.isEmpty(txtEmail)) {
                isEmptyField = true
                edEmailSignUp.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(txtNumberPhone)) {
                isEmptyField = true
                edNumberPhone.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(txtPassword)) {
                isEmptyField = true
                edPasswordSignUp.error = "Tidak boleh kosong"
            }

            if (TextUtils.isEmpty(txtPasswordAgain)) {
                isEmptyField = true
                edPasswordAgainSignUp.error = "Tidak boleh kosong"
            }

            if (!isEmptyField and cbAgreement.isChecked) {
                when {
                    txtPassword.length < 6 -> {
                        Toast.makeText(this@OnBoardingWithLoginActivity,
                            "Password terlalu pendek",
                            Toast.LENGTH_SHORT).show()
                    }
                    txtPassword == txtPasswordAgain -> {
                        signUpEmail(txtEmail, txtPassword)
                    }
                    else -> {
                        Toast.makeText(this@OnBoardingWithLoginActivity,
                            "Password tidak sama",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (!isEmptyField) {
                Toast.makeText(this@OnBoardingWithLoginActivity,
                    "Mohon untuk di ceklis",
                    Toast.LENGTH_SHORT).show()
            }
        }

        tvSignIn.setOnClickListener {
            bottomSheetDialogSignUp.dismiss()
            bottomSheetDialogSignInWithEmail()
        }
    }

    // SignIn with email
    private fun signInEmail(txtEmail: String, txtPassword: String) {
        mAuth.signInWithEmailAndPassword(txtEmail, txtPassword).addOnSuccessListener(this) {
            // jika user memasukan email dan passowrd benar maka langsung berpindah activity
            // data tidak disimpan disini karena data udah tersimpan waktu pendaftaran
            // jadi tidak perlu mengecek apakah user tersbeut akun baru atau tidak karena udah di validasi waktu registrasi
            if (txtEmail == "admin@gmail.com" && txtPassword == "adminonly") {
                signInAdmin()
            } else {
                signIn()
            }
        }.addOnFailureListener { e: Exception? ->
            if (e is FirebaseAuthInvalidUserException) {
                Toast.makeText(this@OnBoardingWithLoginActivity, "Email salah!", Toast.LENGTH_SHORT)
                    .show()
            } else if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@OnBoardingWithLoginActivity,
                    "Password salah!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // method to save data from input user
    // user tidak perlu menginputkan ulang email dan password
    private fun saveData() {
        val emailSave = sharedPreferences.getString(emailpref, "")
        val passwordSave = sharedPreferences.getString(passwordpref, "")

        edEmailSignIn.setText(emailSave)
        edPasswordSignIn.setText(passwordSave)
    }

    // method to make account with email
    private fun signUpEmail(txtEmail: String, txtPassword: String) {

        mAuth.createUserWithEmailAndPassword(txtEmail, txtPassword).addOnSuccessListener(this) {
            val email = mAuth.currentUser!!.email
            // jika email tidak null
            if (email != null) {
                // jika pengguna baru maka simpan data user terlebih dahulu ke dalam realtime database
                if (it.additionalUserInfo!!.isNewUser) {
                    Log.d(TAG, "Login sebagai pengguna baru : $email")
                    saveDataNewRegistration(email.toString())
                    signIn()
                } else {
                    // jika pengguna lama langsung berpindah activity saja
                    Log.d(TAG, "Login sebagai pengguna lama : $email")
                    signIn()
                }
            } else {
                // jika null maka tampilkan toast
                Toast.makeText(this@OnBoardingWithLoginActivity,
                    "Login gagal email anda null",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // view pager onboarding
    private fun setOnboardingViewPagerAdapter(onBoardingData: List<OnboardingData>) {
        onBoardingViewPager = findViewById(R.id.viewPager)
        onBoardingAdapter = OnBoardingAdapter(this, onBoardingData)
        onBoardingViewPager!!.adapter = onBoardingAdapter
        tabLayoutIndicator?.setupWithViewPager(onBoardingViewPager)
    }
}