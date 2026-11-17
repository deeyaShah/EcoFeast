package com.college.ecofeast.ui.auth

import android.app.Activity
import android.util.Log
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private const val TAG = "EcoFeastAuth"

@Composable
fun PhoneNumberScreen(
    onBackClick: () -> Unit = {},
    onSendOtpSuccess: (formattedPhone: String, verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit = { _, _, _ -> },
    onAutoSignedIn: (fullName: String, email: String, phone: String) -> Unit = { _, _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activity = context as? Activity

    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(defaultCountries[0]) } // India (+91) default
    var showCountryPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    if (showCountryPicker) {
        CountrySelectorDialog(
            countries = defaultCountries,
            selectedCountry = selectedCountry,
            onCountrySelected = { country ->
                selectedCountry = country
            },
            onDismiss = {
                showCountryPicker = false
            }
        )
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
                    height = 160.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            text = "Continue with Phone Number",
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

                    // Explanation
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 220)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 220))
                    ) {
                        Text(
                            text = "Enter your phone number to access EcoFeast. We will use phone verification to secure your food rescue account.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = SecondaryText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Decorative Illustration
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 280))
                    ) {
                        PhoneVerificationIllustration(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Phone Input Field (with country selector)
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 350)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 350))
                    ) {
                        PhoneInputRow(
                            phoneValue = phoneNumber,
                            onPhoneChange = { phoneNumber = it },
                            enabled = !isLoading,
                            selectedCountry = selectedCountry,
                            onCountryClick = { showCountryPicker = true },
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Send OTP Button
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 420)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 420))
                    ) {
                        PrimaryAuthButton(
                            text = "Send OTP",
                            loadingText = "Sending Verification...",
                            isLoading = isLoading,
                            onClick = {
                                if (isLoading) return@PrimaryAuthButton

                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val validationError = AuthValidation.validatePhoneNumber(phoneNumber)

                                if (validationError != null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showFeedback(validationError)
                                    }
                                } else if (activity == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showFeedback("Activity context not available for phone verification.")
                                    }
                                } else {
                                    isLoading = true
                                    val cleanedPhone = phoneNumber.trim().filter { it.isDigit() }
                                    val fullFormattedPhone = "${selectedCountry.code}$cleanedPhone"
                                    val displayPhone = "${selectedCountry.code} $cleanedPhone"

                                    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                        .setPhoneNumber(fullFormattedPhone)
                                        .setTimeout(60L, TimeUnit.SECONDS)
                                        .setActivity(activity)
                                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                Log.d(TAG, "PhoneAuth onVerificationCompleted")
                                                FirebaseAuth.getInstance().signInWithCredential(credential)
                                                    .addOnSuccessListener { authResult ->
                                                        isLoading = false
                                                        val user = authResult.user
                                                        Log.d(TAG, "Auto sign-in success: userExists=${user != null}, uid=${user?.uid}")
                                                        if (user != null) {
                                                            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                                                                .addOnSuccessListener { doc ->
                                                                    Log.d(TAG, "Auto sign-in Firestore read success: exists=${doc.exists()}")
                                                                    val name = doc.getString("fullName") ?: ""
                                                                    val email = doc.getString("email") ?: ""
                                                                    val phone = doc.getString("phoneNumber") ?: user.phoneNumber ?: displayPhone
                                                                    onAutoSignedIn(name, email, phone)
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.e(TAG, "Auto sign-in Firestore read failed: exception=${e.javaClass.simpleName}, message=${e.message}", e)
                                                                    coroutineScope.launch {
                                                                        snackbarHostState.showFeedback("Unable to load profile: ${e.localizedMessage}")
                                                                    }
                                                                }
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(TAG, "Auto sign-in failed: exception=${e.javaClass.simpleName}, message=${e.message}", e)
                                                        isLoading = false
                                                        coroutineScope.launch {
                                                            snackbarHostState.showFeedback(e.localizedMessage ?: "Sign-in failed.")
                                                        }
                                                    }
                                            }

                                            override fun onVerificationFailed(e: FirebaseException) {
                                                Log.e(TAG, "PhoneAuth verification failed: exception=${e.javaClass.simpleName}, message=${e.message}", e)
                                                isLoading = false
                                                val errorCode = (e as? FirebaseAuthException)?.errorCode ?: e.javaClass.simpleName
                                                val errorMsg = e.localizedMessage ?: "Verification failed."
                                                val detailedFeedback = "[$errorCode] $errorMsg"
                                                coroutineScope.launch {
                                                    snackbarHostState.showFeedback(detailedFeedback)
                                                }
                                            }

                                            override fun onCodeSent(
                                                verId: String,
                                                token: PhoneAuthProvider.ForceResendingToken
                                            ) {
                                                Log.d(TAG, "PhoneAuth code sent successfully")
                                                isLoading = false
                                                onSendOtpSuccess(displayPhone, verId, token)
                                            }
                                        })
                                        .build()

                                    PhoneAuthProvider.verifyPhoneNumber(options)
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
