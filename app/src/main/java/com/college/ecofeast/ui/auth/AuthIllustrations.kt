package com.college.ecofeast.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.college.ecofeast.ui.theme.GoldenAccent
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import com.college.ecofeast.ui.theme.WarmIvory

/**
 * Custom Compose vector illustrations for EcoFeast authentication and onboarding flows.
 * Uses the EcoFeast brand palette: PrimaryForestGreen, SageGreen, SoftGreen, Terracotta, GoldenAccent, WarmIvory.
 */

@Composable
fun WelcomeFoodIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.42f,
                center = center
            )

            // Subtle outer ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.3f),
                radius = w * 0.46f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Basket Base (Terracotta)
            val basketTop = h * 0.52f
            val basketBottom = h * 0.76f
            val basketWidthTop = w * 0.62f
            val basketWidthBottom = w * 0.48f

            val basketPath = Path().apply {
                moveTo((w - basketWidthTop) / 2f, basketTop)
                lineTo((w + basketWidthTop) / 2f, basketTop)
                lineTo((w + basketWidthBottom) / 2f, basketBottom)
                quadraticTo(w / 2f, basketBottom + 10f, (w - basketWidthBottom) / 2f, basketBottom)
                close()
            }
            drawPath(path = basketPath, color = Terracotta)

            // Basket Rim
            drawRoundRect(
                color = Terracotta.copy(alpha = 0.95f),
                topLeft = Offset((w - basketWidthTop - 12f) / 2f, basketTop - 10f),
                size = Size(basketWidthTop + 12f, 14f),
                cornerRadius = CornerRadius(7f, 7f)
            )

            // Bread / Loaf (Golden Accent)
            drawOval(
                color = GoldenAccent,
                topLeft = Offset(w * 0.28f, h * 0.38f),
                size = Size(w * 0.22f, h * 0.18f)
            )

            // Red Tomato / Fruit
            drawCircle(
                color = Color(0xFFE25B45),
                radius = w * 0.10f,
                center = Offset(w * 0.58f, h * 0.44f)
            )

            // Leaves / Greens
            val leafPath1 = Path().apply {
                moveTo(w * 0.46f, h * 0.22f)
                quadraticTo(w * 0.66f, h * 0.22f, w * 0.60f, h * 0.42f)
                quadraticTo(w * 0.40f, h * 0.42f, w * 0.46f, h * 0.22f)
                close()
            }
            drawPath(path = leafPath1, color = PrimaryForestGreen)

            val leafPath2 = Path().apply {
                moveTo(w * 0.40f, h * 0.28f)
                quadraticTo(w * 0.24f, h * 0.28f, w * 0.30f, h * 0.44f)
                quadraticTo(w * 0.46f, h * 0.44f, w * 0.40f, h * 0.28f)
                close()
            }
            drawPath(path = leafPath2, color = SageGreen)

            // Heart / Community symbol atop food
            val heartPath = Path().apply {
                val cx = w * 0.50f
                val cy = h * 0.32f
                moveTo(cx, cy + 12f)
                cubicTo(cx - 16f, cy - 4f, cx - 22f, cy - 16f, cx - 10f, cy - 20f)
                cubicTo(cx - 2f, cy - 22f, cx, cy - 14f, cx, cy - 12f)
                cubicTo(cx, cy - 14f, cx + 2f, cy - 22f, cx + 10f, cy - 20f)
                cubicTo(cx + 22f, cy - 16f, cx + 16f, cy - 4f, cx, cy + 12f)
                close()
            }
            drawPath(path = heartPath, color = GoldenAccent)
        }
    }
}

@Composable
fun PhoneVerificationIllustration(modifier: Modifier = Modifier) {
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
                center = center
            )

            // Outer dashed circle
            drawCircle(
                color = SageGreen.copy(alpha = 0.4f),
                radius = w * 0.45f,
                center = center,
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )

            // Smartphone Body
            val phoneW = w * 0.34f
            val phoneH = h * 0.54f
            val phoneX = (w - phoneW) / 2f
            val phoneY = (h - phoneH) / 2f

            // Phone Shadow / Back outline
            drawRoundRect(
                color = PrimaryForestGreen.copy(alpha = 0.15f),
                topLeft = Offset(phoneX + 6f, phoneY + 8f),
                size = Size(phoneW, phoneH),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Main Phone Shell
            drawRoundRect(
                color = PrimaryForestGreen,
                topLeft = Offset(phoneX, phoneY),
                size = Size(phoneW, phoneH),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Phone Screen
            drawRoundRect(
                color = WarmIvory,
                topLeft = Offset(phoneX + 8f, phoneY + 16f),
                size = Size(phoneW - 16f, phoneH - 32f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Top Speaker Notch
            drawRoundRect(
                color = WarmIvory.copy(alpha = 0.6f),
                topLeft = Offset(w / 2f - 14f, phoneY + 6f),
                size = Size(28f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Shield / Lock Icon on Phone Screen
            val shieldCx = w / 2f
            val shieldCy = h / 2f - 4f
            val shieldPath = Path().apply {
                moveTo(shieldCx, shieldCy - 22f)
                lineTo(shieldCx + 18f, shieldCy - 14f)
                lineTo(shieldCx + 18f, shieldCy + 4f)
                cubicTo(shieldCx + 18f, shieldCy + 22f, shieldCx, shieldCy + 28f, shieldCx, shieldCy + 28f)
                cubicTo(shieldCx, shieldCy + 28f, shieldCx - 18f, shieldCy + 22f, shieldCx - 18f, shieldCy + 4f)
                lineTo(shieldCx - 18f, shieldCy - 14f)
                close()
            }
            drawPath(path = shieldPath, color = SageGreen)

            // Checkmark on Shield
            val checkPath = Path().apply {
                moveTo(shieldCx - 7f, shieldCy + 2f)
                lineTo(shieldCx - 2f, shieldCy + 7f)
                lineTo(shieldCx + 8f, shieldCy - 5f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(width = 4.5f)
            )

            // Decorative Floating SMS / Signal Waves
            val waveColor = GoldenAccent
            drawCircle(color = waveColor, radius = 6f, center = Offset(phoneX - 18f, h * 0.35f))
            drawCircle(color = Terracotta, radius = 9f, center = Offset(phoneX + phoneW + 20f, h * 0.40f))
            drawCircle(color = SageGreen, radius = 5f, center = Offset(phoneX + phoneW + 12f, h * 0.65f))

            // Floating message bubble
            val bubblePath = Path().apply {
                val bx = phoneX - 28f
                val by = h * 0.52f
                moveTo(bx, by)
                lineTo(bx + 32f, by)
                lineTo(bx + 32f, by + 20f)
                lineTo(bx + 10f, by + 20f)
                lineTo(bx + 4f, by + 26f)
                lineTo(bx + 4f, by + 20f)
                lineTo(bx, by + 20f)
                close()
            }
            drawPath(path = bubblePath, color = Terracotta)
        }
    }
}

@Composable
fun OtpVerificationIllustration(modifier: Modifier = Modifier) {
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
                center = center
            )

            // Outer ring
            drawCircle(
                color = PrimaryForestGreen.copy(alpha = 0.2f),
                radius = w * 0.45f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Security Lock Base Body
            val lockW = w * 0.38f
            val lockH = h * 0.36f
            val lockX = (w - lockW) / 2f
            val lockY = h * 0.45f

            // Lock Shackle (Top Arch)
            val shacklePath = Path().apply {
                val topY = lockY - h * 0.18f
                moveTo(lockX + 18f, lockY)
                lineTo(lockX + 18f, topY + 22f)
                cubicTo(lockX + 18f, topY, w - lockX - 18f, topY, w - lockX - 18f, topY + 22f)
                lineTo(w - lockX - 18f, lockY)
            }
            drawPath(
                path = shacklePath,
                color = GoldenAccent,
                style = Stroke(width = 12f)
            )

            // Lock Body (Forest Green)
            drawRoundRect(
                color = PrimaryForestGreen,
                topLeft = Offset(lockX, lockY),
                size = Size(lockW, lockH),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // Keyhole Circle
            val keyholeCx = w / 2f
            val keyholeCy = lockY + lockH * 0.42f
            drawCircle(
                color = WarmIvory,
                radius = 12f,
                center = Offset(keyholeCx, keyholeCy - 4f)
            )
            // Keyhole Slot
            val keyholeSlot = Path().apply {
                moveTo(keyholeCx - 6f, keyholeCy - 4f)
                lineTo(keyholeCx + 6f, keyholeCy - 4f)
                lineTo(keyholeCx + 8f, keyholeCy + 16f)
                lineTo(keyholeCx - 8f, keyholeCy + 16f)
                close()
            }
            drawPath(path = keyholeSlot, color = WarmIvory)

            // 6 OTP Code Dots floating above lock
            val dotY = h * 0.22f
            val dotSpacing = 18f
            val startX = w / 2f - (2.5f * dotSpacing)
            repeat(6) { index ->
                val isFilled = index < 4
                drawCircle(
                    color = if (isFilled) SageGreen else PrimaryForestGreen.copy(alpha = 0.3f),
                    radius = 7f,
                    center = Offset(startX + index * dotSpacing, dotY)
                )
            }

            // Green Leaf Badge on corner of lock
            val leafPath = Path().apply {
                val lx = lockX + lockW - 8f
                val ly = lockY + 8f
                moveTo(lx, ly)
                quadraticTo(lx + 24f, ly - 10f, lx + 20f, ly + 18f)
                quadraticTo(lx - 4f, ly + 20f, lx, ly)
                close()
            }
            drawPath(path = leafPath, color = SageGreen)
        }
    }
}

@Composable
fun ProfileSetupIllustration(modifier: Modifier = Modifier) {
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
                center = center
            )

            // Outer ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.25f),
                radius = w * 0.45f,
                center = center,
                style = Stroke(width = 3f)
            )

            // User Badge Card background
            val cardW = w * 0.52f
            val cardH = h * 0.58f
            val cardX = (w - cardW) / 2f
            val cardY = (h - cardH) / 2f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cardX, cardY),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Outer card border
            drawRoundRect(
                color = PrimaryForestGreen.copy(alpha = 0.2f),
                topLeft = Offset(cardX, cardY),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(width = 3f)
            )

            // Avatar Head Circle
            val avatarCx = w / 2f
            val avatarCy = cardY + cardH * 0.32f
            drawCircle(
                color = PrimaryForestGreen,
                radius = w * 0.12f,
                center = Offset(avatarCx, avatarCy)
            )

            // Avatar Shoulders Arc
            val shoulderPath = Path().apply {
                addArc(
                    oval = Rect(avatarCx - w * 0.18f, avatarCy + 4f, avatarCx + w * 0.18f, avatarCy + w * 0.36f),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f
                )
            }
            drawPath(path = shoulderPath, color = PrimaryForestGreen)

            // Inner User Silhouette (Warm Ivory)
            drawCircle(
                color = WarmIvory,
                radius = w * 0.055f,
                center = Offset(avatarCx, avatarCy - 2f)
            )

            // Text placeholder lines on card
            drawRoundRect(
                color = SageGreen,
                topLeft = Offset(cardX + 24f, cardY + cardH * 0.68f),
                size = Size(cardW - 48f, 10f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = SecondaryText.copy(alpha = 0.4f),
                topLeft = Offset(cardX + 36f, cardY + cardH * 0.78f),
                size = Size(cardW - 72f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Verified Eco Badge on Top Right
            val badgeCx = cardX + cardW - 8f
            val badgeCy = cardY + 12f
            drawCircle(
                color = GoldenAccent,
                radius = 18f,
                center = Offset(badgeCx, badgeCy)
            )
            // Check mark inside badge
            val badgeCheck = Path().apply {
                moveTo(badgeCx - 6f, badgeCy)
                lineTo(badgeCx - 2f, badgeCy + 4f)
                lineTo(badgeCx + 6f, badgeCy - 4f)
            }
            drawPath(
                path = badgeCheck,
                color = PrimaryForestGreen,
                style = Stroke(width = 3.5f)
            )
        }
    }
}

@Composable
fun GuestDiscoveryIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.42f,
                center = center
            )

            // Concentric map discovery radar rings
            drawCircle(
                color = SageGreen.copy(alpha = 0.25f),
                radius = w * 0.46f,
                center = center,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = PrimaryForestGreen.copy(alpha = 0.15f),
                radius = w * 0.32f,
                center = center,
                style = Stroke(width = 2f)
            )

            // Stylized Map Grid Lines
            val roadColor = WarmIvory
            drawRoundRect(
                color = roadColor,
                topLeft = Offset(w * 0.18f, h * 0.52f),
                size = Size(w * 0.64f, 16f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = roadColor,
                topLeft = Offset(w * 0.48f, h * 0.18f),
                size = Size(16f, h * 0.64f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Large Center Food Basket Pin
            val pinCenterX = w * 0.50f
            val pinCenterY = h * 0.40f
            val pinRadius = w * 0.12f

            // Pin Shadow
            drawCircle(
                color = PrimaryForestGreen.copy(alpha = 0.2f),
                radius = w * 0.08f,
                center = Offset(pinCenterX, pinCenterY + pinRadius * 2.2f)
            )

            // Pin Head
            drawCircle(
                color = PrimaryForestGreen,
                radius = pinRadius,
                center = Offset(pinCenterX, pinCenterY)
            )
            val pinPointer = Path().apply {
                moveTo(pinCenterX - pinRadius, pinCenterY + 4f)
                lineTo(pinCenterX + pinRadius, pinCenterY + 4f)
                lineTo(pinCenterX, pinCenterY + pinRadius * 2.2f)
                close()
            }
            drawPath(path = pinPointer, color = PrimaryForestGreen)

            // Food Basket inside Map Pin
            drawCircle(
                color = GoldenAccent,
                radius = pinRadius * 0.55f,
                center = Offset(pinCenterX, pinCenterY)
            )

            // Secondary Pins (Terracotta & Sage)
            val pin2X = w * 0.26f
            val pin2Y = h * 0.38f
            drawCircle(color = Terracotta, radius = 14f, center = Offset(pin2X, pin2Y))
            val p2 = Path().apply {
                moveTo(pin2X - 14f, pin2Y)
                lineTo(pin2X + 14f, pin2Y)
                lineTo(pin2X, pin2Y + 24f)
                close()
            }
            drawPath(path = p2, color = Terracotta)
            drawCircle(color = WarmIvory, radius = 6f, center = Offset(pin2X, pin2Y))

            val pin3X = w * 0.72f
            val pin3Y = h * 0.60f
            drawCircle(color = SageGreen, radius = 16f, center = Offset(pin3X, pin3Y))
            val p3 = Path().apply {
                moveTo(pin3X - 16f, pin3Y)
                lineTo(pin3X + 16f, pin3Y)
                lineTo(pin3X, pin3Y + 28f)
                close()
            }
            drawPath(path = p3, color = SageGreen)
            drawCircle(color = WarmIvory, radius = 7f, center = Offset(pin3X, pin3Y))
        }
    }
}

@Composable
fun HomeRescueIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.42f,
                center = center
            )

            // Outer ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.3f),
                radius = w * 0.46f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Globe / Community Earth Circle
            drawCircle(
                color = PrimaryForestGreen,
                radius = w * 0.28f,
                center = center
            )

            // Continent / Leaf curves on Earth
            val earthLeaf1 = Path().apply {
                moveTo(w * 0.35f, h * 0.42f)
                quadraticTo(w * 0.52f, h * 0.32f, w * 0.58f, h * 0.48f)
                quadraticTo(w * 0.42f, h * 0.58f, w * 0.35f, h * 0.42f)
                close()
            }
            drawPath(path = earthLeaf1, color = SageGreen)

            val earthLeaf2 = Path().apply {
                moveTo(w * 0.48f, h * 0.55f)
                quadraticTo(w * 0.68f, h * 0.58f, w * 0.62f, h * 0.70f)
                quadraticTo(w * 0.42f, h * 0.72f, w * 0.48f, h * 0.55f)
                close()
            }
            drawPath(path = earthLeaf2, color = SageGreen.copy(alpha = 0.8f))

            // Reaching hands / sprout in center
            val sproutPath = Path().apply {
                val cx = w / 2f
                val cy = h / 2f - 10f
                moveTo(cx, cy + 30f)
                quadraticTo(cx - 15f, cy + 10f, cx - 22f, cy - 15f)
                quadraticTo(cx - 5f, cy - 10f, cx, cy + 10f)
                quadraticTo(cx + 5f, cy - 10f, cx + 22f, cy - 15f)
                quadraticTo(cx + 15f, cy + 10f, cx, cy + 30f)
                close()
            }
            drawPath(path = sproutPath, color = GoldenAccent)

            // Sparkles around
            drawCircle(color = Terracotta, radius = 6f, center = Offset(w * 0.22f, h * 0.28f))
            drawCircle(color = GoldenAccent, radius = 8f, center = Offset(w * 0.78f, h * 0.30f))
            drawCircle(color = SageGreen, radius = 5f, center = Offset(w * 0.80f, h * 0.68f))
        }
    }
}
