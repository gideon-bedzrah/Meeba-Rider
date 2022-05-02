package com.berthstudios.meebarider

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.berthstudios.meeba.Api.DirectionsModel
import com.berthstudios.meebarider.Models.ProductInformation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


data class TripInformation (
    val docID:String? = null,
    val pickup: Boolean = false,
    val pickupTime: Date? = null,
    val dropoff: Boolean = false,
    val dropOffTime: Date? = null,
//    val payInCash: Boolean? = false
)

class DetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var backTapped: LinearLayout
    private lateinit var viewOnMap: TextView
    private var productInformation: ProductInformation? = null
    private lateinit var mMap: GoogleMap
    val db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var requesterName: TextView
    lateinit var requesterNumber: TextView
    lateinit var paymentOption: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        auth = Firebase.auth
        productInformation = arguments?.getSerializable("productInformation") as ProductInformation?
        var mapFragment = SupportMapFragment()
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requesterName = view.findViewById(R.id.text_view_requester_name)
        requesterNumber = view.findViewById(R.id.text_view_requester_number)
        paymentOption = view.findViewById(R.id.text_view_payment_option)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backTapped = view.findViewById(R.id.linear_layout_requests_back)
        viewOnMap = view.findViewById(R.id.text_view_view_on_map)

        val pickupLocation: TextView = view.findViewById(R.id.text_view_pickup_location)
        pickupLocation.text = productInformation?.deliveryInformation?.pickuplocation?.name

        val dropoffLocation: TextView = view.findViewById(R.id.text_view_dropoff_location)
        dropoffLocation.text = productInformation?.deliveryInformation?.dropofflocation?.name

        val total: TextView = view.findViewById(R.id.text_view_total)
        total.text =
            "Ghs " + productInformation?.deliveryInformation?.billingInformation?.total.toString()

        val trackingNumber: TextView = view.findViewById(R.id.text_view_id_number)
        trackingNumber.text = productInformation?.deliveryInformation?.trackingNumber

        val itemType: TextView = view.findViewById(R.id.text_view_item_type)
        itemType.text = productInformation?.itemType + ": " + productInformation?.quantity

        val deliveryType: TextView = view.findViewById(R.id.text_view_delivery_type)
        deliveryType.text = productInformation?.deliveryInformation?.deliveryType

        val dateFormated = SimpleDateFormat("HH:mm a MMM dd, yy").format(productInformation?.deliveryInformation?.requestTime)
        val requestTime: TextView = view.findViewById(R.id.text_view_request_time)
        requestTime.text = dateFormated
        paymentOption.text = productInformation?.deliveryInformation?.billingInformation?.paymentOption

        //product information
        getUserInformation()

        backTapped.setOnClickListener(View.OnClickListener {
            findNavController().navigateUp()
        })

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)

        viewOnMap.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val view = View.inflate(activity, R.layout.dialogue_image_popover, null)
                val builder = AlertDialog.Builder(context)
                builder.setView(view)

//                new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
//                    .execute(MY_URL_STRING)
                DownloadImageTask(view.findViewById(R.id.image_view_item)).execute(productInformation?.imageUrl)
                val dialog = builder.create()
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            }

        })

        val acceptButton: Button = view.findViewById(R.id.button_accept_trip)
        acceptButton.setOnClickListener {

            val currentUser = auth.currentUser
            db.collection("gyemame_request").document(productInformation?.docID.toString())
                .update("status", "Accepted", "riderID", currentUser?.uid).addOnCompleteListener {

                if(!it.isSuccessful) {
                    val builder = AlertDialog.Builder(activity)
                    builder.apply {
                        setTitle("Trip Failed")
                        setMessage("Failed to process accept request, please try again.")
                        setPositiveButton("Ok", null)
                    }
                    builder.show()
                }else {

                   val doc = db.collection("gyemame_request").document(productInformation?.docID.toString())
                        .collection("trip_information").document("1")

                        doc.set(TripInformation()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val builder = AlertDialog.Builder(activity)
                                builder.apply {
                                    setTitle("Pickup Location")
                                    setMessage("Do you want directions to the pickup location?")
                                    setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                                        val gmmIntentUri =
                                            Uri.parse("google.navigation:q=${productInformation?.deliveryInformation?.pickuplocation?.latitude}" +
                                                    ",${productInformation?.deliveryInformation?.pickuplocation?.longitude}&mode=d")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        startActivity(mapIntent)
                                        activity?.finish()
                                    })
                                    setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                                        activity?.supportFragmentManager?.commit {
                                            replace(R.id.fragment_container_view, ProfileFragment())
                                        }

                                    })
                                    show()
                                }


                            }
                        }

                }
            }


        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        GetDirecton(
            getDirectionURL(
                productInformation?.deliveryInformation?.pickuplocation?.latitude!!,
                productInformation?.deliveryInformation?.pickuplocation?.longitude!!,
                productInformation?.deliveryInformation?.dropofflocation?.latitude!!,
                productInformation?.deliveryInformation?.dropofflocation?.longitude!!
            )
        ).execute()


    }

    fun getDirectionURL(
        originLat: Double,
        originLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${originLat},${originLng}&destination=${destinationLat},${destinationLng}&key=AIzaSyDepITIDVYmZzlrrRUsw36PckIj2BSMSHU"
    }

    inner class GetDirecton(val url: String) : AsyncTask<Void, Void, DirectionsModel>() {
        override fun doInBackground(vararg params: Void?): DirectionsModel? {
            val client = com.squareup.okhttp.OkHttpClient()
            val request = com.squareup.okhttp.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()
            try {
                val respObj = Gson().fromJson(data.string(), DirectionsModel::class.java)

                return respObj
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: DirectionsModel?) {

            val path = ArrayList<LatLng>()
            if (result != null) {
                for (i in 0..(result.routes[0].legs[0].steps.size - 1)) {
                    val startlatLng = LatLng(
                        result.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        result.routes[0].legs[0].steps[i].start_location.lng.toDouble()
                    )
                    path.add(startlatLng)
                    val endlatLng = LatLng(
                        result.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
                        result.routes[0].legs[0].steps[i].end_location.lng.toDouble()
                    )
                    path.add(endlatLng)
                }
                val polylineOption = PolylineOptions()
                for (i in path.indices) {
                    polylineOption.add(path[i])
                    polylineOption.width(10f)
                    polylineOption.geodesic(true)
                }
                mMap.addPolyline(polylineOption)

                val startMarker = LatLng(
                    productInformation?.deliveryInformation?.pickuplocation?.latitude!!,
                    productInformation?.deliveryInformation?.pickuplocation?.longitude!!
                )

                val endMarker = LatLng(
                    productInformation?.deliveryInformation?.dropofflocation?.latitude!!,
                    productInformation?.deliveryInformation?.dropofflocation?.longitude!!
                )

                val builder = LatLngBounds.Builder()
                builder.include(startMarker)
                builder.include(endMarker)
                val bounds = builder.build()

                // move camera to Ghana region
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))
            }


        }

    }
    private fun getUserInformation() {

        db.collection("users").document(productInformation?.userID!!).get()
            .addOnSuccessListener { document ->

                if (document.data != null) {

                    val firstname = document.data?.get("firstname").toString()
                    val lastname = document.data?.get("lastname").toString()
                    val emailString = document.data?.get("email").toString()

                    requesterName.text = firstname + " " + lastname
                    requesterNumber.text = emailString

                }else {
                    Toast.makeText(context, "No such user!", Toast.LENGTH_LONG).show()
                }

            }.addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }

    }
}

class DownloadImageTask(bmImage: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {
    var bmImage: ImageView
    protected override fun doInBackground(vararg params: String?): Bitmap? {
        val urldisplay = params[0]
        var mIcon11: Bitmap? = null
        try {
            val `in`: InputStream = URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(`in`)
        } catch (e: java.lang.Exception) {
            Log.e("Error", e.message.toString())
            e.printStackTrace()
        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }

    init {
        this.bmImage = bmImage
    }
}

