package com.college.ecofeast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.college.ecofeast.ui.EcoFeastSplashScreen
import com.college.ecofeast.ui.HomeScreen
import com.college.ecofeast.ui.MainContainerScreen
import com.college.ecofeast.ui.WelcomeScreen
import com.college.ecofeast.ui.auth.OtpScreen
import com.college.ecofeast.ui.auth.PhoneNumberScreen
import com.college.ecofeast.ui.auth.ProfileSetupScreen
import com.college.ecofeast.ui.onboarding.OnboardingScreen
import com.college.ecofeast.ui.theme.EcoFeastTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    WELCOME,
    PHONE_NUMBER,
    OTP_VERIFICATION,
    PROFILE_SETUP,
    HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoFeastTheme {
                var currentScreen by rememberSaveable {
                    mutableStateOf(
                        if (savedInstanceState != null && FirebaseAuth.getInstance().currentUser != null) AppScreen.HOME else AppScreen.SPLASH
                    )
                }
                var isGuest by rememberSaveable { mutableStateOf(false) }
                var phoneNumber by rememberSaveable { mutableStateOf("") }
                var userName by rememberSaveable { mutableStateOf("") }
                var userEmail by rememberSaveable { mutableStateOf("") }
                var verificationId by remember { mutableStateOf("") }
                var resendingToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 400),
                    label = "AppScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            EcoFeastSplashScreen(onSplashFinished = {
                                val prefs = getSharedPreferences("ecofeast_prefs", MODE_PRIVATE)
                                val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)

                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    val uid = currentUser.uid
                                    FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists() && !document.getString("fullName").isNullOrBlank()) {
                                                userName = document.getString("fullName") ?: ""
                                                userEmail = document.getString("email") ?: ""
                                                phoneNumber = document.getString("phoneNumber") ?: currentUser.phoneNumber ?: ""
                                                isGuest = false
                                                currentScreen = AppScreen.HOME
                                            } else {
                                                phoneNumber = currentUser.phoneNumber ?: ""
                                                currentScreen = AppScreen.PROFILE_SETUP
                                            }
                                        }
                                        .addOnFailureListener {
                                            currentScreen = AppScreen.WELCOME
                                        }
                                } else {
                                    if (hasCompletedOnboarding) {
                                        currentScreen = AppScreen.WELCOME
                                    } else {
                                        currentScreen = AppScreen.ONBOARDING
                                    }
                                }
                            })
                        }
                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(onOnboardingFinished = {
                                val prefs = getSharedPreferences("ecofeast_prefs", MODE_PRIVATE)
                                prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                                currentScreen = AppScreen.WELCOME
                            })
                        }
                        AppScreen.WELCOME -> {
                            WelcomeScreen(
                                onContinueWithPhoneClick = {
                                    isGuest = false
                                    currentScreen = AppScreen.PHONE_NUMBER
                                },
                                onBrowseAsGuestClick = {
                                    isGuest = true
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                        AppScreen.PHONE_NUMBER -> {
                            PhoneNumberScreen(
                                onBackClick = {
                                    currentScreen = AppScreen.WELCOME
                                },
                                onSendOtpSuccess = { formattedPhone, verId, token ->
                                    phoneNumber = formattedPhone
                                    verificationId = verId
                                    resendingToken = token
                                    currentScreen = AppScreen.OTP_VERIFICATION
                                },
                                onAutoSignedIn = { name, email, phone ->
                                    userName = name
                                    userEmail = email
                                    phoneNumber = phone
                                    isGuest = false
                                    currentScreen = if (name.isNotBlank()) AppScreen.HOME else AppScreen.PROFILE_SETUP
                                }
                            )
                        }
                        AppScreen.OTP_VERIFICATION -> {
                            OtpScreen(
                                phoneNumber = phoneNumber,
                                verificationId = verificationId,
                                resendingToken = resendingToken,
                                onBackClick = {
                                    currentScreen = AppScreen.PHONE_NUMBER
                                },
                                onVerificationSuccess = { name, email, phone ->
                                    userName = name
                                    userEmail = email
                                    phoneNumber = phone
                                    currentScreen = if (name.isNotBlank()) AppScreen.HOME else AppScreen.PROFILE_SETUP
                                },
                                onNavigateToProfileSetup = {
                                    currentScreen = AppScreen.PROFILE_SETUP
                                },
                                onResendTokenUpdated = { token ->
                                    resendingToken = token
                                },
                                onVerificationIdUpdated = { newVerId ->
                                    verificationId = newVerId
                                }
                            )
                        }
                        AppScreen.PROFILE_SETUP -> {
                            ProfileSetupScreen(
                                phoneNumber = phoneNumber,
                                onBackClick = {
                                    currentScreen = AppScreen.OTP_VERIFICATION
                                },
                                onProfileCompleteClick = { name, email ->
                                    userName = name
                                    userEmail = email
                                    isGuest = false
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                        AppScreen.HOME -> {
                            MainContainerScreen(
                                isGuest = isGuest,
                                userName = userName,
                                userPhone = phoneNumber,
                                userEmail = userEmail,
                                onSignInClick = {
                                    isGuest = false
                                    currentScreen = AppScreen.PHONE_NUMBER
                                },
                                onSignOutClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    isGuest = false
                                    phoneNumber = ""
                                    userName = ""
                                    userEmail = ""
                                    verificationId = ""
                                    resendingToken = null
                                    currentScreen = AppScreen.WELCOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
