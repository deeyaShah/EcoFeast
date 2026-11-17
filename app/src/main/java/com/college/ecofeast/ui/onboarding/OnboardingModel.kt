package com.college.ecofeast.ui.onboarding

enum class OnboardingPageType {
    SAVE_FOOD,
    FIND_NEARBY,
    CONNECT_RESCUE
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val pageType: OnboardingPageType
)

val onboardingPages = listOf(
    OnboardingPageData(
        title = "Save Good Food",
        description = "Give surplus food a second chance by sharing it with people in your local community.",
        pageType = OnboardingPageType.SAVE_FOOD
    ),
    OnboardingPageData(
        title = "Find Food Near You",
        description = "Discover available surplus food from people and local businesses around you.",
        pageType = OnboardingPageType.FIND_NEARBY
    ),
    OnboardingPageData(
        title = "Claim. Connect. Rescue.",
        description = "Claim available food, chat with the donor, and coordinate a simple self-pickup.",
        pageType = OnboardingPageType.CONNECT_RESCUE
    )
)
