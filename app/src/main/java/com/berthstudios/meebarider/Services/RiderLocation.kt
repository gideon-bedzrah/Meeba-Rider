package com.berthstudios.meebarider.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.berthstudios.meebarider.R
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

data class UpdateLocation(
    val latitude: Double? =null,
    val longitude: Double? =null
)

class RiderLocation: Service(), LocationListener {

    val db = Firebase.firestore
   lateinit var auth: FirebaseAuth

    private val TAG = "DL_LOCATION"

    private var location: Location? = null

    private var locationRequest: LocationRequest? = null

    override fun onBind(intent: Intent?): IBinder? {
       return null
    }



    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "smt_location"
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name) + " using your location",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdate()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startLocationUpdate() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 2500

        val builder = LocationSettingsRequest.Builder()
        locationRequest?.let { locReq ->
            builder.addLocationRequest(locReq)
            val locationSettingRequest = builder.build()

            val locationSetting = LocationServices.getSettingsClient(this)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                getFusedLocationProviderClient(this).requestLocationUpdates(
                    locReq,
                    object : LocationCallback() {
                        override fun onLocationResult(p0: LocationResult?) {
                            p0?.lastLocation?.let { lastLocation ->
                                onLocationChanged(lastLocation)
                            }
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }

    }

    override fun onLocationChanged(location: Location) {

//        Log.d(TAG, "onLocationChanged:${location.latitude}, ${location.longitude}")
//        val myLocation = MyLocation(latitude = p0.latitude, longitude = p0.longitude)
        val updateLocation = UpdateLocation(latitude = location.latitude, longitude = location.longitude)
//        dbRef?.child("driver_points")?.setValue(myLocation)
        db.collection("riders").document(auth.currentUser.toString()).collection("current_location").document("location").set(updateLocation)
    }


}