package com.berthstudios.meebarider

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.berthstudios.meebarider.Models.ProductInformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var riderTripAdapter: RiderTripAdapter
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    lateinit var requests: ArrayList<ProductInformation>
    val sharedPreferences = activity?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
    lateinit var fullname: TextView
    lateinit var email: TextView
    lateinit var phonenumber: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        requests = arrayListOf<ProductInformation>()
        auth = Firebase.auth

        fullname = view.findViewById(R.id.text_view_fullname)
        email = view.findViewById(R.id.text_view_email)
        phonenumber = view.findViewById(R.id.text_view_phone)

        recyclerView = view.findViewById(R.id.recycler_view_rider_trips)
        riderTripAdapter = RiderTripAdapter(requests, {position -> onItemTapped(position) })
        recyclerView.adapter = riderTripAdapter

        getRiderInformation()
        val today = Date()

        val docRef = db.collection("gyemame_request").whereEqualTo("status", "Accepted")
            .whereEqualTo("riderID", auth.currentUser?.uid)
            .orderBy("createdTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if(error != null) {
                    Log.d("Requests", error.localizedMessage )
                }else {
                    for (doc in snapshot?.documents!!) {

                        val pd = doc.toObject<ProductInformation>()
                        Log.d("PD", pd?.itemType.toString())
                        if (pd != null) {
                            requests.add(pd)
                        }
                    }
                    riderTripAdapter.notifyDataSetChanged()
                    val requestTotal: TextView = view.findViewById(R.id.text_view_total_requests)
                    val amountTotal: TextView = view.findViewById(R.id.text_view_amount)
                    requestTotal.text = requests.size.toString()
                    var amount = 0
                     for (t in requests) {
                       amount = (amount + t.deliveryInformation?.billingInformation?.total!!).toInt()
                    }
                    amountTotal.text = amount.toString()
                }
            }

        val signout: CardView = view.findViewById(R.id.card_view_sign_out)
        signout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.apply {
                setPositiveButton("Ok", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        Firebase.messaging.unsubscribeFromTopic("rider_requests")
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    activity?.startActivity(Intent(context, NewLoginActivity::class.java))
                                    activity?.finish()
                                    Firebase.auth.signOut()
                                }

                            }
                        if (sharedPreferences != null) {
                            sharedPreferences.edit().clear().commit()
                        }

                    }
                })
                setNegativeButton("Cancel", null)
                setTitle("Sign out")
                setMessage("Are you sure you want to sign out?")
            }
            builder.show()
        }

        return view
    }

    fun onItemTapped(position: Int) {
//        Toast.makeText(activity, newRequests[position].itemType, Toast.LENGTH_LONG).show()
        val intent = Intent(activity, CurrentItemActivity::class.java)
        intent.putExtra("requests", requests[position])
        startActivity(intent)
    }

   private fun getRiderInformation() {
       val currentUser = auth.currentUser

        db.collection("riders").document(currentUser?.uid.toString()).get().addOnSuccessListener { document ->

            if (document != null) {

                val firstname = document.data?.get("firstname").toString()
                val lastname = document.data?.get("lastname").toString()
                val emailString = document.data?.get("email").toString()
                val phone = document.data?.get("phoneNumber").toString()

                fullname.text = firstname + " " + lastname
                email.text = emailString
                phonenumber.text = phone
            }else {
                Toast.makeText(context, "No such rider!", Toast.LENGTH_LONG).show()
            }

        }.addOnFailureListener {
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

}