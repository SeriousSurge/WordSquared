package com.hiremarknolan.wsq.models

import kotlin.test.Test
import kotlin.test.assertEquals

class WordSquareBorderTest {
    @Test
    fun testToMap() {
        val border = WordSquareBorder("WORD", "DOWN", "TOWN", "WANT")
        val map = border.toMap()
        assertEquals(4, map.size)
        assertEquals("WORD", map["top"])
        assertEquals("DOWN", map["right"])
        assertEquals("TOWN", map["bottom"])
        assertEquals("WANT", map["left"])
    }
} 