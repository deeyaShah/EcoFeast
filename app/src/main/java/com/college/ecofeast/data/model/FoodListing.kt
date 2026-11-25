package com.college.ecofeast.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FoodListing(
    @DocumentId
    val id: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val foodName: String = "",
    val category: String = "",
    val description: String = "",
    val quantity: Double = 0.0,
    val quantityUnit: String = "Portions",
    val photoUrls: List<String> = emptyList(),
    val preparationDateTime: Long = System.currentTimeMillis(),
    val expiryDateTime: Long? = null,
    val foodCondition: String = "Freshly Prepared",
    val allergenNotes: String? = null,
    val pickupAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pickupInstructions: String? = null,
    val status: String = "available", // "available", "claimed", "completed", "cancelled"
    val claimantId: String? = null,
    val claimantName: String? = null,
    val claimedAt: Timestamp? = null,
    val receiverPickupConfirmed: Boolean = false,
    val receiverPickupConfirmedAt: Timestamp? = null,
    val donorHandoverConfirmed: Boolean = false,
    val donorHandoverConfirmedAt: Timestamp? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
