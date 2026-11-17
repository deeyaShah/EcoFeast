package com.college.ecofeast.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.auth.ProfileSetupIllustration
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import com.college.ecofeast.ui.theme.WarmIvory

/**
 * Polished Profile screen supporting both Guest and Signed-In user states.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    isGuest: Boolean = false,
    userName: String = "",
    userPhone: String = "",
    userEmail: String = "",
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "My Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = if (isGuest) "Guest Mode" else "EcoFeast Community Member",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Avatar Illustration
            ProfileSetupIllustration(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(160.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isGuest) {
                // Guest Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFC8D6B9))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Guest Mode Active",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You are currently exploring EcoFeast as a guest. Sign in with your phone number to manage your profile, donate surplus food, claim listings, and view your activity history.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SecondaryText,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryForestGreen,
                                contentColor = WarmIvory
                            )
                        ) {
                            Text(
                                text = "Sign In with Phone Number",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Signed-In User Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFC8D6B9))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // User Name
                        Text(
                            text = if (userName.isNotBlank()) userName else "EcoFeast User",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Verified Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftGreen)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✅ Verified Account",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Number Row
                        if (userPhone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = PrimaryForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = userPhone,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DeepText,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        }

                        // Email Row
                        if (userEmail.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = PrimaryForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DeepText,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sign Out Button
                        OutlinedButton(
                            onClick = onSignOutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.5.dp, Terracotta)
                        ) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Terracotta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version & Community Message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EcoFeast v1.0 • College Project",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryForestGreen,
                            fontSize = 12.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hyperlocal Peer-to-Peer Food Surplus Rescue & Redistribution System",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
