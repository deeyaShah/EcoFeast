package com.college.ecofeast.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.auth.WelcomeFoodIllustration
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.WarmIvory

private val RemoveWhiteBgMatrix = ColorMatrix(
    floatArrayOf(
        1.08f, 0f, 0f, 0f, -5f,
        0f, 1.08f, 0f, 0f, -5f,
        0f, 0f, 1.08f, 0f, -5f,
        0f, 0f, 0f, 1f, 0f
    )
)

@Composable
fun WelcomeScreen(
    onContinueWithPhoneClick: () -> Unit = {},
    onBrowseAsGuestClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Upper/Middle Content: Logo, Headline & Decorative Food Illustration
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.92f, animationSpec = tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // EcoFeast Logo with transparent background effect
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ecofeast_logo),
                            contentDescription = "EcoFeast Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.colorMatrix(RemoveWhiteBgMatrix)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ecofeast_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.tint(WarmIvory, blendMode = BlendMode.Multiply)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Heading
                    Text(
                        text = "Welcome to EcoFeast",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryForestGreen,
                            fontSize = 28.sp,
                            letterSpacing = 0.3.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tagline
                    Text(
                        text = "Good food shouldn't go to waste.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText,
                            fontSize = 16.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Supporting Sentence
                    Text(
                        text = "Rescue surplus food, connect with your community, and make a difference.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = SecondaryText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Food Sharing & Sustainability Illustration
                    WelcomeFoodIllustration(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lower Section: Primary & Secondary Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Primary Button: Continue with Phone Number
                Button(
                    onClick = onContinueWithPhoneClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryForestGreen,
                        contentColor = WarmIvory
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "Continue with Phone Number",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }

                // Secondary Button: Browse Food as Guest
                OutlinedButton(
                    onClick = onBrowseAsGuestClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, PrimaryForestGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SoftGreen.copy(alpha = 0.4f),
                        contentColor = PrimaryForestGreen
                    )
                ) {
                    Text(
                        text = "Browse Food as Guest",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
