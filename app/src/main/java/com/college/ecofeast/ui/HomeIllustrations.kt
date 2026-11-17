package com.college.ecofeast.ui

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
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.GoldenAccent
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import com.college.ecofeast.ui.theme.WarmIvory

/**
 * Custom Compose Vector Illustration for the Home Hero Banner.
 * Depicts two hands extending/sharing a cardboard box filled with fresh food (bread, tomatoes, greens).
 */
@Composable
fun HeroBannerSharingIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background soft sun/glow
            drawCircle(
                color = GoldenAccent.copy(alpha = 0.35f),
                radius = w * 0.18f,
                center = Offset(w * 0.82f, h * 0.22f)
            )

            // Soft decorative foliage behind box
            val bgLeaf = Path().apply {
                moveTo(w * 0.72f, h * 0.15f)
                quadraticTo(w * 0.92f, h * 0.20f, w * 0.85f, h * 0.55f)
                quadraticTo(w * 0.65f, h * 0.50f, w * 0.72f, h * 0.15f)
                close()
            }
            drawPath(path = bgLeaf, color = SageGreen.copy(alpha = 0.5f))

            // Food Box in Center
            val boxW = w * 0.44f
            val boxH = h * 0.38f
            val boxX = (w - boxW) / 2f
            val boxY = h * 0.42f

            // Cardboard Box Shadow
            drawRoundRect(
                color = DeepText.copy(alpha = 0.08f),
                topLeft = Offset(boxX + 4f, boxY + boxH * 0.6f),
                size = Size(boxW, boxH * 0.45f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Main Cardboard Box
            val boxPath = Path().apply {
                moveTo(boxX, boxY)
                lineTo(boxX + boxW, boxY)
                lineTo(boxX + boxW - 8f, boxY + boxH)
                lineTo(boxX + 8f, boxY + boxH)
                close()
            }
            drawPath(path = boxPath, color = Color(0xFFD4A373))

            // Box Flap Details & Leaf Emblem
            drawRoundRect(
                color = Color(0xFFC38E5C),
                topLeft = Offset(boxX - 4f, boxY - 6f),
                size = Size(boxW + 8f, 14f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Small Leaf Emblem on Box
            val boxLeaf = Path().apply {
                val cx = w * 0.50f
                val cy = boxY + boxH * 0.48f
                moveTo(cx, cy - 12f)
                quadraticTo(cx + 12f, cy - 8f, cx + 8f, cy + 10f)
                quadraticTo(cx - 10f, cy + 6f, cx, cy - 12f)
                close()
            }
            drawPath(path = boxLeaf, color = PrimaryForestGreen)

            // --- FOOD INSIDE BOX ---
            // Sourdough Bread (Golden Accent)
            drawOval(
                color = GoldenAccent,
                topLeft = Offset(boxX + boxW * 0.52f, boxY - h * 0.26f),
                size = Size(boxW * 0.42f, h * 0.32f)
            )
            // Bread score lines
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(boxX + boxW * 0.62f, boxY - h * 0.18f),
                end = Offset(boxX + boxW * 0.72f, boxY - h * 0.10f),
                strokeWidth = 3f
            )

            // Tomato (Red-Terracotta)
            drawCircle(
                color = Color(0xFFE25B45),
                radius = w * 0.07f,
                center = Offset(boxX + boxW * 0.28f, boxY - h * 0.06f)
            )

            // Fresh Leaf / Greens
            val green1 = Path().apply {
                moveTo(boxX + boxW * 0.12f, boxY - h * 0.22f)
                quadraticTo(boxX + boxW * 0.42f, boxY - h * 0.24f, boxX + boxW * 0.35f, boxY)
                quadraticTo(boxX + boxW * 0.05f, boxY - h * 0.02f, boxX + boxW * 0.12f, boxY - h * 0.22f)
                close()
            }
            drawPath(path = green1, color = PrimaryForestGreen)

            val green2 = Path().apply {
                moveTo(boxX + boxW * 0.38f, boxY - h * 0.28f)
                quadraticTo(boxX + boxW * 0.58f, boxY - h * 0.22f, boxX + boxW * 0.48f, boxY - h * 0.04f)
                quadraticTo(boxX + boxW * 0.22f, boxY - h * 0.10f, boxX + boxW * 0.38f, boxY - h * 0.28f)
                close()
            }
            drawPath(path = green2, color = SageGreen)

            // --- HANDS EXTENDING FROM LEFT AND RIGHT ---
            // Left Hand (Donor)
            val leftHand = Path().apply {
                moveTo(0f, h * 0.58f)
                quadraticTo(boxX - 10f, h * 0.58f, boxX + 12f, boxY + boxH * 0.25f)
                lineTo(boxX + 22f, boxY + boxH * 0.50f)
                quadraticTo(boxX - 20f, h * 0.72f, 0f, h * 0.72f)
                close()
            }
            drawPath(path = leftHand, color = PrimaryForestGreen.copy(alpha = 0.85f))

            // Right Hand (Receiver)
            val rightHand = Path().apply {
                moveTo(w, h * 0.58f)
                quadraticTo(boxX + boxW + 10f, h * 0.58f, boxX + boxW - 12f, boxY + boxH * 0.25f)
                lineTo(boxX + boxW - 22f, boxY + boxH * 0.50f)
                quadraticTo(boxX + boxW + 20f, h * 0.72f, w, h * 0.72f)
                close()
            }
            drawPath(path = rightHand, color = Terracotta.copy(alpha = 0.85f))
        }
    }
}

/**
 * Custom Compose Vector Illustration for "Available Near You" empty state.
 * Depicts a stylized folded map with location pin and growing green leaves.
 */
@Composable
fun MapEmptyStateIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background soft circle
            drawCircle(
                color = SoftGreen,
                radius = w * 0.38f,
                center = center
            )

            // Folded Map Base
            val mapW = w * 0.58f
            val mapH = h * 0.38f
            val mapX = (w - mapW) / 2f
            val mapY = h * 0.48f

            val panelW = mapW / 3f

            // Panel 1 (Left)
            val p1 = Path().apply {
                moveTo(mapX, mapY)
                lineTo(mapX + panelW, mapY - 8f)
                lineTo(mapX + panelW, mapY + mapH - 8f)
                lineTo(mapX, mapY + mapH)
                close()
            }
            drawPath(path = p1, color = SageGreen.copy(alpha = 0.6f))

            // Panel 2 (Center)
            val p2 = Path().apply {
                moveTo(mapX + panelW, mapY - 8f)
                lineTo(mapX + panelW * 2f, mapY)
                lineTo(mapX + panelW * 2f, mapY + mapH)
                lineTo(mapX + panelW, mapY + mapH - 8f)
                close()
            }
            drawPath(path = p2, color = SageGreen.copy(alpha = 0.85f))

            // Panel 3 (Right)
            val p3 = Path().apply {
                moveTo(mapX + panelW * 2f, mapY)
                lineTo(mapX + mapW, mapY - 8f)
                lineTo(mapX + mapW, mapY + mapH - 8f)
                lineTo(mapX + panelW * 2f, mapY + mapH)
                close()
            }
            drawPath(path = p3, color = SageGreen.copy(alpha = 0.6f))

            // Map Road Lines
            val roadPath = Path().apply {
                moveTo(mapX + 10f, mapY + mapH * 0.5f)
                quadraticTo(w / 2f, mapY + mapH * 0.2f, mapX + mapW - 10f, mapY + mapH * 0.6f)
            }
            drawPath(
                path = roadPath,
                color = WarmIvory,
                style = Stroke(
                    width = 5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )

            // Map Pin (Terracotta) in center
            val pinCx = w / 2f
            val pinCy = h * 0.34f
            val pinR = w * 0.08f

            drawCircle(
                color = Terracotta,
                radius = pinR,
                center = Offset(pinCx, pinCy)
            )
            val pinPointer = Path().apply {
                moveTo(pinCx - pinR, pinCy + 2f)
                lineTo(pinCx + pinR, pinCy + 2f)
                lineTo(pinCx, pinCy + pinR * 2.2f)
                close()
            }
            drawPath(path = pinPointer, color = Terracotta)

            // Inner pin dot
            drawCircle(
                color = WarmIvory,
                radius = pinR * 0.45f,
                center = Offset(pinCx, pinCy)
            )

            // Floating decorative leaves on sides
            val leafLeft = Path().apply {
                val lx = mapX - 12f
                val ly = h * 0.38f
                moveTo(lx, ly)
                quadraticTo(lx - 20f, ly - 15f, lx - 18f, ly + 15f)
                quadraticTo(lx + 8f, ly + 12f, lx, ly)
                close()
            }
            drawPath(path = leafLeft, color = PrimaryForestGreen)

            val leafRight = Path().apply {
                val rx = mapX + mapW + 12f
                val ry = h * 0.38f
                moveTo(rx, ry)
                quadraticTo(rx + 20f, ry - 15f, rx + 18f, ry + 15f)
                quadraticTo(rx - 8f, ry + 12f, rx, ry)
                close()
            }
            drawPath(path = leafRight, color = PrimaryForestGreen)
        }
    }
}
