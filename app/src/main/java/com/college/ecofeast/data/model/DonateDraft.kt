package com.college.ecofeast.data.model

data class DonateDraft(
    val wizardStep: Int = 1,
    val isWizardActive: Boolean = true,
    val foodName: String = "",
    val category: String = "",
    val description: String = "",
    val quantityText: String = "",
    val quantityUnit: String = "Portions",
    val localPhotoPaths: List<String> = emptyList(), // Absolute file paths inside app private storage
    val preparationTimestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long? = null,
    val foodCondition: String = "Freshly Prepared",
    val allergenNotes: String = "",
    val pickupAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pickupInstructions: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
