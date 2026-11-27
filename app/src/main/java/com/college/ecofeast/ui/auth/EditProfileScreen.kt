package com.college.ecofeast.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    phoneNumber: String,
    onBackClick: () -> Unit = {},
    onProfileSaved: (fullName: String, email: String) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var fullName by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var isLoading by remember { mutableStateOf(false) }

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
                .statusBarsPadding()
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
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Update your profile information visible to the EcoFeast community.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SecondaryText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Registered Phone Number (Read-only)
                    Text(
                        text = "Registered Phone Number",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 13.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AuthTextField(
                        value = phoneNumber.ifBlank { "Not provided" },
                        onValueChange = {},
                        placeholderText = "Phone Number",
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                imageVector = PhoneIcon,
                                contentDescription = null,
                                tint = SecondaryText
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Full Name Input
                    Text(
                        text = "Full Name *",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 13.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email Address Input
                    Text(
                        text = "Email Address *",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 13.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons: Save Changes & Cancel
                    PrimaryAuthButton(
                        text = "Save Changes",
                        loadingText = "Saving Changes...",
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
                                val updates = hashMapOf<String, Any>(
                                    "fullName" to fullName.trim(),
                                    "email" to email.trim()
                                )

                                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                                    .set(updates, SetOptions.merge())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onProfileSaved(fullName.trim(), email.trim())
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        coroutineScope.launch {
                                            snackbarHostState.showFeedback(e.localizedMessage ?: "Failed to update profile. Please try again.")
                                        }
                                    }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBackClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
