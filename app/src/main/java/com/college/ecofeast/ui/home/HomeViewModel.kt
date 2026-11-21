package com.college.ecofeast.ui.home

import android.app.Application
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

data class HomeUiState(
    val availableListings: List<FoodListing> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var rawListings: List<FoodListing> = emptyList()

    private val authStateListener = FirebaseAuth.AuthStateListener { _ ->
        reapplyFilter(rawListings)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadListings()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            auth.removeAuthStateListener(authStateListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadListings() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            foodRepository.getAvailableListings()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load listings."
                        )
                    }
                }
                .collect { listings ->
                    rawListings = listings
                    reapplyFilter(listings)
                }
        }
    }

    private fun reapplyFilter(listings: List<FoodListing>) {
        val currentUserId = auth.currentUser?.uid
        val eligible = if (!currentUserId.isNullOrBlank()) {
            listings.filter { it.donorId != currentUserId }
        } else {
            listings
        }
        _uiState.update {
            it.copy(
                availableListings = eligible,
                isLoading = false,
                error = null
            )
        }
    }
}
