package com.college.ecofeast.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.WarmIvory

enum class EcoFeastTab {
    HOME,
    DONATE,
    NEARBY,
    PROFILE
}

/**
 * Custom Shape for the Bottom Navigation Bar creating a smooth, symmetrical cradle curve
 * cutout in the top edge around the center raised EcoFeast logo.
 */
class CurvedBottomNavigationShape(
    private val cornerRadius: Dp = 20.dp,
    private val cutoutWidth: Dp = 72.dp,
    private val cutoutDepth: Dp = 20.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val cornerRadiusPx = with(density) { cornerRadius.toPx() }
            val cutoutWidthPx = with(density) { cutoutWidth.toPx() }
            val cutoutDepthPx = with(density) { cutoutDepth.toPx() }

            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val halfCutout = cutoutWidthPx / 2f

            reset()
            // Start at top-left
            moveTo(0f, cornerRadiusPx)
            // Top-left corner curve
            quadraticTo(0f, 0f, cornerRadiusPx, 0f)

            // Top edge to start of cradle cutout
            lineTo(centerX - halfCutout, 0f)

            // Left side of cradle curve down to center bottom
            cubicTo(
                centerX - halfCutout + halfCutout * 0.45f, 0f,
                centerX - halfCutout * 0.4f, cutoutDepthPx,
                centerX, cutoutDepthPx
            )

            // Right side of cradle curve back up to top edge
            cubicTo(
                centerX + halfCutout * 0.4f, cutoutDepthPx,
                centerX + halfCutout - halfCutout * 0.45f, 0f,
                centerX + halfCutout, 0f
            )

            // Top edge to top-right corner
            lineTo(width - cornerRadiusPx, 0f)

            // Top-right corner curve
            quadraticTo(width, 0f, width, cornerRadiusPx)

            // Right edge down to bottom
            lineTo(width, height)

            // Bottom edge to left
            lineTo(0f, height)

            // Close path
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Single Unified Curved Bottom Navigation Bar for EcoFeast with 4 interactive destinations
 * and a raised decorative central EcoFeast logo medallion.
 *
 * Background: Single continuous Warm Cream (#F7F5EE) matching screen palette.
 * Layout: Perfectly balanced 1:1 option spacing with zero text truncation on any screen size.
 */
@Composable
fun EcoFeastBottomNavigation(
    currentTab: EcoFeastTab,
    onTabSelected: (EcoFeastTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarContentHeight = 64.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Single Unified Warm Cream Navigation Surface (#F7F5EE)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = CurvedBottomNavigationShape(
                cornerRadius = 20.dp,
                cutoutWidth = 72.dp,
                cutoutDepth = 20.dp
            ),
            color = WarmIvory,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarContentHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    .navigationBarsPadding()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Options Group: Home & Donate (evenly spaced)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTabItem(
                        label = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        isSelected = currentTab == EcoFeastTab.HOME,
                        onClick = { onTabSelected(EcoFeastTab.HOME) }
                    )

                    NavigationTabItem(
                        label = "Donate",
                        selectedIcon = Icons.Filled.Favorite,
                        unselectedIcon = Icons.Outlined.FavoriteBorder,
                        isSelected = currentTab == EcoFeastTab.DONATE,
                        onClick = { onTabSelected(EcoFeastTab.DONATE) }
                    )
                }

                // Center Gap for Decorative Logo Medallion
                Spacer(modifier = Modifier.width(48.dp))

                // Right Options Group: Nearby & Profile (evenly spaced)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTabItem(
                        label = "Nearby",
                        selectedIcon = Icons.Filled.LocationOn,
                        unselectedIcon = Icons.Outlined.LocationOn,
                        isSelected = currentTab == EcoFeastTab.NEARBY,
                        onClick = { onTabSelected(EcoFeastTab.NEARBY) }
                    )

                    NavigationTabItem(
                        label = "Profile",
                        selectedIcon = Icons.Filled.Person,
                        unselectedIcon = Icons.Outlined.Person,
                        isSelected = currentTab == EcoFeastTab.PROFILE,
                        onClick = { onTabSelected(EcoFeastTab.PROFILE) }
                    )
                }
            }
        }

        // 2. Center Raised Decorative EcoFeast Logo Medallion (NON-CLICKABLE, DECORATIVE ONLY)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .semantics { contentDescription = "EcoFeast Decorative Emblem" },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = SoftGreen,
                border = BorderStroke(1.5.dp, PrimaryForestGreen.copy(alpha = 0.35f)),
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ecofeast_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationTabItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryForestGreen else SecondaryText,
        animationSpec = tween(durationMillis = 200),
        label = "TabIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryForestGreen else SecondaryText,
        animationSpec = tween(durationMillis = 200),
        label = "TabTextColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(2.5.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            ),
            maxLines = 1,
            softWrap = false
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.5.dp)
                    .size(width = 16.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryForestGreen)
            )
        } else {
            Spacer(modifier = Modifier.height(2.5.dp))
        }
    }
}
