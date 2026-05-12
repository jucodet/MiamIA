package com.miamia.home

import org.junit.Assert.assertTrue
import org.junit.Test

class HomeWelcomeBlockOrderTest {

    @Test
    fun welcomeBlockKeepsSecondPositionInReferenceOrder() {
        val second = HomeLayoutOrderBuilder().buildPortraitOrder()[1]
        assertTrue(second.sectionId == HomeSectionId.WELCOME_MESSAGE)
    }
}
