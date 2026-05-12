package com.miamia.home

class HomeLayoutOrderBuilder(
    private val sections: List<HomeLayoutSection> = HomeLayoutSections.portraitReferenceOrder
) {
    fun buildPortraitOrder(): List<HomeLayoutSection> = sections.sortedBy { it.orderIndex }

    fun isStableWhenPreviewUnavailable(): Boolean {
        val ids = buildPortraitOrder().map { it.sectionId }
        return ids == listOf(
            HomeSectionId.STATUS_INDICATOR,
            HomeSectionId.WELCOME_MESSAGE,
            HomeSectionId.PHOTO_PREVIEW,
            HomeSectionId.CAPTURE_BUTTON
        )
    }
}
