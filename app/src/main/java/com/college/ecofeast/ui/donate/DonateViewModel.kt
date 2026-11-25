package com.college.ecofeast.ui.donate

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.college.ecofeast.data.model.DonateDraft
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.data.repository.DonateDraftRepository
import com.college.ecofeast.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DonateSubScreen {
    ENTRY,
    WIZARD,
    SUCCESS,
    MY_DONATIONS_LIST,
    MY_DONATIONS_DETAIL
}

data class DonateUiState(
    val subScreen: DonateSubScreen = DonateSubScreen.ENTRY,
    val wizardStep: Int = 1, // 1..5
    val hasDraft: Boolean = false,
    val isDraftLoaded: Boolean = false,
    
    // Step 1: Basic Details
    val foodName: String = "",
    val foodNameError: String? = null,
    val category: String = "",
    val categoryError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val quantityText: String = "",
    val quantityTextError: String? = null,
    val quantityUnit: String = "Portions",

    // Step 2: Photos
    val selectedPhotoPaths: List<String> = emptyList(),
    val photoError: String? = null,

    // Step 3: Additional Details
    val preparationTimestamp: Long = System.currentTimeMillis(),
    val preparationError: String? = null,
    val expiryTimestamp: Long? = null,
    val expiryError: String? = null,
    val foodCondition: String = "Freshly Prepared",
    val allergenNotes: String = "",

    // Step 4: Pickup Location
    val pickupAddress: String = "",
    val pickupAddressError: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pickupInstructions: String = "",

    // Step 5: Review & Submit
    val isConfirmed: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,

    // My Donations
    val userListings: List<FoodListing> = emptyList(),
    val selectedListingTab: Int = 0, // 0 = Active, 1 = Past
    val selectedListing: FoodListing? = null,
    val isLoadingListings: Boolean = false
)

class DonateViewModel(application: Application) : AndroidViewModel(application) {

    private val draftRepository = DonateDraftRepository(application.applicationContext)
    private val foodRepository = FoodRepository()

    private val initialDraft = draftRepository.getDraftSync()

    private val _uiState = MutableStateFlow(
        if (initialDraft != null && initialDraft.isWizardActive) {
            DonateUiState(
                subScreen = DonateSubScreen.WIZARD,
                wizardStep = initialDraft.wizardStep,
                hasDraft = true,
                foodName = initialDraft.foodName,
                category = initialDraft.category,
                description = initialDraft.description,
                quantityText = initialDraft.quantityText,
                quantityUnit = initialDraft.quantityUnit,
                selectedPhotoPaths = initialDraft.localPhotoPaths,
                preparationTimestamp = initialDraft.preparationTimestamp,
                expiryTimestamp = initialDraft.expiryTimestamp,
                foodCondition = initialDraft.foodCondition,
                allergenNotes = initialDraft.allergenNotes,
                pickupAddress = initialDraft.pickupAddress,
                latitude = initialDraft.latitude,
                longitude = initialDraft.longitude,
                pickupInstructions = initialDraft.pickupInstructions
            )
        } else {
            DonateUiState(hasDraft = initialDraft != null)
        }
    )
    val uiState: StateFlow<DonateUiState> = _uiState.asStateFlow()

    init {
        checkDraftStatus()
    }

    fun checkDraftStatus() {
        viewModelScope.launch {
            val draft = draftRepository.getDraft()
            val hasDraft = draft != null
            if (draft != null && draft.isWizardActive) {
                // Auto-restore active wizard flow (e.g. returning from external camera activity)
                _uiState.update {
                    it.copy(
                        subScreen = DonateSubScreen.WIZARD,
                        wizardStep = draft.wizardStep,
                        hasDraft = true,
                        foodName = draft.foodName,
                        category = draft.category,
                        description = draft.description,
                        quantityText = draft.quantityText,
                        quantityUnit = draft.quantityUnit,
                        selectedPhotoPaths = draft.localPhotoPaths,
                        preparationTimestamp = draft.preparationTimestamp,
                        expiryTimestamp = draft.expiryTimestamp,
                        foodCondition = draft.foodCondition,
                        allergenNotes = draft.allergenNotes,
                        pickupAddress = draft.pickupAddress,
                        latitude = draft.latitude,
                        longitude = draft.longitude,
                        pickupInstructions = draft.pickupInstructions
                    )
                }
            } else {
                _uiState.update { it.copy(hasDraft = hasDraft) }
            }
        }
    }

    fun startNewDonation() {
        viewModelScope.launch {
            draftRepository.clearDraft()
            _uiState.update {
                DonateUiState(
                    subScreen = DonateSubScreen.WIZARD,
                    wizardStep = 1,
                    hasDraft = false,
                    userListings = it.userListings
                )
            }
        }
    }

    fun continueDraft() {
        viewModelScope.launch {
            val draft = draftRepository.getDraft()
            if (draft != null) {
                _uiState.update {
                    it.copy(
                        subScreen = DonateSubScreen.WIZARD,
                        wizardStep = draft.wizardStep,
                        foodName = draft.foodName,
                        category = draft.category,
                        description = draft.description,
                        quantityText = draft.quantityText,
                        quantityUnit = draft.quantityUnit,
                        selectedPhotoPaths = draft.localPhotoPaths,
                        preparationTimestamp = draft.preparationTimestamp,
                        expiryTimestamp = draft.expiryTimestamp,
                        foodCondition = draft.foodCondition,
                        allergenNotes = draft.allergenNotes,
                        pickupAddress = draft.pickupAddress,
                        latitude = draft.latitude,
                        longitude = draft.longitude,
                        pickupInstructions = draft.pickupInstructions
                    )
                }
            } else {
                startNewDonation()
            }
        }
    }

    fun discardDraft() {
        viewModelScope.launch {
            draftRepository.clearDraft()
            _uiState.update {
                it.copy(
                    hasDraft = false,
                    subScreen = DonateSubScreen.ENTRY
                )
            }
        }
    }

    private fun autoSaveDraft() {
        val state = _uiState.value
        if (state.subScreen != DonateSubScreen.WIZARD) return

        viewModelScope.launch {
            val draft = DonateDraft(
                wizardStep = state.wizardStep,
                isWizardActive = true,
                foodName = state.foodName,
                category = state.category,
                description = state.description,
                quantityText = state.quantityText,
                quantityUnit = state.quantityUnit,
                localPhotoPaths = state.selectedPhotoPaths,
                preparationTimestamp = state.preparationTimestamp,
                expiryTimestamp = state.expiryTimestamp,
                foodCondition = state.foodCondition,
                allergenNotes = state.allergenNotes,
                pickupAddress = state.pickupAddress,
                latitude = state.latitude,
                longitude = state.longitude,
                pickupInstructions = state.pickupInstructions,
                lastUpdated = System.currentTimeMillis()
            )
            draftRepository.saveDraft(draft)
            _uiState.update { it.copy(hasDraft = true) }
        }
    }

    // Step Navigation
    fun goToNextStep() {
        val state = _uiState.value
        val isValid = when (state.wizardStep) {
            1 -> validateStep1()
            2 -> validateStep2()
            3 -> validateStep3()
            4 -> validateStep4()
            else -> true
        }

        if (isValid && state.wizardStep < 5) {
            _uiState.update { it.copy(wizardStep = it.wizardStep + 1) }
            autoSaveDraft()
        }
    }

    fun goToPreviousStep() {
        val state = _uiState.value
        if (state.wizardStep > 1) {
            _uiState.update { it.copy(wizardStep = it.wizardStep - 1) }
            autoSaveDraft()
        } else {
            // Exit wizard back to entry screen
            _uiState.update { it.copy(subScreen = DonateSubScreen.ENTRY) }
            viewModelScope.launch {
                val draft = draftRepository.getDraft()
                if (draft != null) {
                    draftRepository.saveDraft(draft.copy(isWizardActive = false))
                }
            }
        }
    }

    fun jumpToStep(step: Int) {
        if (step in 1..5) {
            _uiState.update { it.copy(wizardStep = step) }
            autoSaveDraft()
        }
    }

    fun navigateTo(subScreen: DonateSubScreen) {
        _uiState.update { it.copy(subScreen = subScreen) }
    }

    // Step 1 Form Updates
    fun updateFoodName(value: String) {
        _uiState.update { it.copy(foodName = value, foodNameError = null) }
        autoSaveDraft()
    }

    fun updateCategory(value: String) {
        _uiState.update { it.copy(category = value, categoryError = null) }
        autoSaveDraft()
    }

    fun updateDescription(value: String) {
        val trimmed = if (value.length > 500) value.substring(0, 500) else value
        _uiState.update { it.copy(description = trimmed, descriptionError = null) }
        autoSaveDraft()
    }

    fun updateQuantityText(value: String) {
        _uiState.update { it.copy(quantityText = value, quantityTextError = null) }
        autoSaveDraft()
    }

    fun updateQuantityUnit(value: String) {
        _uiState.update { it.copy(quantityUnit = value) }
        autoSaveDraft()
    }

    // Step 2 Photos Updates
    fun addPhotoUri(uri: Uri) {
        viewModelScope.launch {
            val localPath = draftRepository.copyUriToDraftStorage(uri)
            if (localPath != null) {
                _uiState.update {
                    val currentList = it.selectedPhotoPaths.toMutableList()
                    if (currentList.size < 5) {
                        currentList.add(localPath)
                    }
                    it.copy(
                        subScreen = DonateSubScreen.WIZARD,
                        wizardStep = 2,
                        selectedPhotoPaths = currentList,
                        photoError = null
                    )
                }
                autoSaveDraft()
            }
        }
    }

    fun removePhotoPath(path: String) {
        _uiState.update {
            val currentList = it.selectedPhotoPaths.toMutableList()
            currentList.remove(path)
            it.copy(selectedPhotoPaths = currentList)
        }
        autoSaveDraft()
    }

    // Step 3 Additional Details Updates
    fun updatePreparationTimestamp(ts: Long) {
        _uiState.update { it.copy(preparationTimestamp = ts, preparationError = null) }
        autoSaveDraft()
    }

    fun updateExpiryTimestamp(ts: Long?) {
        _uiState.update { it.copy(expiryTimestamp = ts, expiryError = null) }
        autoSaveDraft()
    }

    fun updateFoodCondition(condition: String) {
        _uiState.update { it.copy(foodCondition = condition) }
        autoSaveDraft()
    }

    fun updateAllergenNotes(notes: String) {
        val trimmed = if (notes.length > 200) notes.substring(0, 200) else notes
        _uiState.update { it.copy(allergenNotes = trimmed) }
        autoSaveDraft()
    }

    // Step 4 Location Updates
    fun updatePickupAddress(address: String) {
        _uiState.update { it.copy(pickupAddress = address, pickupAddressError = null) }
        autoSaveDraft()
    }

    fun updateLocation(lat: Double, lng: Double, addressName: String? = null) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                pickupAddress = if (!addressName.isNullOrBlank()) addressName else it.pickupAddress,
                pickupAddressError = null
            )
        }
        autoSaveDraft()
    }

    fun updatePickupInstructions(instructions: String) {
        val trimmed = if (instructions.length > 200) instructions.substring(0, 200) else instructions
        _uiState.update { it.copy(pickupInstructions = trimmed) }
        autoSaveDraft()
    }

    // Step 5 Review & Confirm Updates
    fun setConfirmed(confirmed: Boolean) {
        _uiState.update { it.copy(isConfirmed = confirmed) }
    }

    // Quantity Helper Parser
    private fun parseQuantityAndUnit(input: String): Pair<Double?, String> {
        val trimmed = input.trim()
        val regex = Regex("""^\s*(\d+(?:\.\d+)?)\s*(.*?)\s*$""")
        val match = regex.find(trimmed)
        if (match != null) {
            val numVal = match.groupValues[1].toDoubleOrNull()
            val unitVal = match.groupValues[2].ifBlank { "Portions" }
            return Pair(numVal, unitVal)
        }
        return Pair(null, "Portions")
    }

    // Validations
    fun validateStep1(): Boolean {
        val state = _uiState.value
        var isValid = true

        var foodNameErr: String? = null
        if (state.foodName.isBlank()) {
            foodNameErr = "Please enter the food name"
            isValid = false
        }

        var categoryErr: String? = null
        if (state.category.isBlank()) {
            categoryErr = "Please select a food category"
            isValid = false
        }

        var descErr: String? = null
        if (state.description.isBlank()) {
            descErr = "Please enter a short description"
            isValid = false
        }

        var qtyErr: String? = null
        val (parsedNum, parsedUnit) = parseQuantityAndUnit(state.quantityText)
        if (state.quantityText.isBlank() || parsedNum == null || parsedNum <= 0) {
            qtyErr = "Please enter quantity (e.g. 5 portions, 2 kg)"
            isValid = false
        }

        _uiState.update {
            it.copy(
                foodNameError = foodNameErr,
                categoryError = categoryErr,
                descriptionError = descErr,
                quantityTextError = qtyErr,
                quantityUnit = parsedUnit
            )
        }
        return isValid
    }

    fun validateStep2(): Boolean {
        val state = _uiState.value
        if (state.selectedPhotoPaths.isEmpty()) {
            _uiState.update { it.copy(photoError = "Please add at least 1 photo of the food") }
            return false
        }
        _uiState.update { it.copy(photoError = null) }
        return true
    }

    fun validateStep3(): Boolean {
        val state = _uiState.value
        var isValid = true

        var prepErr: String? = null
        if (state.preparationTimestamp > System.currentTimeMillis() + 60000) {
            prepErr = "Preparation time cannot be in the future"
            isValid = false
        }

        var expErr: String? = null
        val now = System.currentTimeMillis()
        if (state.expiryTimestamp == null) {
            expErr = "Please select a best before / expiry date & time."
            isValid = false
        } else if (state.expiryTimestamp <= now) {
            expErr = "Expiry time must be in the future."
            isValid = false
        } else if (state.expiryTimestamp <= state.preparationTimestamp) {
            expErr = "Expiry date and time must be after preparation time."
            isValid = false
        }

        _uiState.update {
            it.copy(
                preparationError = prepErr,
                expiryError = expErr
            )
        }
        return isValid
    }

    fun validateStep4(): Boolean {
        val state = _uiState.value
        if (state.pickupAddress.isBlank()) {
            _uiState.update { it.copy(pickupAddressError = "Please enter a pickup address") }
            return false
        }
        _uiState.update { it.copy(pickupAddressError = null) }
        return true
    }

    // Submit Listing
    fun submitListing(donorName: String) {
        val state = _uiState.value

        // Prevent duplicate submissions while already submitting
        if (state.isSubmitting) return

        if (!validateStep1() || !validateStep2() || !validateStep3() || !validateStep4() || !state.isConfirmed) {
            _uiState.update { it.copy(submitError = "Please check all required fields and confirm food safety") }
            return
        }

        // Revalidate expiry timestamp immediately before saving to ensure it hasn't expired in the interim
        val now = System.currentTimeMillis()
        val exp = state.expiryTimestamp
        if (exp == null || exp <= now || exp <= state.preparationTimestamp) {
            validateStep3()
            _uiState.update { it.copy(submitError = "Expiry time has elapsed or is invalid. Please update step 3.") }
            return
        }

        val donorId = foodRepository.currentUserId
        if (donorId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    submitError = "You must be signed in to submit a donation. Please sign in and try again."
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            // 1. Upload photos directly to Cloudinary (unsigned multipart upload)
            val uploadResult = foodRepository.uploadPhotosToCloudinary(state.selectedPhotoPaths)
            val photoUrls = uploadResult.getOrElse { err ->
                val errorMsg = err.message ?: "Network upload failed. Please try again."
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = errorMsg
                    )
                }
                return@launch
            }

            // 2. Create food listing object
            val (parsedNum, parsedUnit) = parseQuantityAndUnit(state.quantityText)
            val listing = FoodListing(
                donorId = donorId,
                donorName = donorName.ifBlank { "EcoFeast Donor" },
                foodName = state.foodName.trim(),
                category = state.category,
                description = state.description.trim(),
                quantity = parsedNum ?: 1.0,
                quantityUnit = parsedUnit.ifBlank { state.quantityUnit.ifBlank { "Portions" } },
                photoUrls = photoUrls,
                preparationDateTime = state.preparationTimestamp,
                expiryDateTime = state.expiryTimestamp,
                foodCondition = state.foodCondition,
                allergenNotes = state.allergenNotes.ifBlank { null },
                pickupAddress = state.pickupAddress.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                pickupInstructions = state.pickupInstructions.ifBlank { null },
                status = "available",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // 3. Save listing to Cloud Firestore
            val saveResult = foodRepository.createListing(listing)
            if (saveResult.isSuccess) {
                // 4. Clear draft and move to Success Screen
                draftRepository.clearDraft()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        hasDraft = false,
                        subScreen = DonateSubScreen.SUCCESS,
                        submitError = null
                    )
                }
            } else {
                val err = saveResult.exceptionOrNull()?.message ?: "Failed to save listing to database."
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = "Submission failed: $err"
                    )
                }
            }
        }
    }

    // My Donations Operations
    fun loadUserListings(donorId: String = "") {
        val targetId = if (donorId.isNotBlank()) donorId else (foodRepository.currentUserId ?: "")
        if (targetId.isBlank()) {
            _uiState.update { it.copy(isLoadingListings = false, userListings = emptyList()) }
            return
        }

        _uiState.update { it.copy(isLoadingListings = true) }
        viewModelScope.launch {
            foodRepository.getUserListings(targetId).collect { listings ->
                _uiState.update {
                    it.copy(
                        userListings = listings,
                        isLoadingListings = false
                    )
                }
            }
        }
    }

    fun setSelectedListingTab(tab: Int) {
        _uiState.update { it.copy(selectedListingTab = tab) }
    }

    fun selectListingForDetails(listing: FoodListing) {
        _uiState.update {
            it.copy(
                selectedListing = listing,
                subScreen = DonateSubScreen.MY_DONATIONS_DETAIL
            )
        }
    }

    fun confirmHandover(listingId: String) {
        viewModelScope.launch {
            val result = foodRepository.confirmHandover(listingId)
            if (result.isSuccess) {
                val updated = foodRepository.getListingById(listingId)
                _uiState.update {
                    it.copy(
                        selectedListing = updated ?: it.selectedListing
                    )
                }
            }
        }
    }

    fun withdrawDonation(listingId: String) {
        viewModelScope.launch {
            val result = foodRepository.withdrawDonation(listingId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(subScreen = DonateSubScreen.MY_DONATIONS_LIST, selectedListing = null)
                }
            }
        }
    }
}
