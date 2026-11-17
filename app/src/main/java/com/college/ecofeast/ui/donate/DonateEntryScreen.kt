package com.college.ecofeast.ui.donate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.auth.WelcomeFoodIllustration
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.GoldenAccent
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import com.college.ecofeast.ui.theme.WarmIvory

/**
 * Storyboard Panel 1: Donate Entry Screen ("Share Surplus Food").
 * Arranged responsively with minimal top spacing, top-left back navigation,
 * custom Surplus Food Box illustration at top, title/subtitle, 3 benefit cards,
 * draft indicator, and primary/secondary CTA buttons.
 */
@Composable
fun DonateEntryScreen(
    isGuest: Boolean,
    hasDraft: Boolean,
    onStartNewDonation: () -> Unit,
    onContinueDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    onViewMyDonations: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = "Discard Unfinished Draft?",
                    fontWeight = FontWeight.Bold,
                    color = DeepText
                )
            },
            text = {
                Text(
                    text = "All saved fields and photo previews in your draft will be permanently deleted.",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onDiscardDraft()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Draft", color = PrimaryForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header Row with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepText
                    )
                }
            }

            if (isGuest) {
                // Storyboard Panel 10: Guest View (CTA)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ecofeast_logo),
                        contentDescription = "EcoFeast Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EcoFeast",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryForestGreen,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Good Food • Less Waste • Happier Communities",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                WelcomeFoodIllustration(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(160.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ready to make an impact?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in or create an account to donate food and track your activity.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontSize = 13.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryForestGreen,
                        contentColor = WarmIvory
                    )
                ) {
                    Text(
                        text = "Continue with Phone",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { onViewMyDonations() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, PrimaryForestGreen)
                ) {
                    Text(
                        text = "Browse as Guest",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryForestGreen
                    )
                }

            } else {
                // Storyboard Panel 1: Donate Entry Screen ("Share Surplus Food")
                
                // 1. Hero Vector Illustration FIRST (Cardboard Box filled with food)
                DonateSurplusFoodBoxIllustration(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(145.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Title & Subtitle
                Text(
                    text = "Share Surplus Food",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 23.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Turn your extra food into a meaningful contribution.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Three Impact Benefit Cards
                ImpactBenefitItem(
                    icon = Icons.Filled.Favorite,
                    title = "Help people in need",
                    description = "Make a positive difference in your community."
                )

                Spacer(modifier = Modifier.height(8.dp))

                ImpactBenefitItem(
                    icon = Icons.Filled.CheckCircle,
                    title = "Reduce food waste",
                    description = "Keep good food out of landfills."
                )

                Spacer(modifier = Modifier.height(8.dp))

                ImpactBenefitItem(
                    icon = Icons.Filled.Groups,
                    title = "Build a stronger community",
                    description = "Share, support, and care."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // If draft exists, show Continue Draft Card
                if (hasDraft) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, PrimaryForestGreen.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryForestGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Drafts,
                                        contentDescription = null,
                                        tint = WarmIvory,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Unfinished Draft Available",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryForestGreen,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = "Resume where you left off",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DeepText,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onContinueDraft,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                                ) {
                                    Text("Continue Draft", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { showDiscardDialog = true },
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD9774A))
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Discard Draft",
                                        tint = Color(0xFFD9774A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 4. Primary CTA Button: "Create Food Listing ->"
                Button(
                    onClick = onStartNewDonation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryForestGreen,
                        contentColor = WarmIvory
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (hasDraft) "Start New Donation" else "Create Food Listing",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Secondary Button: View My Donations
                OutlinedButton(
                    onClick = onViewMyDonations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.5.dp, PrimaryForestGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ListAlt,
                            contentDescription = null,
                            tint = PrimaryForestGreen,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View My Donations",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ImpactBenefitItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8D8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SoftGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 14.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Custom Compose Vector Illustration matching Panel 1 of Storyboard.
 * Depicts a Cardboard Box filled with fresh vegetables, carrots, tomatoes, bread, and greens,
 * surrounded by a soft green circle backdrop with floating leaves and hearts.
 */
@Composable
fun DonateSurplusFoodBoxIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft backdrop circle
            drawCircle(
                color = SoftGreen,
                radius = w * 0.40f,
                center = Offset(w * 0.50f, h * 0.52f)
            )

            // Outer soft green ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.35f),
                radius = w * 0.44f,
                center = Offset(w * 0.50f, h * 0.52f),
                style = Stroke(width = 2.5f)
            )

            // Floating Hearts above box (Terracotta & Forest Green)
            val heart1 = Path().apply {
                val cx = w * 0.48f
                val cy = h * 0.15f
                moveTo(cx, cy)
                cubicTo(cx - 8f, cy - 10f, cx - 18f, cy + 2f, cx, cy + 12f)
                cubicTo(cx + 18f, cy + 2f, cx + 8f, cy - 10f, cx, cy)
                close()
            }
            drawPath(path = heart1, color = PrimaryForestGreen)

            val heart2 = Path().apply {
                val cx = w * 0.62f
                val cy = h * 0.12f
                moveTo(cx, cy)
                cubicTo(cx - 6f, cy - 8f, cx - 14f, cy + 2f, cx, cy + 10f)
                cubicTo(cx + 14f, cy + 2f, cx + 6f, cy - 8f, cx, cy)
                close()
            }
            drawPath(path = heart2, color = Terracotta)

            // Floating Leaf Left
            val leafLeft = Path().apply {
                val lx = w * 0.28f
                val ly = h * 0.22f
                moveTo(lx, ly)
                quadraticTo(lx - 12f, ly - 10f, lx - 8f, ly + 14f)
                quadraticTo(lx + 10f, ly + 8f, lx, ly)
                close()
            }
            drawPath(path = leafLeft, color = SageGreen)

            // Floating Leaf Right
            val leafRight = Path().apply {
                val rx = w * 0.74f
                val ry = h * 0.24f
                moveTo(rx, ry)
                quadraticTo(rx + 12f, ry - 10f, rx + 8f, ry + 14f)
                quadraticTo(rx - 10f, ry + 8f, rx, ry)
                close()
            }
            drawPath(path = leafRight, color = PrimaryForestGreen)

            // --- FOOD INSIDE BOX ---
            val boxW = w * 0.54f
            val boxH = h * 0.38f
            val boxX = (w - boxW) / 2f
            val boxY = h * 0.48f

            // Sourdough Bread (Golden Accent)
            drawOval(
                color = GoldenAccent,
                topLeft = Offset(boxX + boxW * 0.52f, boxY - h * 0.24f),
                size = Size(boxW * 0.40f, h * 0.30f)
            )

            // Red Tomato (Terracotta / Red)
            drawCircle(
                color = Color(0xFFE25B45),
                radius = w * 0.08f,
                center = Offset(boxX + boxW * 0.28f, boxY - h * 0.05f)
            )

            // Carrot (Golden / Terracotta)
            val carrot = Path().apply {
                moveTo(boxX + boxW * 0.44f, boxY - h * 0.20f)
                lineTo(boxX + boxW * 0.55f, boxY - h * 0.18f)
                lineTo(boxX + boxW * 0.46f, boxY + h * 0.04f)
                close()
            }
            drawPath(path = carrot, color = Terracotta)

            // Fresh Leaves / Greens
            val greenLeaf1 = Path().apply {
                moveTo(boxX + boxW * 0.12f, boxY - h * 0.20f)
                quadraticTo(boxX + boxW * 0.38f, boxY - h * 0.22f, boxX + boxW * 0.32f, boxY)
                quadraticTo(boxX + boxW * 0.06f, boxY - h * 0.02f, boxX + boxW * 0.12f, boxY - h * 0.20f)
                close()
            }
            drawPath(path = greenLeaf1, color = PrimaryForestGreen)

            val greenLeaf2 = Path().apply {
                moveTo(boxX + boxW * 0.34f, boxY - h * 0.26f)
                quadraticTo(boxX + boxW * 0.56f, boxY - h * 0.20f, boxX + boxW * 0.46f, boxY - h * 0.02f)
                quadraticTo(boxX + boxW * 0.22f, boxY - h * 0.08f, boxX + boxW * 0.34f, boxY - h * 0.26f)
                close()
            }
            drawPath(path = greenLeaf2, color = SageGreen)

            // --- CARDBOARD BOX BASE ---
            // Box Shadow
            drawRoundRect(
                color = DeepText.copy(alpha = 0.08f),
                topLeft = Offset(boxX + 2f, boxY + boxH * 0.65f),
                size = Size(boxW, boxH * 0.40f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Box Body (Cardboard Brown)
            val boxBody = Path().apply {
                moveTo(boxX, boxY)
                lineTo(boxX + boxW, boxY)
                lineTo(boxX + boxW - 6f, boxY + boxH)
                lineTo(boxX + 6f, boxY + boxH)
                close()
            }
            drawPath(path = boxBody, color = Color(0xFFD4A373))

            // Box Top Rim Flap
            drawRoundRect(
                color = Color(0xFFC38E5C),
                topLeft = Offset(boxX - 4f, boxY - 5f),
                size = Size(boxW + 8f, 12f),
                cornerRadius = CornerRadius(5f, 5f)
            )

            // Heart Emblem on Cardboard Box Center
            val boxHeart = Path().apply {
                val cx = w * 0.50f
                val cy = boxY + boxH * 0.50f
                moveTo(cx, cy - 4f)
                cubicTo(cx - 5f, cy - 10f, cx - 12f, cy - 2f, cx, cy + 7f)
                cubicTo(cx + 12f, cy - 2f, cx + 5f, cy - 10f, cx, cy - 4f)
                close()
            }
            drawPath(path = boxHeart, color = PrimaryForestGreen)
        }
    }
}
