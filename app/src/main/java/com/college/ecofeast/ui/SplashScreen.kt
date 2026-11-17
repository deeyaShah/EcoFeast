package com.college.ecofeast.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.theme.EcoFeastTheme
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color Matrix that boosts near-white background pixels to pure white (#FFFFFF)
// so BlendMode.Multiply makes the background 100% transparent on WarmIvory.
private val RemoveWhiteBgMatrix = ColorMatrix(
    floatArrayOf(
        1.08f, 0f, 0f, 0f, -5f,
        0f, 1.08f, 0f, 0f, -5f,
        0f, 0f, 1.08f, 0f, -5f,
        0f, 0f, 0f, 1f, 0f
    )
)

@Composable
fun EcoFeastSplashScreen(
    onSplashFinished: (() -> Unit)? = null
) {
    val isPreview = LocalInspectionMode.current
    val contentScale = remember { Animatable(if (isPreview) 1f else 0.88f) }
    val contentAlpha = remember { Animatable(if (isPreview) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            launch {
                contentScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
            launch {
                contentAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }

            // Splash duration (~2.5s total)
            delay(2500)
            onSplashFinished?.invoke()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .scale(contentScale.value)
                .alpha(contentAlpha.value)
        ) {
            // Bowl Logo Image with offscreen compositing strategy & Multiply blend to eliminate white background
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ecofeast_logo),
                    contentDescription = "EcoFeast Bowl Logo",
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

            Spacer(modifier = Modifier.height(28.dp))

            // EcoFeast Title matching reference image:
            // "Eco" in deep forest green (#1B4C28), "Feast" in vibrant leaf green (#609A32)
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF1B4C28),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp
                        )
                    ) {
                        append("Eco")
                    }
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF609A32),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp
                        )
                    ) {
                        append("Feast")
                    }
                },
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtext / Tagline matching exact reference image color & style
            Text(
                text = "Rescue Food. Reduce Waste.",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E3A24),
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Leaf Divider
            LeafDivider()
        }
    }
}

@Composable
fun LeafDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(55.dp)
                .height(1.5.dp)
                .background(Color(0xFF609A32).copy(alpha = 0.5f))
        )

        Canvas(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(22.dp)
        ) {
            val w = size.width
            val h = size.height

            // Left leaf
            val leftLeaf = Path().apply {
                moveTo(w * 0.5f, h * 0.85f)
                quadraticTo(w * 0.1f, h * 0.5f, w * 0.25f, h * 0.15f)
                quadraticTo(w * 0.65f, h * 0.35f, w * 0.5f, h * 0.85f)
                close()
            }
            drawPath(path = leftLeaf, color = PrimaryForestGreen)

            // Right leaf
            val rightLeaf = Path().apply {
                moveTo(w * 0.5f, h * 0.85f)
                quadraticTo(w * 0.9f, h * 0.5f, w * 0.75f, h * 0.15f)
                quadraticTo(w * 0.35f, h * 0.35f, w * 0.5f, h * 0.85f)
                close()
            }
            drawPath(path = rightLeaf, color = SageGreen)
        }

        Box(
            modifier = Modifier
                .width(55.dp)
                .height(1.5.dp)
                .background(Color(0xFF609A32).copy(alpha = 0.5f))
        )
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    EcoFeastSplashScreen(onSplashFinished = onAnimationFinished)
}

@Preview(showBackground = true)
@Composable
fun EcoFeastSplashScreenPreview() {
    EcoFeastTheme {
        EcoFeastSplashScreen()
    }
}
