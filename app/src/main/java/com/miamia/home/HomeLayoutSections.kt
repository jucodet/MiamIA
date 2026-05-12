package com.miamia.home

enum class HomeSectionId {
    STATUS_INDICATOR,
    WELCOME_MESSAGE,
    PHOTO_PREVIEW,
    CAPTURE_BUTTON
}

data class HomeLayoutSection(
    val sectionId: HomeSectionId,
    val orderIndex: Int,
    val isVisible: Boolean = true
)

object HomeLayoutSections {
    val portraitReferenceOrder: List<HomeLayoutSection> = listOf(
        HomeLayoutSection(HomeSectionId.STATUS_INDICATOR, 1),
        HomeLayoutSection(HomeSectionId.WELCOME_MESSAGE, 2),
        HomeLayoutSection(HomeSectionId.PHOTO_PREVIEW, 3),
        HomeLayoutSection(HomeSectionId.CAPTURE_BUTTON, 4)
    )
}
