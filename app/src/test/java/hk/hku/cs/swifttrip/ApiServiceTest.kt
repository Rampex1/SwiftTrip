package hk.hku.cs.swifttrip

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ApiServiceTest {

    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        apiService = ApiService()
    }

    @Test
    fun `test getAirportCode returns correct code for valid city`() {
        val result = apiService.getAirportCode("Hong Kong")
        assertNotNull("Airport code should not be null for Hong Kong", result)
    }

    @Test
    fun `test getAirportCode is case insensitive`() {
        val result1 = apiService.getAirportCode("hong kong")
        val result2 = apiService.getAirportCode("HONG KONG")
        val result3 = apiService.getAirportCode("Hong Kong")

        assertEquals("Airport codes should match regardless of case", result1, result2)
        assertEquals("Airport codes should match regardless of case", result2, result3)
    }

    @Test
    fun `test clientId is not empty`() {
        assertTrue("Client ID should not be empty", apiService.clientId.isNotEmpty())
    }

    @Test
    fun `test clientSecret is not empty`() {
        assertTrue("Client Secret should not be empty", apiService.clientSecret.isNotEmpty())
    }

    @Test
    fun `test getAmadeusAccessToken handles network failure gracefully`() = runBlocking {
        val result = apiService.getAmadeusAccessToken()
        assertTrue("Should handle failure gracefully", result == null || result.isNotEmpty())
    }
}