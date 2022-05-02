package com.berthstudios.meebarider

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.berthstudios.meebarider.Models.ProductInformation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class CurrentItemActivity : AppCompatActivity() {

    lateinit var  itemType: TextView
    lateinit var  deliveryType: TextView
    lateinit var totalDistance: TextView
    lateinit var averageTime: TextView
    lateinit var requestDate: TextView
    lateinit var pickuplocation: TextView
    lateinit var dropofflocation: TextView
    lateinit var totalAmount: TextView
    lateinit var pickupcheckmark: CheckBox
    lateinit var dropoffcheckmark: CheckBox
    lateinit var trackingNumber: TextView
    lateinit var pickDirection: TextView
    lateinit var dropDirection: TextView
    lateinit var requesterName: TextView
    lateinit var requesterNumber: TextView
    lateinit var paymentOption: TextView

    lateinit var pickupname: TextView
    lateinit var pickupnumber: TextView
    lateinit var pickupPhone: ImageButton

    lateinit var dropoffname: TextView
    lateinit var dropoffnumber: TextView
    lateinit var dropoffPhone: ImageButton
    lateinit var tapToViewItem: TextView

    val db = Firebase.firestore

    var requests: ProductInformation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_item)
        requests = intent.getSerializableExtra("requests") as ProductInformation

        deliveryType = findViewById(R.id.text_view_delivery_type)
        requestDate = findViewById(R.id.text_view_request_time)
        averageTime = findViewById(R.id.text_view_total_mins)
        pickuplocation = findViewById(R.id.text_view_pickup_location)
        dropofflocation = findViewById(R.id.text_view_dropoff_location)
        totalAmount = findViewById(R.id.text_view_total_amount)
        trackingNumber = findViewById(R.id.text_view_tracking_number)
        pickupcheckmark = findViewById(R.id.check_mark_pick_up)
        dropoffcheckmark = findViewById(R.id.check_mark_drop_off)
        pickDirection = findViewById(R.id.text_view_get_pick_location)
        dropDirection = findViewById(R.id.text_view_get_drop_location)
        requesterName = findViewById(R.id.text_view_requester_name)
        requesterNumber = findViewById(R.id.text_view_requester_number)
        paymentOption = findViewById(R.id.text_view_payment_option)
        itemType = findViewById(R.id.text_view_item_type)


        pickupname = findViewById(R.id.text_view_pickup_name)
        pickupnumber = findViewById(R.id.text_view_pickup_number)
        pickupPhone = findViewById(R.id.image_button_pickup_contact)

        dropoffname = findViewById(R.id.text_view_dropoff_name)
        dropoffnumber = findViewById(R.id.text_view_dropoff_number)
        dropoffPhone = findViewById(R.id.image_button_dropoff_contact)

        tapToViewItem = findViewById(R.id.text_view_view_item)

        tapToViewItem.setOnClickListener {
            val view = View.inflate(this, R.layout.dialogue_image_popover, null)
            val builder = android.app.AlertDialog.Builder(this)
            builder.setView(view)

            DownloadImageTask(view.findViewById(R.id.image_view_item)).execute(requests?.imageUrl)
            val dialog = builder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        deliveryType.text = requests?.deliveryInformation?.deliveryType
        val dateFormated = SimpleDateFormat("HH:mm a MMM dd, yy").format(requests?.deliveryInformation?.requestTime)
        requestDate.text = dateFormated
        averageTime.text = requests?.deliveryInformation?.averageTime
        pickuplocation.text = requests?.deliveryInformation?.pickuplocation?.name
        dropofflocation.text = requests?.deliveryInformation?.dropofflocation?.name
        totalAmount.text = "GHS "+ requests?.deliveryInformation?.billingInformation?.total.toString()
        trackingNumber.text = requests?.deliveryInformation?.trackingNumber.toString()
        pickupname.text = requests?.deliveryInformation?.pickupContact?.name
        dropoffname.text = requests?.deliveryInformation?.dropoffContact?.name
        pickupnumber.text = requests?.deliveryInformation?.pickupContact?.number
        dropoffnumber.text = requests?.deliveryInformation?.dropoffContact?.number
        itemType.text = requests?.itemType

        pickupPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                val REQUEST_CODE = 0
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),
                    REQUEST_CODE)

            } else {
                if (requests?.deliveryInformation?.pickupContact?.number != null) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + requests?.deliveryInformation?.pickupContact?.number))
                    startActivity(intent)
                }
            }
        }

        dropoffPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                val REQUEST_CODE = 0
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),
                    REQUEST_CODE)

            } else {

                if (requests?.deliveryInformation?.dropoffContact?.number != null) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + requests?.deliveryInformation?.dropoffContact?.number))
                    startActivity(intent)
                }

            }
        }



        paymentOption.text =
            requests?.deliveryInformation?.billingInformation?.paymentOption ?: ""
        pickDirection.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=${requests?.deliveryInformation?.pickuplocation?.latitude}" +
                        ",${requests?.deliveryInformation?.pickuplocation?.longitude}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }


        getUserInformation()
        dropDirection.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=${requests?.deliveryInformation?.dropofflocation?.latitude}" +
                        ",${requests?.deliveryInformation?.dropofflocation?.longitude}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }



        db.collection("gyemame_request").document(requests?.docID.toString())
            .collection("trip_information").document("1").get().addOnSuccessListener { snap ->

                if (snap.data != null) {

                   if (snap.data!!.get("pickup") == true) {
                       pickupcheckmark.visibility = View.INVISIBLE
                   }else {
                       pickupcheckmark.visibility = View.VISIBLE
                   }

                    if (snap.data!!.get("dropoff") == true) {
                        dropoffcheckmark.visibility = View.INVISIBLE
                    }else {
                        dropoffcheckmark.visibility = View.VISIBLE
                    }
                }

            }

        pickupcheckmark.setOnClickListener {

            if (pickupcheckmark.isChecked) {

                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setTitle("Comfirm Pick")
                    setMessage("Has item been picked up?")
                    setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        db.collection("gyemame_request").document(requests?.docID.toString())
                            .collection("trip_information").document("1")
                            .update("pickup", true, "pickupTime", Date()).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    it.visibility = View.INVISIBLE
                                }
                            }
                    })
                    setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        pickupcheckmark.isChecked = false
                    })
                    show()
                }

            }

        }



        dropoffcheckmark.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Comfirm Dropoff")
                setMessage("Has item been dropped off?")
                setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    db.collection("gyemame_request").document(requests?.docID.toString())
                        .collection("trip_information").document("1")
                        .update("dropoff", true, "dropoffTime", Date()).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                it.visibility = View.INVISIBLE
                            }
                        }
                })
                setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    dropoffcheckmark.isChecked = false
                })
                show()
            }

            val completeButton: Button = findViewById(R.id.button_complete_trip)
            completeButton.setOnClickListener{
                db.collection("gyemame_request").document(requests?.docID.toString()).update("status", "Completed").addOnSuccessListener {
                    completeButton.visibility = View.INVISIBLE
                }
            }


        }



    }

    private fun getUserInformation() {

        db.collection("users").document(requests?.userID!!).get()
            .addOnSuccessListener { document ->

            if (document.data != null) {

                val firstname = document.data?.get("firstname").toString()
                val lastname = document.data?.get("lastname").toString()
                val emailString = document.data?.get("email").toString()

                requesterName.text = firstname + " " + lastname
                requesterNumber.text = emailString

            }else {
                Toast.makeText(this, "No such user!", Toast.LENGTH_LONG).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
        }

    }
}