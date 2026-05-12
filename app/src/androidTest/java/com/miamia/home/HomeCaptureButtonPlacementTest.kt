package com.miamia.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeCaptureButtonPlacementTest {

    @Test
    fun captureButtonComesJustAfterPhotoPreview() {
        val order = HomeLayoutOrderBuilder().buildPortraitOrder().map { it.sectionId }
        val previewIndex = order.indexOf(HomeSectionId.PHOTO_PREVIEW)
        val buttonIndex = order.indexOf(HomeSectionId.CAPTURE_BUTTON)
        assertEquals(previewIndex + 1, buttonIndex)
    }
}
