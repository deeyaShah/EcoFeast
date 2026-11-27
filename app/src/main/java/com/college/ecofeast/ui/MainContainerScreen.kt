package com.college.ecofeast.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.college.ecofeast.ui.auth.EditProfileScreen
import com.college.ecofeast.ui.claims.ClaimsViewModel
import com.college.ecofeast.ui.claims.MyClaimsScreen
import com.college.ecofeast.ui.nearby.NearbyViewModel
import com.college.ecofeast.ui.theme.WarmIvory

/**
 * Container composable for EcoFeast Home experience with persistent Bottom Navigation.
 * Manages tab switching between Home, Donate, Nearby, and Profile.
 * Hides bottom bar during the 5-step Donate wizard for a focused full-screen flow.
 */
@Composable
fun MainContainerScreen(
    modifier: Modifier = Modifier,
    isGuest: Boolean = false,
    userName: String = "",
    userPhone: String = "",
    userEmail: String = "",
    pendingClaimListingId: String? = null,
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onProfileUpdated: (String, String) -> Unit = { _, _ -> },
    onClaimAsGuest: (String) -> Unit = {},
    onPendingClaimRestored: () -> Unit = {}
) {
    var currentTab by rememberSaveable { mutableStateOf(EcoFeastTab.HOME) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var showMyClaims by rememberSaveable { mutableStateOf(false) }
    var showEditProfile by rememberSaveable { mutableStateOf(false) }

    val nearbyViewModel: NearbyViewModel = viewModel()
    val claimsViewModel: ClaimsViewModel = viewModel()
    val claimsUiState by claimsViewModel.uiState.collectAsState()

    // Automatically restore pending claim view after guest completes auth
    LaunchedEffect(pendingClaimListingId, isGuest) {
        if (!isGuest && !pendingClaimListingId.isNullOrBlank()) {
            currentTab = EcoFeastTab.NEARBY
            isBottomBarVisible = true
            showMyClaims = false
            showEditProfile = false
            nearbyViewModel.restorePendingClaimListing(pendingClaimListingId)
            onPendingClaimRestored()
        }
    }

    LaunchedEffect(showMyClaims, isGuest) {
        if (!isGuest && showMyClaims) {
            claimsViewModel.loadUserClaims()
        }
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible && !showMyClaims && !showEditProfile) {
                EcoFeastBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        currentTab = selectedTab
                        isBottomBarVisible = true
                        showMyClaims = false
                        showEditProfile = false
                    }
                )
            }
        },
        containerColor = WarmIvory,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isBottomBarVisible && !showMyClaims && !showEditProfile) innerPadding else PaddingValues())
        ) {
            if (showEditProfile) {
                EditProfileScreen(
                    currentName = userName,
                    currentEmail = userEmail,
                    phoneNumber = userPhone,
                    onBackClick = { showEditProfile = false },
                    onProfileSaved = { newName, newEmail ->
                        showEditProfile = false
                        onProfileUpdated(newName, newEmail)
                    }
                )
            } else if (showMyClaims) {
                MyClaimsScreen(
                    uiState = claimsUiState,
                    viewModel = claimsViewModel,
                    onBackClick = { showMyClaims = false },
                    onNavigateToNearby = {
                        showMyClaims = false
                        nearbyViewModel.selectViewMode(0)
                        currentTab = EcoFeastTab.NEARBY
                        isBottomBarVisible = true
                    }
                )
            } else {
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(durationMillis = 300),
                    label = "MainTabTransition"
                ) { tab ->
                    when (tab) {
                        EcoFeastTab.HOME -> {
                            HomeScreen(
                                isGuest = isGuest,
                                userName = userName,
                                onNavigateToDonate = {
                                    currentTab = EcoFeastTab.DONATE
                                    isBottomBarVisible = true
                                },
                                onNavigateToNearby = {
                                    nearbyViewModel.selectViewMode(1) // Map mode
                                    currentTab = EcoFeastTab.NEARBY
                                    isBottomBarVisible = true
                                },
                                onNavigateToProfile = {
                                    currentTab = EcoFeastTab.PROFILE
                                    isBottomBarVisible = true
                                },
                                onNavigateToMyClaims = {
                                    showMyClaims = true
                                },
                                onSignInClick = onSignInClick
                            )
                        }
                        EcoFeastTab.DONATE -> {
                            DonateScreen(
                                isGuest = isGuest,
                                userName = userName,
                                userPhone = userPhone,
                                onSignInClick = onSignInClick,
                                onNavigateToHome = {
                                    currentTab = EcoFeastTab.HOME
                                    isBottomBarVisible = true
                                },
                                onBottomBarVisibilityChanged = { visible ->
                                    isBottomBarVisible = visible
                                }
                            )
                        }
                        EcoFeastTab.NEARBY -> {
                            NearbyScreen(
                                isGuest = isGuest,
                                onSignInClick = onSignInClick,
                                onClaimAsGuest = onClaimAsGuest,
                                viewModel = nearbyViewModel
                            )
                        }
                        EcoFeastTab.PROFILE -> {
                            ProfileScreen(
                                isGuest = isGuest,
                                userName = userName,
                                userPhone = userPhone,
                                userEmail = userEmail,
                                onSignInClick = onSignInClick,
                                onSignOutClick = onSignOutClick,
                                onEditProfileClick = {
                                    showEditProfile = true
                                },
                                onNavigateToMyDonations = {
                                    currentTab = EcoFeastTab.DONATE
                                    isBottomBarVisible = true
                                },
                                onNavigateToMyClaims = {
                                    showMyClaims = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
