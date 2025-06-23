package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.domain.usecase.WordValidationDomainService
import com.hiremarknolan.wsq.network.WordSquareApiClient
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class WordValidationDomainServiceTest {
    // Fake in-memory Settings for testing
    private class FakeSettings : Settings {
        private val map = mutableMapOf<String, Any>()
        override fun getBoolean(key: String, defaultValue: Boolean) = defaultValue
        override fun getBooleanOrNull(key: String): Boolean? {
            TODO("Not yet implemented")
        }

        override fun putBoolean(key: String, value: Boolean) { map[key] = value }
        override fun getString(key: String, defaultValue: String) = defaultValue
        override fun getStringOrNull(key: String): String? {
            TODO("Not yet implemented")
        }

        override fun hasKey(key: String): Boolean {
            TODO("Not yet implemented")
        }

        override fun putString(key: String, value: String) { map[key] = value }
        override fun getLong(key: String, defaultValue: Long) = defaultValue
        override fun getLongOrNull(key: String): Long? {
            TODO("Not yet implemented")
        }

        override fun putLong(key: String, value: Long) { map[key] = value }
        override fun getInt(key: String, defaultValue: Int) = defaultValue
        override fun getIntOrNull(key: String): Int? {
            TODO("Not yet implemented")
        }

        override fun putInt(key: String, value: Int) { map[key] = value }
        override fun getDouble(key: String, defaultValue: Double) = defaultValue
        override fun getDoubleOrNull(key: String): Double? {
            TODO("Not yet implemented")
        }

        override fun putDouble(key: String, value: Double) { map[key] = value }
        override fun getFloat(key: String, defaultValue: Float) = defaultValue
        override fun getFloatOrNull(key: String): Float? {
            TODO("Not yet implemented")
        }

        override fun putFloat(key: String, value: Float) { map[key] = value }
        override fun remove(key: String) { map.remove(key) }
        override val keys: Set<String>
            get() = TODO("Not yet implemented")
        override val size: Int
            get() = TODO("Not yet implemented")

        override fun clear() { map.clear() }
    }
    private val apiClient = WordSquareApiClient(FakeSettings())
    private val service = WordValidationDomainService(apiClient)

    @Test
    fun testValidateWordSquareAllValidOffline() = runTest {
        // 'coal' is in embedded word list
        val border = WordSquareBorder("coal", "coal", "coal", "coal")
        val result = service.validateWordSquare(border)
        assertTrue(result.isValid)
        assertTrue(result.invalidWords.isEmpty())
        assertFalse(result.hasNetworkError)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun testValidateSingleWordValidAndInvalid() = runTest {
        // Valid word offline
        val valid = service.validateSingleWord("coal")
        assertTrue(valid.isValid)
        assertEquals(null, valid.errorMessage)

        // Invalid word offline and online
        val invalidWord = "zzzz"
        val invalid = service.validateSingleWord(invalidWord)
        assertFalse(invalid.isValid)
        assertEquals("'$invalidWord' is not a valid word", invalid.errorMessage)
    }
} 