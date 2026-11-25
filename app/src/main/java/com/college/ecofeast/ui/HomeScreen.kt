package com.college.ecofeast.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.auth.WelcomeFoodIllustration
import com.college.ecofeast.ui.onboarding.ClaimConnectIllustration
import com.college.ecofeast.ui.onboarding.FindFoodIllustration
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.Terracotta
import androidx.lifecycle.viewmodel.compose.viewModel
import com.college.ecofeast.ui.home.HomeViewModel
import com.college.ecofeast.ui.home.HomeMapPreview
import com.college.ecofeast.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Data class representing a slide in the EcoFeast Hero Carousel.
 */
data class HeroCarouselSlide(
    val id: Int,
    val title: String,
    val description: String,
    val tagMessage: String,
    val illustrationType: Int
)

private val heroSlides = listOf(
    HeroCarouselSlide(
        id = 1,
        title = "Good food deserves another chance",
        description = "Share surplus food or discover food near you.",
        tagMessage = "Together, we reduce food waste.",
        illustrationType = 1
    ),
    HeroCarouselSlide(
        id = 2,
        title = "Share More. Waste Less.",
        description = "Turn extra food into a meaningful contribution.",
        tagMessage = "Every meal counts.",
        illustrationType = 2
    ),
    HeroCarouselSlide(
        id = 3,
        title = "Good Food, Closer to You",
        description = "Discover surplus food available in your community.",
        tagMessage = "Rescue food near you.",
        illustrationType = 3
    ),
    HeroCarouselSlide(
        id = 4,
        title = "Every Share Makes a Difference",
        description = "Connect with your community and help good food find a home.",
        tagMessage = "Small actions. Big impact.",
        illustrationType = 4
    )
)

/**
 * Modern, polished Home Screen for EcoFeast with responsive layout and animated entrance.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isGuest: Boolean = false,
    userName: String = "",
    onNavigateToDonate: () -> Unit = {},
    onNavigateToNearby: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMyClaims: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    var showGuestDialog by remember { mutableStateOf(false) }
    var guestActionText by remember { mutableStateOf("") }

    // Smooth Entrance Animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "HomeAlpha"
    )

    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "HomeOffsetY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = animatedAlpha
                    translationY = animatedOffsetY.toPx()
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // A. TOP HEADER (Without Profile Icon to prevent duplication)
            HomeHeader()

            Spacer(modifier = Modifier.height(14.dp))

            // B. PERSONALIZED GREETING
            GreetingSection(
                isGuest = isGuest,
                userName = userName
            )

            Spacer(modifier = Modifier.height(16.dp))

            // C. HERO CAROUSEL (4 Slides, Auto-scroll & Swiping, Centered Indicators)
            HomeHeroCarousel()

            Spacer(modifier = Modifier.height(18.dp))

            // D. PRIMARY ACTION CARDS (Equal height & width)
            PrimaryActionCards(
                onDonateClick = onNavigateToDonate,
                onNearbyClick = onNavigateToNearby
            )

            Spacer(modifier = Modifier.height(22.dp))

            // E. YOUR ACTIVITY SHORTCUTS
            YourActivitySection(
                isGuest = isGuest,
                onDonationsClick = {
                    if (isGuest) {
                        guestActionText = "donations history"
                        showGuestDialog = true
                    } else {
                        onNavigateToDonate()
                    }
                },
                onClaimsClick = {
                    if (isGuest) {
                        guestActionText = "claims history"
                        showGuestDialog = true
                    } else {
                        onNavigateToMyClaims()
                    }
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // F. AVAILABLE NEAR YOU
            val homeUiState by homeViewModel.uiState.collectAsState()

            HomeMapPreview(
                listings = homeUiState.availableListings,
                isLocationAvailable = true,
                locationErrorMessage = null,
                onPreviewClick = onNavigateToNearby,
                onEnableLocationClick = onNavigateToNearby
            )

            Spacer(modifier = Modifier.height(18.dp))

            // G. SMALL IMPACT MESSAGE
            EcoImpactBanner()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Sign In Prompt Dialog for Guests
    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = {
                Text(
                    text = "Sign In Required",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                )
            },
            text = {
                Text(
                    text = "You need to sign in with your phone number to track $guestActionText.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        lineHeight = 19.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGuestDialog = false
                        onSignInClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryForestGreen,
                        contentColor = WarmIvory
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Sign In Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGuestDialog = false }
                ) {
                    Text(text = "Cancel", color = SecondaryText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Clean Top App Header displaying EcoFeast Logo, Title, and Tagline (No profile button).
 * Formatted cleanly so tagline never clips with ellipsis.
 */
@Composable
fun HomeHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ecofeast_logo),
            contentDescription = "EcoFeast Logo",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "EcoFeast",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryForestGreen,
                    fontSize = 21.sp,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = "Good Food • Less Waste • Happier Communities",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SecondaryText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp
                ),
                maxLines = 2,
                softWrap = true
            )
        }
    }
}

/**
 * Greeting Section displaying personalized user greeting and soft subtitle.
 */
@Composable
fun GreetingSection(
    isGuest: Boolean,
    userName: String,
    modifier: Modifier = Modifier
) {
    val displayName = remember(isGuest, userName) {
        if (isGuest || userName.isBlank()) "Food Saver" else userName
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Hello, $displayName 🌿",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepText,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Every meal shared creates a better tomorrow.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 13.5.sp
            )
        )
    }
}

/**
 * Landscape Hero Carousel displaying 4 distinct promotional slides with auto-advance every 4s,
 * smooth horizontal sliding transitions, manual swipe support, and centered page indicators.
 */
@Composable
fun HomeHeroCarousel(
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { heroSlides.size })

    // Automatic slide advance every 4 seconds with smooth FastOutSlowInEasing transition.
    // Resets cleanly when user interacts manually.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collectLatest {
                delay(4000L)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.settledPage + 1) % heroSlides.size
                    runCatching {
                        pagerState.animateScrollToPage(
                            page = nextPage,
                            animationSpec = tween(
                                durationMillis = 750,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
            }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
            ) { page ->
                val slide = heroSlides[page]
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Text Content (~60% width)
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryForestGreen,
                                fontSize = 16.5.sp,
                                lineHeight = 20.sp
                            ),
                            softWrap = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DeepText.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            softWrap = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = slide.tagMessage,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Terracotta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            softWrap = true
                        )
                    }

                    // Right Illustration (~40% width)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (slide.illustrationType) {
                            1 -> HeroBannerSharingIllustration(modifier = Modifier.fillMaxSize())
                            2 -> WelcomeFoodIllustration(modifier = Modifier.fillMaxSize())
                            3 -> FindFoodIllustration(modifier = Modifier.fillMaxSize())
                            else -> ClaimConnectIllustration(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centered Page Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(heroSlides.size) { index ->
                    val activePage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                    val isSelected = activePage == index

                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 18.dp else 6.dp,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "IndicatorWidth"
                    )

                    val indicatorColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryForestGreen else SageGreen.copy(alpha = 0.4f),
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "IndicatorColor"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(indicatorWidth)
                            .clip(RoundedCornerShape(3.dp))
                            .background(indicatorColor)
                    )
                }
            }
        }
    }
}

/**
 * Primary Action Cards ("Donate Surplus Food" & "Find Food Nearby").
 * Uses IntrinsicSize.Max to guarantee both cards have EXACTLY EQUAL HEIGHT and EQUAL WIDTH.
 */
@Composable
fun PrimaryActionCards(
    onDonateClick: () -> Unit,
    onNearbyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Donate Surplus Food (Forest Green)
        ActionCardItem(
            title = "Donate\nSurplus Food",
            description = "Share extra food with people in need.",
            backgroundColor = PrimaryForestGreen,
            iconVector = Icons.Filled.Favorite,
            onClick = onDonateClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        // Card 2: Find Food Nearby (Terracotta)
        ActionCardItem(
            title = "Find Food\nNearby",
            description = "Discover available food around you.",
            backgroundColor = Terracotta,
            iconVector = Icons.Filled.LocationOn,
            onClick = onNearbyClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    description: String,
    backgroundColor: Color,
    iconVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "ActionCardScale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = WarmIvory,
                    modifier = Modifier.size(26.dp)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WarmIvory.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow",
                        tint = WarmIvory,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory,
                        fontSize = 15.5.sp,
                        lineHeight = 19.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WarmIvory.copy(alpha = 0.88f),
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

/**
 * Compact "Your Activity" section with My Donations & My Claims shortcut cards.
 */
@Composable
fun YourActivitySection(
    isGuest: Boolean,
    onDonationsClick: () -> Unit,
    onClaimsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Activity",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    fontSize = 17.5.sp
                )
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDonationsClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    )
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityShortcutCard(
                title = "My Donations",
                subtitle = if (isGuest) "Sign in to track" else "Food you've shared",
                iconVector = Icons.Filled.Inventory2,
                onClick = onDonationsClick,
                modifier = Modifier.weight(1f)
            )

            ActivityShortcutCard(
                title = "My Claims",
                subtitle = if (isGuest) "Sign in to track" else "Food you've claimed",
                iconVector = Icons.Filled.Favorite,
                onClick = onClaimsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActivityShortcutCard(
    title: String,
    subtitle: String,
    iconVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color(0xFFC8D6B9).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon circle on left, Chevron arrow on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        imageVector = iconVector,
                        contentDescription = title,
                        tint = PrimaryForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = SecondaryText.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Text Column: Takes full width of the card
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 14.5.sp
                    ),
                    maxLines = 1,
                    softWrap = true
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



/**
 * Compact Eco Impact Banner at the bottom of Home screen.
 */
@Composable
fun EcoImpactBanner(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
        border = BorderStroke(1.dp, SageGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌱",
                fontSize = 15.sp
            )

            Text(
                text = "Small actions. Big impact. ♡",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryForestGreen,
                    fontSize = 12.5.sp
                )
            )

            Text(
                text = "🌿",
                fontSize = 15.sp
            )
        }
    }
}
