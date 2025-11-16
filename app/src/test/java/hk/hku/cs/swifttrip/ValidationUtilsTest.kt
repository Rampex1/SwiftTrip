package hk.hku.cs.swifttrip.utils

import org.junit.Test
import org.junit.Assert.*
import java.util.*

class ValidationUtilsTest {

    @Test
    fun `test validatePassengerCount with valid count`() {
        assertTrue(validatePassengerCount(1, 0))
        assertTrue(validatePassengerCount(5, 3))
        assertTrue(validatePassengerCount(9, 0))
        assertTrue(validatePassengerCount(1, 8))
    }

    @Test
    fun `test validatePassengerCount with invalid count`() {
        assertFalse(validatePassengerCount(10, 0))
        assertFalse(validatePassengerCount(5, 5))
        assertFalse(validatePassengerCount(9, 1))
    }

    @Test
    fun `test validatePassengerCount with zero adults`() {
        assertFalse(validatePassengerCount(0, 1))
        assertFalse(validatePassengerCount(0, 0))
    }

    @Test
    fun `test validatePassengerCount with negative values`() {
        assertFalse(validatePassengerCount(-1, 0))
        assertFalse(validatePassengerCount(1, -1))
    }

    @Test
    fun `test validateDates with valid dates`() {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }

        assertTrue(validateDates(today, tomorrow))
        assertTrue(validateDates(today, nextWeek))
        assertTrue(validateDates(tomorrow, nextWeek))
    }

    @Test
    fun `test validateDates with return before departure`() {
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val today = Calendar.getInstance()

        assertFalse(validateDates(tomorrow, today))
    }

    @Test
    fun `test validateDates with same date`() {
        val today = Calendar.getInstance()
        val sameDay = Calendar.getInstance()

        // Same day should be valid (can return same day)
        assertTrue(validateDates(today, sameDay))
    }

    @Test
    fun `test validateDates with null dates`() {
        val today = Calendar.getInstance()

        assertFalse(validateDates(null, today))
        assertFalse(validateDates(today, null))
        assertFalse(validateDates(null, null))
    }

    @Test
    fun `test validateLocation with valid inputs`() {
        assertTrue(validateLocation("Hong Kong"))
        assertTrue(validateLocation("New York"))
        assertTrue(validateLocation("Tokyo"))
    }

    @Test
    fun `test validateLocation with empty or blank inputs`() {
        assertFalse(validateLocation(""))
        assertFalse(validateLocation("   "))
        assertFalse(validateLocation("\t\n"))
    }

    @Test
    fun `test validateDifferentLocations with different cities`() {
        assertTrue(validateDifferentLocations("Hong Kong", "Tokyo"))
        assertTrue(validateDifferentLocations("New York", "London"))
    }

    @Test
    fun `test validateDifferentLocations with same cities`() {
        assertFalse(validateDifferentLocations("Hong Kong", "Hong Kong"))
        assertFalse(validateDifferentLocations("hong kong", "Hong Kong"))
    }

    @Test
    fun `test validateDifferentLocations is case insensitive`() {
        assertFalse(validateDifferentLocations("HONG KONG", "hong kong"))
        assertFalse(validateDifferentLocations("New York", "new york"))
    }

    // Helper functions (create these in a ValidationUtils.kt file)
    private fun validatePassengerCount(adults: Int, children: Int): Boolean {
        if (adults < 1) return false
        if (children < 0) return false
        return (adults + children) <= 9
    }

    private fun validateDates(departureDate: Calendar?, returnDate: Calendar?): Boolean {
        if (departureDate == null || returnDate == null) return false
        return !returnDate.before(departureDate)
    }

    private fun validateLocation(location: String): Boolean {
        return location.trim().isNotEmpty()
    }

    private fun validateDifferentLocations(origin: String, destination: String): Boolean {
        return !origin.equals(destination, ignoreCase = true)
    }
}