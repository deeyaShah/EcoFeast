package com.college.ecofeast.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.college.ecofeast.ui.theme.GoldenAccent
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import com.college.ecofeast.ui.theme.WarmIvory

@Composable
fun OnboardingIllustration(
    pageType: OnboardingPageType,
    modifier: Modifier = Modifier
) {
    when (pageType) {
        OnboardingPageType.SAVE_FOOD -> SaveFoodIllustration(modifier)
        OnboardingPageType.FIND_NEARBY -> FindFoodIllustration(modifier)
        OnboardingPageType.CONNECT_RESCUE -> ClaimConnectIllustration(modifier)
    }
}

@Composable
fun SaveFoodIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.44f,
                center = center
            )

            // Outer subtle stroke ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.25f),
                radius = w * 0.47f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Basket Container (Terracotta base)
            val basketTop = h * 0.52f
            val basketBottom = h * 0.78f
            val basketWidthTop = w * 0.65f
            val basketWidthBottom = w * 0.50f

            val basketPath = Path().apply {
                moveTo((w - basketWidthTop) / 2f, basketTop)
                lineTo((w + basketWidthTop) / 2f, basketTop)
                lineTo((w + basketWidthBottom) / 2f, basketBottom)
                quadraticTo(w / 2f, basketBottom + 12f, (w - basketWidthBottom) / 2f, basketBottom)
                close()
            }

            drawPath(
                path = basketPath,
                color = Terracotta
            )

            // Basket rim
            drawRoundRect(
                color = Terracotta.copy(alpha = 0.92f),
                topLeft = Offset((w - basketWidthTop - 12f) / 2f, basketTop - 12f),
                size = Size(basketWidthTop + 12f, 16f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Bread / Loaf (Golden) peeking out left
            drawOval(
                color = GoldenAccent,
                topLeft = Offset(w * 0.28f, h * 0.38f),
                size = Size(w * 0.22f, h * 0.20f)
            )

            // Fruit / Tomato (Warm Red-Terracotta)
            drawCircle(
                color = Color(0xFFE25B45),
                radius = w * 0.11f,
                center = Offset(w * 0.58f, h * 0.45f)
            )

            // Fresh Leaf / Greens on top
            val leafPath = Path().apply {
                moveTo(w * 0.48f, h * 0.22f)
                quadraticTo(w * 0.68f, h * 0.22f, w * 0.62f, h * 0.42f)
                quadraticTo(w * 0.42f, h * 0.42f, w * 0.48f, h * 0.22f)
                close()
            }
            drawPath(path = leafPath, color = PrimaryForestGreen)

            // Second small leaf
            val leafPath2 = Path().apply {
                moveTo(w * 0.42f, h * 0.28f)
                quadraticTo(w * 0.25f, h * 0.28f, w * 0.32f, h * 0.44f)
                quadraticTo(w * 0.48f, h * 0.44f, w * 0.42f, h * 0.28f)
                close()
            }
            drawPath(path = leafPath2, color = SageGreen)

            // Sparkle / Freshness dots
            drawCircle(color = GoldenAccent, radius = 6f, center = Offset(w * 0.72f, h * 0.28f))
            drawCircle(color = SageGreen, radius = 4f, center = Offset(w * 0.25f, h * 0.22f))
        }
    }
}

@Composable
fun FindFoodIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.44f,
                center = center
            )

            // Outer subtle stroke ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.25f),
                radius = w * 0.47f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Stylized map grid paths
            val roadColor = WarmIvory
            drawRoundRect(
                color = roadColor,
                topLeft = Offset(w * 0.2f, h * 0.48f),
                size = Size(w * 0.6f, 18f),
                cornerRadius = CornerRadius(9f, 9f)
            )
            drawRoundRect(
                color = roadColor,
                topLeft = Offset(w * 0.46f, h * 0.2f),
                size = Size(18f, h * 0.6f),
                cornerRadius = CornerRadius(9f, 9f)
            )

            // Small business / home block 1
            drawRoundRect(
                color = PrimaryForestGreen.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.25f, h * 0.28f),
                size = Size(w * 0.16f, h * 0.16f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Small business / home block 2
            drawRoundRect(
                color = Terracotta.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.58f, h * 0.58f),
                size = Size(w * 0.18f, h * 0.16f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // User location pulsing ring
            drawCircle(
                color = PrimaryForestGreen.copy(alpha = 0.2f),
                radius = w * 0.12f,
                center = Offset(w * 0.33f, h * 0.57f)
            )
            drawCircle(
                color = PrimaryForestGreen,
                radius = w * 0.04f,
                center = Offset(w * 0.33f, h * 0.57f)
            )

            // Main Map Pin (PrimaryForestGreen pin with golden center)
            val pinCenterX = w * 0.55f
            val pinCenterY = h * 0.35f
            val pinRadius = w * 0.09f

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

            drawCircle(
                color = GoldenAccent,
                radius = pinRadius * 0.45f,
                center = Offset(pinCenterX, pinCenterY)
            )

            // Secondary Map Pin (Terracotta pin)
            val pin2X = w * 0.67f
            val pin2Y = h * 0.64f
            val pin2R = w * 0.065f

            drawCircle(color = Terracotta, radius = pin2R, center = Offset(pin2X, pin2Y))
            val pin2Pointer = Path().apply {
                moveTo(pin2X - pin2R, pin2Y + 2f)
                lineTo(pin2X + pin2R, pin2Y + 2f)
                lineTo(pin2X, pin2Y + pin2R * 2f)
                close()
            }
            drawPath(path = pin2Pointer, color = Terracotta)
            drawCircle(color = WarmIvory, radius = pin2R * 0.4f, center = Offset(pin2X, pin2Y))
        }
    }
}

@Composable
fun ClaimConnectIllustration(modifier: Modifier = Modifier) {
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
                radius = w * 0.44f,
                center = center
            )

            // Outer subtle stroke ring
            drawCircle(
                color = SageGreen.copy(alpha = 0.25f),
                radius = w * 0.47f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Connecting dashed curve between donor and receiver
            val connectPath = Path().apply {
                moveTo(w * 0.30f, h * 0.60f)
                quadraticTo(w * 0.50f, h * 0.72f, w * 0.70f, h * 0.60f)
            }
            drawPath(
                path = connectPath,
                color = SageGreen,
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )

            // Donor Avatar (Left - PrimaryForestGreen)
            val donorX = w * 0.28f
            val donorY = h * 0.54f
            drawCircle(color = PrimaryForestGreen, radius = w * 0.08f, center = Offset(donorX, donorY - w * 0.10f))
            drawArc(
                color = PrimaryForestGreen,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(donorX - w * 0.12f, donorY - w * 0.02f),
                size = Size(w * 0.24f, h * 0.24f)
            )

            // Receiver Avatar (Right - SageGreen)
            val rxX = w * 0.72f
            val rxY = h * 0.54f
            drawCircle(color = SageGreen, radius = w * 0.08f, center = Offset(rxX, rxY - w * 0.10f))
            drawArc(
                color = SageGreen,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(rxX - w * 0.12f, rxY - w * 0.02f),
                size = Size(w * 0.24f, h * 0.24f)
            )

            // Shared Food Parcel in center (Terracotta & Golden)
            val boxX = w * 0.50f
            val boxY = h * 0.55f
            drawRoundRect(
                color = Terracotta,
                topLeft = Offset(boxX - 22f, boxY - 18f),
                size = Size(44f, 36f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Golden ribbon
            drawRoundRect(
                color = GoldenAccent,
                topLeft = Offset(boxX - 5f, boxY - 18f),
                size = Size(10f, 36f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Speech Bubble 1 (Left top)
            drawRoundRect(
                color = WarmIvory,
                topLeft = Offset(w * 0.22f, h * 0.22f),
                size = Size(w * 0.24f, h * 0.14f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = PrimaryForestGreen.copy(alpha = 0.6f),
                topLeft = Offset(w * 0.26f, h * 0.27f),
                size = Size(w * 0.16f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Speech Bubble 2 (Right top)
            drawRoundRect(
                color = WarmIvory,
                topLeft = Offset(w * 0.54f, h * 0.22f),
                size = Size(w * 0.24f, h * 0.14f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawCircle(
                color = Terracotta,
                radius = 8f,
                center = Offset(w * 0.66f, h * 0.29f)
            )
        }
    }
}
