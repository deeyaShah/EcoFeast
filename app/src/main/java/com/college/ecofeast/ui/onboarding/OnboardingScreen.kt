package com.college.ecofeast.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SageGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit = {}
) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    // Automatic page advancement timer (3 seconds per page, direct switch, stopping on the last page)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < onboardingPages.size - 1) {
            delay(3000)
            pagerState.scrollToPage(pagerState.currentPage + 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(onboardingPages.size - 1)
                            }
                        }
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Pager Pages with smooth transition transformation
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                            alpha = 1f - pageOffset.coerceIn(0f, 1f) * 0.4f
                            scaleX = 1f - pageOffset.coerceIn(0f, 1f) * 0.08f
                            scaleY = 1f - pageOffset.coerceIn(0f, 1f) * 0.08f
                        }
                ) {
                    OnboardingPageItem(page = onboardingPages[pageIndex])
                }
            }

            // Bottom Navigation Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                PageIndicator(
                    pagerState = pagerState,
                    pageCount = onboardingPages.size
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Primary Button
                PrimaryButton(
                    text = if (isLastPage) "Get Started" else "Next",
                    onClick = {
                        if (isLastPage) {
                            onOnboardingFinished()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingPageItem(
    page: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Upper/Middle Illustration Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .aspectRatio(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            OnboardingIllustration(
                pageType = page.pageType,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                letterSpacing = 0.2.sp,
                fontSize = 26.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = SecondaryText,
                lineHeight = 22.sp,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun PageIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 10.dp,
                animationSpec = tween(300),
                label = "indicatorWidth"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(10.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) PrimaryForestGreen else SageGreen.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
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
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
