package com.college.ecofeast.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(
    phoneNumber: String,
    onBackClick: () -> Unit = {},
    onProfileCompleteClick: (fullName: String, email: String) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        snackbarHost = {
            EcoFeastSnackbarHost(hostState = snackbarHostState)
        },
        containerColor = WarmIvory
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(WarmIvory)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Section: Auth Background Artwork with Back Button
                AuthHeaderArtwork(
                    onBackClick = onBackClick,
                    height = 150.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Content Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Headline
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 150)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 150))
                    ) {
                        Text(
                            text = "Complete Your Profile",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 24.sp,
                                letterSpacing = 0.3.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tagline / Explanation
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 220)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 220))
                    ) {
                        Text(
                            text = "Set up your EcoFeast profile so community members know who they are sharing food with.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = SecondaryText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Profile Illustration
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 280))
                    ) {
                        ProfileSetupIllustration(
                            modifier = Modifier
                                .size(130.dp)
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Full Name Input
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 340)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 340))
                    ) {
                        AuthTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholderText = "Full Name",
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = PersonIcon,
                                    contentDescription = null,
                                    tint = PrimaryForestGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Address Input
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 400)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 400))
                    ) {
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholderText = "Email Address",
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = EmailIcon,
                                    contentDescription = null,
                                    tint = PrimaryForestGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Continue Button
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 460)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 460))
                    ) {
                        PrimaryAuthButton(
                            text = "Save & Continue",
                            loadingText = "Saving Profile...",
                            isLoading = isLoading,
                            onClick = {
                                if (isLoading) return@PrimaryAuthButton

                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val validationError = AuthValidation.validateProfileSetup(
                                    fullName = fullName,
                                    email = email
                                )

                                if (validationError != null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showFeedback(validationError)
                                    }
                                } else {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user == null) {
                                        coroutineScope.launch {
                                            snackbarHostState.showFeedback("Authentication session expired. Please sign in again.")
                                        }
                                        return@PrimaryAuthButton
                                    }

                                    isLoading = true
                                    val profileData = hashMapOf(
                                        "fullName" to fullName.trim(),
                                        "email" to email.trim(),
                                        "phoneNumber" to (user.phoneNumber ?: phoneNumber),
                                        "createdAt" to FieldValue.serverTimestamp()
                                    )

                                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                                        .set(profileData, SetOptions.merge())
                                        .addOnSuccessListener {
                                            isLoading = false
                                            onProfileCompleteClick(fullName.trim(), email.trim())
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            coroutineScope.launch {
                                                snackbarHostState.showFeedback(e.localizedMessage ?: "Failed to save profile. Please try again.")
                                            }
                                        }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
