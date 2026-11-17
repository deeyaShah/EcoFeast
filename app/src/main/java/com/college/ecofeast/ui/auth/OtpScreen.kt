package com.college.ecofeast.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EcoFeastAuth"

private fun formatTimerText(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

private fun parsePhoneAuthError(e: FirebaseException): FeedbackMessage {
    val msg = e.message ?: ""
    val messageText = when {
        e is FirebaseAuthInvalidCredentialsException -> {
            "Invalid phone number or request configuration."
        }
        msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
        msg.contains("quota", ignoreCase = true) ||
        msg.contains("exceeded", ignoreCase = true) -> {
            "Too many requests. Please wait a few minutes before trying again."
        }
        msg.contains("network", ignoreCase = true) ||
        msg.contains("connection", ignoreCase = true) ||
        msg.contains("UNAVAILABLE", ignoreCase = true) -> {
            "Network error. Please check your internet connection and try again."
        }
        e is FirebaseAuthException -> {
            when (e.errorCode) {
                "ERROR_TOO_MANY_REQUESTS", "ERROR_QUOTA_EXCEEDED" ->
                    "Too many resend attempts. Please wait a few minutes."
                "ERROR_INVALID_PHONE_NUMBER" ->
                    "Invalid phone number format."
                "ERROR_SESSION_EXPIRED" ->
                    "Verification session expired. Please re-enter your phone number."
                else -> e.localizedMessage ?: "Verification failed."
            }
        }
        else -> e.localizedMessage ?: "Failed to resend verification code."
    }
    return FeedbackMessage(text = messageText, category = FeedbackCategory.AUTHENTICATION)
}

@Composable
fun OtpScreen(
    phoneNumber: String,
    verificationId: String,
    resendingToken: PhoneAuthProvider.ForceResendingToken?,
    onBackClick: () -> Unit = {},
    onVerificationSuccess: (fullName: String, email: String, phone: String) -> Unit = { _, _, _ -> },
    onNavigateToProfileSetup: () -> Unit = {},
    onResendTokenUpdated: (PhoneAuthProvider.ForceResendingToken?) -> Unit = {},
    onVerificationIdUpdated: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activity = context as? Activity

    var currentVerificationId by remember(verificationId) { mutableStateOf(verificationId) }
    var currentResendingToken by remember(resendingToken) { mutableStateOf(resendingToken) }

    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }

    var resendTimerKey by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }

    val isPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(resendTimerKey) {
        isVisible = true
        timeLeft = 30
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    fun performResend() {
        if (timeLeft > 0 || isLoading || isResending) return

        keyboardController?.hide()
        focusManager.clearFocus()

        if (activity == null) {
            coroutineScope.launch {
                snackbarHostState.showFeedback(
                    FeedbackMessage("Activity context not available.", FeedbackCategory.AUTHENTICATION)
                )
            }
            return
        }

        val e164Phone = if (phoneNumber.startsWith("+")) {
            "+" + phoneNumber.filter { it.isDigit() }
        } else {
            "+" + phoneNumber.filter { it.isDigit() }
        }

        if (e164Phone.length < 8) {
            coroutineScope.launch {
                snackbarHostState.showFeedback(
                    FeedbackMessage("Invalid phone number format. Please return and re-enter your number.", FeedbackCategory.VALIDATION)
                )
            }
            return
        }

        isResending = true

        if (currentResendingToken == null) {
            coroutineScope.launch {
                snackbarHostState.showFeedback(
                    FeedbackMessage("Resend token missing. Requesting new verification code...", FeedbackCategory.INFO)
                )
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(e164Phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Resend onVerificationCompleted")
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            isResending = false
                            val user = authResult.user
                            if (user != null) {
                                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                                    .addOnSuccessListener { doc ->
                                        val name = doc.getString("fullName") ?: ""
                                        val email = doc.getString("email") ?: ""
                                        val phone = doc.getString("phoneNumber") ?: user.phoneNumber ?: phoneNumber
                                        onVerificationSuccess(name, email, phone)
                                    }
                                    .addOnFailureListener { e ->
                                        coroutineScope.launch {
                                            snackbarHostState.showFeedback(
                                                FeedbackMessage("Unable to load profile: ${e.localizedMessage}", FeedbackCategory.AUTHENTICATION)
                                            )
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            isResending = false
                            coroutineScope.launch {
                                snackbarHostState.showFeedback(
                                    FeedbackMessage(e.localizedMessage ?: "Sign-in failed.", FeedbackCategory.AUTHENTICATION)
                                )
                            }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Resend verification failed: exception=${e.javaClass.simpleName}, message=${e.message}", e)
                    isResending = false
                    val feedback = parsePhoneAuthError(e)
                    coroutineScope.launch {
                        snackbarHostState.showFeedback(feedback)
                    }
                }

                override fun onCodeSent(
                    verId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "Resend code sent successfully")
                    isResending = false
                    currentVerificationId = verId
                    currentResendingToken = token
                    onVerificationIdUpdated(verId)
                    onResendTokenUpdated(token)
                    resendTimerKey++
                    coroutineScope.launch {
                        snackbarHostState.showFeedback(
                            message = "Verification code resent successfully.",
                            category = FeedbackCategory.SUCCESS
                        )
                    }
                }
            })

        if (currentResendingToken != null) {
            optionsBuilder.setForceResendingToken(currentResendingToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
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
                            text = "Verify Your Number",
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

                    // Phone number explanation
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 220)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 220))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Enter the 6-digit verification code for",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = SecondaryText,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = phoneNumber.ifEmpty { "+91 Phone Number" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepText,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryForestGreen,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onBackClick() }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Security / Lock Illustration
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 280))
                    ) {
                        OtpVerificationIllustration(
                            modifier = Modifier
                                .size(135.dp)
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 6-Digit OTP Box Input
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 350)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 350))
                    ) {
                        OtpDigitInputRow(
                            otpValue = otpCode,
                            onOtpChange = { otpCode = it },
                            enabled = !isLoading && !isResending,
                            length = 6,
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resend OTP Link with 30s Countdown Timer
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 390))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Didn't receive code? ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SecondaryText,
                                    fontSize = 14.sp
                                )
                            )
                            if (isResending) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Resending... ",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = PrimaryForestGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = PrimaryForestGreen,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else {
                                Text(
                                    text = if (timeLeft > 0) "Resend OTP in ${formatTimerText(timeLeft)}" else "Resend OTP",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (timeLeft > 0) SecondaryText else PrimaryForestGreen,
                                        fontWeight = if (timeLeft > 0) FontWeight.Normal else FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = timeLeft == 0 && !isLoading && !isResending) {
                                            performResend()
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Verify OTP Button
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350, delayMillis = 440)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350, delayMillis = 440))
                    ) {
                        PrimaryAuthButton(
                            text = "Verify OTP",
                            loadingText = "Verifying Code...",
                            isLoading = isLoading,
                            onClick = {
                                if (isLoading || isResending) return@PrimaryAuthButton

                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val validationError = AuthValidation.validateOtp(otpCode, expectedLength = 6)

                                if (validationError != null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showFeedback(validationError)
                                    }
                                } else if (currentVerificationId.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showFeedback(
                                            FeedbackMessage("Verification ID missing. Please re-enter your phone number.", FeedbackCategory.AUTHENTICATION)
                                        )
                                    }
                                } else {
                                    isLoading = true
                                    try {
                                        val credential = PhoneAuthProvider.getCredential(currentVerificationId, otpCode)
                                        Log.d(TAG, "OTP credential created successfully")

                                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                            .addOnSuccessListener { authResult ->
                                                val user = authResult.user
                                                Log.d(TAG, "Firebase sign-in succeeded: userExists=${user != null}, uid=${user?.uid}")
                                                if (user != null) {
                                                    FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                                                        .addOnSuccessListener { document ->
                                                            isLoading = false
                                                            val exists = document.exists()
                                                            val hasName = !document.getString("fullName").isNullOrBlank()
                                                            Log.d(TAG, "Firestore profile read succeeded: documentExists=$exists, hasFullName=$hasName")
                                                            if (exists && hasName) {
                                                                val name = document.getString("fullName") ?: ""
                                                                val email = document.getString("email") ?: ""
                                                                val phone = document.getString("phoneNumber") ?: user.phoneNumber ?: phoneNumber
                                                                onVerificationSuccess(name, email, phone)
                                                            } else {
                                                                Log.d(TAG, "Profile document missing or incomplete, routing to Profile Setup")
                                                                onNavigateToProfileSetup()
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            val exClass = e.javaClass.simpleName
                                                            val exMsg = e.localizedMessage ?: "Unknown error"
                                                            Log.e(TAG, "Firestore profile read failed: exception=$exClass, message=$exMsg", e)
                                                            val errorFeedback = if (exMsg.contains("PERMISSION_DENIED", ignoreCase = true) || exMsg.contains("permission", ignoreCase = true)) {
                                                                "Firestore permission denied. Please check Firestore security rules for users/{uid}."
                                                            } else {
                                                                "Unable to verify user profile: $exMsg"
                                                            }
                                                            coroutineScope.launch {
                                                                snackbarHostState.showFeedback(
                                                                    FeedbackMessage(errorFeedback, FeedbackCategory.AUTHENTICATION)
                                                                )
                                                            }
                                                        }
                                                } else {
                                                    isLoading = false
                                                    Log.e(TAG, "Firebase sign-in succeeded but current user is null")
                                                    coroutineScope.launch {
                                                        snackbarHostState.showFeedback(
                                                            FeedbackMessage("Authentication failed. Please try again.", FeedbackCategory.AUTHENTICATION)
                                                        )
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                val exClass = e.javaClass.simpleName
                                                val exMsg = e.localizedMessage ?: "Unknown error"
                                                Log.e(TAG, "Firebase sign-in failed: exception=$exClass, message=$exMsg", e)
                                                val errorMsg = when (e) {
                                                    is FirebaseAuthInvalidCredentialsException -> "Invalid verification code. Please check and try again."
                                                    else -> "Verification failed: $exMsg"
                                                }
                                                coroutineScope.launch {
                                                    snackbarHostState.showFeedback(
                                                        FeedbackMessage(errorMsg, FeedbackCategory.AUTHENTICATION)
                                                    )
                                                }
                                            }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        val exClass = e.javaClass.simpleName
                                        val exMsg = e.localizedMessage ?: "Unknown error"
                                        Log.e(TAG, "OTP credential creation failed: exception=$exClass, message=$exMsg", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showFeedback(
                                                FeedbackMessage("Failed to create credential: $exMsg", FeedbackCategory.AUTHENTICATION)
                                            )
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
