package com.berthstudios.meebarider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berthstudios.meebarider.Models.ProductInformation
import org.w3c.dom.Text
import java.text.SimpleDateFormat

class RiderTripAdapter(val requestItems: ArrayList<ProductInformation>, val onItemClicked: (position: Int) -> Unit): RecyclerView.Adapter<RiderTripAdapter.viewHolder>(){

    inner class viewHolder(itemView: View,
                           private val onItemClicked: (position: Int) -> Unit): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val itemType: TextView
        val totalDistance: TextView
        val requestDate: TextView
        val pickuplocation: TextView
        val dropofflocation: TextView
        val totalAmount: TextView
        val trackingNumber: TextView

        init {
            itemType = itemView.findViewById(R.id.text_view_item_type)
            totalDistance = itemView.findViewById(R.id.text_view_total_distance)
            requestDate = itemView.findViewById(R.id.text_view_request_time)
            pickuplocation = itemView.findViewById(R.id.text_view_pickup_location)
            dropofflocation = itemView.findViewById(R.id.text_view_dropoff_location)
            totalAmount = itemView.findViewById(R.id.text_view_total_amount)
            trackingNumber = itemView.findViewById(R.id.text_view_requester_info)

            itemView.setOnClickListener(this)

        }
        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trips, parent, false)
        return viewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.itemType.text = requestItems[position].itemType
        holder.totalDistance.text = requestItems[position].deliveryInformation?.distance
        holder.pickuplocation.text = requestItems[position].deliveryInformation?.pickuplocation?.name
        holder.dropofflocation.text = requestItems[position].deliveryInformation?.dropofflocation?.name
        holder.totalAmount.text = requestItems[position].deliveryInformation?.billingInformation?.total.toString()
        val dateFormated = SimpleDateFormat("HH:mm a MMM dd, yy").format(requestItems[position].deliveryInformation?.requestTime)
        holder.requestDate.text = dateFormated
        holder.trackingNumber.text = requestItems[position].deliveryInformation?.trackingNumber
    }

    override fun getItemCount(): Int {
        return requestItems.size
    }
}