package com.berthstudios.meebarider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berthstudios.meebarider.Models.ProductInformation
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class RequestRecyclerViewAdapter(val requests: ArrayList<ProductInformation>, val onItemClicked: (position: Int) -> Unit): RecyclerView.Adapter<RequestRecyclerViewAdapter.RequestRecyclerViewHolder>() {


    inner class RequestRecyclerViewHolder(itemView: View,
                                          private val onItemClicked: (position: Int) -> Unit): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val itemType: TextView
        val deliveryType: TextView
        val pickLocation: TextView
        val dropofflocation: TextView
        val requestTime: TextView
        val trackingNumber: TextView

        init {
            itemType = itemView.findViewById(R.id.text_view_item_type)
            deliveryType = itemView.findViewById(R.id.text_view_delivery_type)
            pickLocation = itemView.findViewById(R.id.text_view_pickup_location)
            dropofflocation = itemView.findViewById(R.id.text_view_dropoff_location)
            requestTime = itemView.findViewById(R.id.text_view_request_time)
            trackingNumber = itemView.findViewById(R.id.text_view_requester_info)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_request_item, parent, false)
        return RequestRecyclerViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: RequestRecyclerViewHolder, position: Int) {
       holder.itemType.text = requests[position].itemType
        holder.deliveryType.text = requests[position].deliveryInformation?.deliveryType
        holder.dropofflocation.text = requests[position].deliveryInformation?.dropofflocation?.name
        holder.pickLocation.text = requests[position].deliveryInformation?.pickuplocation?.name
        holder.trackingNumber.text = requests[position].deliveryInformation?.trackingNumber
        val dateFormated = SimpleDateFormat("HH:mm a MMM dd, yy")
            .format(requests[position].deliveryInformation?.requestTime)
        holder.requestTime.text = dateFormated
    }

    override fun getItemCount(): Int {
       return requests.size
    }
}