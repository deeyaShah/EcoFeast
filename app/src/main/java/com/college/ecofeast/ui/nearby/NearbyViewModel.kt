package com.college.ecofeast.ui.nearby

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.data.repository.FoodRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NearbyUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedCondition: String = "All",
    val sortBy: String = "Newest First", // "Newest First", "Nearest First", "Expiring Soonest"
    val selectedViewMode: Int = 0, // 0 = List View, 1 = Map View
    val allListings: List<FoodListing> = emptyList(),
    val filteredListings: List<FoodListing> = emptyList(),
    val distancesMap: Map<String, String> = emptyMap(), // listing.id -> "1.2 km away"
    val isLoading: Boolean = true,
    val error: String? = null,
    val receiverLatitude: Double? = null,
    val receiverLongitude: Double? = null,
    val isLocatingReceiver: Boolean = false,
    val locationErrorMessage: String? = null,
    val selectedListing: FoodListing? = null
) {
    val activeFilterCount: Int
        get() = (if (selectedCategory != "All") 1 else 0) +
                (if (selectedCondition != "All") 1 else 0) +
                (if (sortBy != "Newest First") 1 else 0)
}

class NearbyViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    val categories = listOf(
        "All",
        "Prepared Meal",
        "Bakery & Bread",
        "Fresh Produce",
        "Dairy & Eggs",
        "Beverages",
        "Packaged Food",
        "Others"
    )

    val conditions = listOf(
        "All",
        "Freshly Prepared",
        "Packaged / Sealed",
        "Good Condition"
    )

    val sortOptions = listOf(
        "Newest First",
        "Nearest First",
        "Expiring Soonest"
    )

    init {
        loadAvailableListings()
    }

    fun loadAvailableListings() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            foodRepository.getAvailableListings()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load available food listings."
                        )
                    }
                }
                .collect { listings ->
                    _uiState.update { state ->
                        val filtered = applyFilterAndSort(
                            listings,
                            state.searchQuery,
                            state.selectedCategory,
                            state.selectedCondition,
                            state.sortBy,
                            state.receiverLatitude,
                            state.receiverLongitude
                        )
                        val distances = computeDistancesMap(
                            filtered,
                            state.receiverLatitude,
                            state.receiverLongitude
                        )
                        state.copy(
                            allListings = listings,
                            filteredListings = filtered,
                            distancesMap = distances,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                query,
                state.selectedCategory,
                state.selectedCondition,
                state.sortBy,
                state.receiverLatitude,
                state.receiverLongitude
            )
            state.copy(searchQuery = query, filteredListings = filtered)
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                state.searchQuery,
                category,
                state.selectedCondition,
                state.sortBy,
                state.receiverLatitude,
                state.receiverLongitude
            )
            state.copy(selectedCategory = category, filteredListings = filtered)
        }
    }

    fun selectCondition(condition: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                state.searchQuery,
                state.selectedCategory,
                condition,
                state.sortBy,
                state.receiverLatitude,
                state.receiverLongitude
            )
            state.copy(selectedCondition = condition, filteredListings = filtered)
        }
    }

    fun selectSortBy(sortBy: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                state.searchQuery,
                state.selectedCategory,
                state.selectedCondition,
                sortBy,
                state.receiverLatitude,
                state.receiverLongitude
            )
            state.copy(sortBy = sortBy, filteredListings = filtered)
        }
    }

    fun selectViewMode(mode: Int) {
        _uiState.update { it.copy(selectedViewMode = mode) }
    }

    /**
     * Updates receiver coordinates and immediately computes distance strings
     * for all eligible food listings with valid latitude and longitude.
     */
    fun updateReceiverLocation(lat: Double, lng: Double) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                state.searchQuery,
                state.selectedCategory,
                state.selectedCondition,
                state.sortBy,
                lat,
                lng
            )
            val distances = computeDistancesMap(filtered, lat, lng)
            state.copy(
                receiverLatitude = lat,
                receiverLongitude = lng,
                filteredListings = filtered,
                distancesMap = distances,
                isLocatingReceiver = false,
                locationErrorMessage = null
            )
        }
    }

    /**
     * Immediately clears active receiver coordinates and purges all calculated distances
     * from UI state when GPS/location service is disabled or permission revoked.
     */
    fun clearReceiverLocation(errorMessage: String? = null) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                state.searchQuery,
                state.selectedCategory,
                state.selectedCondition,
                state.sortBy,
                null,
                null
            )
            state.copy(
                receiverLatitude = null,
                receiverLongitude = null,
                filteredListings = filtered,
                distancesMap = emptyMap(),
                isLocatingReceiver = false,
                locationErrorMessage = errorMessage
            )
        }
    }

    fun setLocatingReceiver(isLocating: Boolean) {
        _uiState.update {
            it.copy(
                isLocatingReceiver = isLocating,
                locationErrorMessage = if (isLocating) null else it.locationErrorMessage
            )
        }
    }

    fun setLocationError(message: String) {
        _uiState.update {
            it.copy(
                isLocatingReceiver = false,
                locationErrorMessage = message
            )
        }
    }

    fun selectListing(listing: FoodListing?) {
        val currentUid = auth.currentUser?.uid
        if (listing != null && !currentUid.isNullOrBlank() && listing.donorId == currentUid) {
            // Self-donated listing cannot be selected or claimed
            return
        }
        _uiState.update { it.copy(selectedListing = listing) }
    }

    fun clearFilters() {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(
                state.allListings,
                "",
                "All",
                "All",
                "Newest First",
                state.receiverLatitude,
                state.receiverLongitude
            )
            state.copy(
                searchQuery = "",
                selectedCategory = "All",
                selectedCondition = "All",
                sortBy = "Newest First",
                filteredListings = filtered
            )
        }
    }

    private fun applyFilterAndSort(
        rawListings: List<FoodListing>,
        query: String,
        category: String,
        condition: String,
        sortBy: String,
        userLat: Double?,
        userLng: Double?
    ): List<FoodListing> {
        val currentUserId = auth.currentUser?.uid

        // 1. Exclude current user's own donations if authenticated
        val eligibleListings = if (!currentUserId.isNullOrBlank()) {
            rawListings.filter { it.donorId != currentUserId }
        } else {
            rawListings
        }

        // 2. Filter by search query, category, and food condition
        val filtered = eligibleListings.filter { listing ->
            val matchesSearch = query.isBlank() ||
                    listing.foodName.contains(query, ignoreCase = true) ||
                    listing.category.contains(query, ignoreCase = true) ||
                    listing.pickupAddress.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true)

            val matchesCategory = category.equals("All", ignoreCase = true) ||
                    listing.category.equals(category, ignoreCase = true)

            val matchesCondition = condition.equals("All", ignoreCase = true) ||
                    listing.foodCondition.equals(condition, ignoreCase = true)

            matchesSearch && matchesCategory && matchesCondition
        }

        // 3. Apply sorting options
        return when (sortBy) {
            "Nearest First" -> {
                if (userLat != null && userLng != null) {
                    filtered.sortedBy { listing ->
                        if (listing.latitude != null && listing.longitude != null) {
                            val results = FloatArray(1)
                            Location.distanceBetween(userLat, userLng, listing.latitude, listing.longitude, results)
                            results[0]
                        } else {
                            Float.MAX_VALUE
                        }
                    }
                } else {
                    filtered.sortedByDescending { it.createdAt }
                }
            }
            "Expiring Soonest" -> {
                filtered.sortedBy { listing ->
                    listing.expiryDateTime ?: Long.MAX_VALUE
                }
            }
            else -> { // "Newest First"
                filtered.sortedByDescending { it.createdAt }
            }
        }
    }

    private fun computeDistancesMap(
        listings: List<FoodListing>,
        userLat: Double?,
        userLng: Double?
    ): Map<String, String> {
        if (userLat == null || userLng == null) return emptyMap()
        val resultMap = mutableMapOf<String, String>()
        for (listing in listings) {
            if (listing.latitude != null && listing.longitude != null) {
                val distStr = calculateDistanceString(userLat, userLng, listing.latitude, listing.longitude)
                if (distStr != null) {
                    resultMap[listing.id] = distStr
                }
            }
        }
        return resultMap
    }

    private fun calculateDistanceString(
        userLat: Double,
        userLng: Double,
        donorLat: Double,
        donorLng: Double
    ): String? {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(userLat, userLng, donorLat, donorLng, results)
            val meters = results[0]
            if (meters < 1000) {
                "${meters.toInt()} m away"
            } else {
                val km = meters / 1000f
                "%.1f km away".format(km)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getDistanceForListing(listingId: String): String? {
        return _uiState.value.distancesMap[listingId]
    }
}
