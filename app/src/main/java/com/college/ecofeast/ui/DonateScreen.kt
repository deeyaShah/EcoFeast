package com.college.ecofeast.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.college.ecofeast.ui.donate.DonateEntryScreen
import com.college.ecofeast.ui.donate.DonateSubScreen
import com.college.ecofeast.ui.donate.DonateSuccessScreen
import com.college.ecofeast.ui.donate.DonateViewModel
import com.college.ecofeast.ui.donate.DonateWizardScreen
import com.college.ecofeast.ui.donate.MyDonationsScreen
import com.google.firebase.auth.FirebaseAuth

/**
 * Main host screen for the Donate Food feature.
 * Coordinates transitions between Entry Screen, 5-Step Donation Wizard, Success Screen,
 * and My Donations views.
 */
@Composable
fun DonateScreen(
    modifier: Modifier = Modifier,
    isGuest: Boolean = false,
    userName: String = "",
    userPhone: String = "",
    onSignInClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {}
) {
    val viewModel: DonateViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Control persistent Bottom Navigation visibility based on active sub-screen
    LaunchedEffect(uiState.subScreen) {
        val shouldShowBottomBar = uiState.subScreen != DonateSubScreen.WIZARD
        onBottomBarVisibilityChanged(shouldShowBottomBar)
    }

    // Load donor's published listings when navigating to My Donations
    LaunchedEffect(uiState.subScreen, isGuest) {
        if (!isGuest && (uiState.subScreen == DonateSubScreen.MY_DONATIONS_LIST || uiState.subScreen == DonateSubScreen.MY_DONATIONS_DETAIL)) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            viewModel.loadUserListings(currentUid)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = uiState.subScreen,
            animationSpec = tween(durationMillis = 300),
            label = "DonateSubScreenTransition"
        ) { subScreen ->
            when (subScreen) {
                DonateSubScreen.ENTRY -> {
                    DonateEntryScreen(
                        isGuest = isGuest,
                        hasDraft = uiState.hasDraft,
                        onStartNewDonation = { viewModel.startNewDonation() },
                        onContinueDraft = { viewModel.continueDraft() },
                        onDiscardDraft = { viewModel.discardDraft() },
                        onViewMyDonations = { viewModel.navigateTo(DonateSubScreen.MY_DONATIONS_LIST) },
                        onSignInClick = onSignInClick,
                        onBackClick = onNavigateToHome
                    )
                }
                DonateSubScreen.WIZARD -> {
                    DonateWizardScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        userName = userName,
                        userPhone = userPhone
                    )
                }
                DonateSubScreen.SUCCESS -> {
                    DonateSuccessScreen(
                        onViewMyDonations = { viewModel.navigateTo(DonateSubScreen.MY_DONATIONS_LIST) },
                        onBackToHome = {
                            viewModel.navigateTo(DonateSubScreen.ENTRY)
                            onNavigateToHome()
                        }
                    )
                }
                DonateSubScreen.MY_DONATIONS_LIST, DonateSubScreen.MY_DONATIONS_DETAIL -> {
                    MyDonationsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateTo(DonateSubScreen.ENTRY) },
                        onStartNewDonation = { viewModel.startNewDonation() }
                    )
                }
            }
        }
    }
}
