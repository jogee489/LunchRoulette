package com.thejiltedalchemist.lunchroulette

import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantNameTest {

    @Test
    fun trimsSurroundingWhitespace() {
        assertEquals("Pizza", normalizeRestaurantName("  Pizza \t"))
    }

    @Test
    fun collapsesInnerWhitespaceRuns() {
        assertEquals("Pizza Hut", normalizeRestaurantName("Pizza   Hut"))
        assertEquals("Pizza Hut", normalizeRestaurantName("Pizza\t\nHut"))
    }

    @Test
    fun preservesCaseForDisplay() {
        assertEquals("McDonald's", normalizeRestaurantName("McDonald's"))
    }

    @Test
    fun blankInputBecomesEmpty() {
        assertEquals("", normalizeRestaurantName("   \t "))
        assertEquals("", normalizeRestaurantName(""))
    }
}
