package com.miamia.welcome

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WelcomeMessageSelectorTest {

    @Test
    fun select_returnsNull_whenCatalogIsEmpty() {
        val selector = WelcomeMessageSelector(Random(42))
        assertNull(selector.select(emptyList()))
    }

    @Test
    fun select_returnsMessageFromList_whenCatalogHasValues() {
        val selector = WelcomeMessageSelector(Random(0))
        val messages = listOf(
            WelcomeMessage(id = "a", text = "A", toneTags = listOf("positif", "chaleureux")),
            WelcomeMessage(id = "b", text = "B", toneTags = listOf("positif", "chaleureux"))
        )
        val selected = selector.select(messages)
        assertEquals(true, selected in messages)
    }
}
