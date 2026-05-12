package com.miamia.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutOrderAcceptanceTest {

    @Test
    fun portraitOrderMatchesSpecification() {
        val order = HomeLayoutOrderBuilder().buildPortraitOrder().map { it.sectionId }
        assertEquals(
            listOf(
                HomeSectionId.STATUS_INDICATOR,
                HomeSectionId.WELCOME_MESSAGE,
                HomeSectionId.PHOTO_PREVIEW,
                HomeSectionId.CAPTURE_BUTTON
            ),
            order
        )
    }
}
