package com.berthstudios.meebarider.Models

import java.io.Serializable
import java.util.*

data class ProductInformation(
    val status: String? = null,
    val riderID: String? = null,
    val docID: String? = null,
    val userID: String? = null,
    val itemType: String? = null,
    val quantity: Double? = null,
    val createdTime: Date? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val deliveryInformation: DeliveryInformation? = null
) : Serializable

data class DeliveryInformation (
    val pickuplocation: LocationInformation? = null,
    val pickupContact: ContactInformation? = null,
    val dropoffContact: ContactInformation? = null,
    val dropofflocation: LocationInformation? = null,
    val trackingNumber: String? = null,
    val averageTime: String? = null,
    val amount: Double? = null,
    val distance: String? = null,
    val deliveryType: String? = null,
    val requestTime: Date? = null,
    val billingInformation: BillingInformation? = null,
): Serializable

data class LocationInformation (
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeID: String? = null
    ): Serializable

data class BillingInformation(
    val paymentOption: String? = null,
    var paidby: String? = null,
    var transactionID: String? = null,
    val deliveryCharge: Double? = null,
    val serviceCost: Double? = null,
    val total: Double? = null
): Serializable

data class ContactInformation(
    val name: String? = null,
    val number : String? = null
): Serializable