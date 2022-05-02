package com.berthstudios.meebarider

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.berthstudios.meebarider.Models.ProductInformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class RequestsFragment : Fragment() {

    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var requestRecyclerViewAdapter: RequestRecyclerViewAdapter
    lateinit var noRequestLayout: LinearLayout
    lateinit var requestsButton: ImageButton
    private lateinit var auth: FirebaseAuth
    var numberOfRequests = 0
    val orderTypes = arrayListOf<String>("Gye Mame", "Somame", "Gye Mame", "Somame", "Gye Mame")
    val db = Firebase.firestore
    lateinit var newRequests: ArrayList<ProductInformation>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_requests, container, false)
        newRequests = arrayListOf<ProductInformation>()
        noRequestLayout = view.findViewById(R.id.linear_no_requests)
        auth = Firebase.auth

        requestsRecyclerView = view.findViewById(R.id.recycler_view_requests)

        requestRecyclerViewAdapter = RequestRecyclerViewAdapter(newRequests, {position -> onItemTapped(position)  })

        requestsRecyclerView.adapter = requestRecyclerViewAdapter

        requestsButton = view.findViewById(R.id.image_button_go_to_requests)
        requestsButton.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragment_container_view, ProfileFragment())
            }
        }

        val docRef = db.collection("gyemame_request")
            .whereEqualTo("status", "Requested")
            .orderBy("createdTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                newRequests.clear()
            if(error != null) {
                Log.d("Requests", error.localizedMessage )
            }else {
                if (snapshot?.isEmpty == true) {
                    noRequestLayout.visibility = View.VISIBLE
                }else {
                    noRequestLayout.visibility = View.GONE
                    for (doc in snapshot?.documents!!) {

                        val pd = doc.toObject<ProductInformation>()
                        doc.id
                        Log.d("PD", pd?.itemType.toString())
                        if (pd != null) {
                            newRequests.add(pd)

                            requestsRecyclerView
                            Log.d("requestFragment", "items retrieved")
                        }
                    }

                    requestRecyclerViewAdapter.notifyDataSetChanged()
                }

            }
        }

        val docRef2 = db.collection("gyemame_request").whereEqualTo("status", "Accepted").whereEqualTo("riderID", auth.currentUser?.uid)
            .addSnapshotListener { snapshot, error ->

                if(error != null) {
                    Log.d("Requests", error.localizedMessage )
                }else {
                    if (snapshot?.isEmpty == true) {

                    }else {
                        val notif: CardView = view.findViewById(R.id.card_view_notification)
                        notif.visibility = View.VISIBLE

                        for (doc in snapshot?.documents!!) {
                            numberOfRequests++
                        }
                        val accepted:TextView = view.findViewById(R.id.text_view_accepted_requests)
                        accepted.text = "You have accepted ${numberOfRequests} requests"
                        Log.d("requestFragment", "You have accepted ${numberOfRequests} requests")
                    }

                }
            }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    fun onItemTapped(position: Int) {
//        Toast.makeText(activity, newRequests[position].itemType, Toast.LENGTH_LONG).show()

        if (numberOfRequests < 3) {
            val bundle = Bundle()
            bundle.putSerializable("productInformation", newRequests[position])
            findNavController().navigate(R.id.action_requestsFragment_to_detailFragment, bundle)
        }else {
            val alert = AlertDialog.Builder(activity)
            alert.apply {
                setTitle("Sorry you cannot accept anymore requests")
                setMessage("You have reached a maximum of 3 accepted requests")
                setPositiveButton("OK", null)
                show()
            }
        }

    }

}