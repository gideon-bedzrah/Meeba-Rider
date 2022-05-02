package com.berthstudios.meebarider

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.berthstudios.meebarider.Services.RiderLocation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    val db = Firebase.firestore
    val PERMISSION_ID = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val auth = Firebase.auth
        val currentUser = auth.currentUser

        val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        val editor = sharedPreferences?.edit()
        editor?.apply{
            putString("firstname", "Hello world")
            apply()
        }

        if (checkPermission()) {
            val serviceIntent = Intent(applicationContext, RiderLocation::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            }else {
                startService(serviceIntent)
            }
        }else {
            requestPermission()
        }


        bottomNavigationView = findViewById(R.id.bottom_nav)

        onRequestTapped()
//        getDeviceToken()

        bottomNavigationView.setOnItemSelectedListener { item ->

            when(item.itemId) {
                R.id.item_requests -> { onRequestTapped() }
                R.id.item_profile -> { onProfileTapped() }
                else -> false
            }
        }

        Firebase.messaging.subscribeToTopic("rider_requests")
            .addOnCompleteListener { task ->
                val TAG = "NEW"
                var msg = "subscribed successfully"
                if (!task.isSuccessful) {
                    msg = "subscription unsuccessfull"
                }
                Log.d(TAG, msg)
            }
    }

    fun onRequestTapped(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, HostFragment())
        }
        return true
    }

    fun onProfileTapped(): Boolean {

        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, ProfileFragment())
        }
        return true
    }


    fun getDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            val TAG = "new"

            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
            Toast.makeText(baseContext, token, Toast.LENGTH_LONG).show()
        })
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }
    fun checkPermission(): Boolean {
        // true: if we have permission
        //false: if we don't have permission

        if (
            this.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
            == PackageManager.PERMISSION_GRANTED || this.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false

    }
}