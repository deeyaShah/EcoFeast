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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onClaimAsGuest: (String) -> Unit = {},
    onPendingClaimRestored: () -> Unit = {}
) {
    var currentTab by rememberSaveable { mutableStateOf(EcoFeastTab.HOME) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val nearbyViewModel: NearbyViewModel = viewModel()

    // Automatically restore pending claim view after guest completes auth
    LaunchedEffect(pendingClaimListingId, isGuest) {
        if (!isGuest && !pendingClaimListingId.isNullOrBlank()) {
            currentTab = EcoFeastTab.NEARBY
            isBottomBarVisible = true
            nearbyViewModel.restorePendingClaimListing(pendingClaimListingId)
            onPendingClaimRestored()
        }
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                EcoFeastBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        currentTab = selectedTab
                        isBottomBarVisible = true
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
                .padding(if (isBottomBarVisible) innerPadding else PaddingValues())
        ) {
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
                            onSignOutClick = onSignOutClick
                        )
                    }
                }
            }
        }
    }
}
