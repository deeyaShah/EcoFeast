package com.college.ecofeast.ui.claims

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ClaimsSubScreen {
    LIST,
    DETAIL
}

data class ClaimsUiState(
    val subScreen: ClaimsSubScreen = ClaimsSubScreen.LIST,
    val claims: List<FoodListing> = emptyList(),
    val selectedTab: Int = 0, // 0 = Active, 1 = Past
    val selectedClaim: FoodListing? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ClaimsViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository()

    private val _uiState = MutableStateFlow(ClaimsUiState())
    val uiState: StateFlow<ClaimsUiState> = _uiState.asStateFlow()

    init {
        loadUserClaims()
    }

    fun loadUserClaims() {
        val currentUid = foodRepository.currentUserId
        if (currentUid.isNullOrBlank()) {
            _uiState.update { it.copy(isLoading = false, claims = emptyList()) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            foodRepository.getUserClaims(currentUid).collect { claims ->
                _uiState.update {
                    it.copy(
                        claims = claims,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectClaimForDetails(claim: FoodListing) {
        _uiState.update {
            it.copy(
                selectedClaim = claim,
                subScreen = ClaimsSubScreen.DETAIL
            )
        }
    }

    fun navigateToList() {
        _uiState.update {
            it.copy(
                subScreen = ClaimsSubScreen.LIST,
                selectedClaim = null
            )
        }
    }

    fun confirmPickup(listingId: String) {
        viewModelScope.launch {
            val result = foodRepository.confirmPickup(listingId)
            if (result.isSuccess) {
                val updated = foodRepository.getListingById(listingId)
                _uiState.update {
                    it.copy(
                        selectedClaim = updated ?: it.selectedClaim,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "Failed to confirm pickup.")
                }
            }
        }
    }

    fun releaseClaim(listingId: String) {
        viewModelScope.launch {
            val result = foodRepository.releaseClaim(listingId)
            if (result.isSuccess) {
                navigateToList()
            } else {
                _uiState.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "Failed to release claim.")
                }
            }
        }
    }
}
