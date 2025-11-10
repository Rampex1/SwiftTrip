package hk.hku.cs.swifttrip
import CityResponse
import FlightResponse
import HotelListResponse
import HotelResponse
import TokenResponse
import android.util.Log
import hk.hku.cs.swifttrip.utils.CityList
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.bodyAsText

class ApiService {

    // ----------------------- TOKEN SERVICE -----------------------------------------
    val clientId: String = "X5JXchc2GfwloKA9uZbaZj8x8GgAMDZr"
    val clientSecret: String = "cJaOAOqa4ucPCYpG"

    private val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            defaultRequest {
                url("https://test.api.amadeus.com/")  // Prefixes relative paths with this (HTTPS)
            }
        }
    }


    suspend fun getAmadeusAccessToken(): String? {
        return try {
            val httpResponse = client.post("https://test.api.amadeus.com/v1/security/oauth2/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret")
                timeout {
                    requestTimeoutMillis = 10000
                }
            }

            if (httpResponse.status.value == 200) {
                val response: TokenResponse = httpResponse.body()
                response.accessToken
            } else {
                val errorBody = httpResponse.bodyAsText()
                Log.e("ApiService", "Exception: $errorBody")
                return null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception message: ${e.message}")
            null
        }
    }

    // -------------------------- FLIGHT SERVICE -------------------------------
    suspend fun getFlightOffers(auth: String, originCode: String, destCode: String, depDateStr: String, retDateStr: String, adults: Int): FlightResponse? {
        return try {
            val httpResponse = client.get("v2/shopping/flight-offers") {
                header("Authorization", auth)
                parameter("originLocationCode", originCode)
                parameter("destinationLocationCode", destCode)
                parameter("departureDate", depDateStr)
                parameter("returnDate", retDateStr)
                parameter("adults", adults)
                parameter("currencyCode", "USD")
                parameter("max", 10)
            }

            if (httpResponse.status.value != 200) {
                Log.e("ApiService", "Flight offers error")
                return null
            }

            val response = httpResponse.body<FlightResponse>()
            response
        } catch (e: Exception) {
            Log.e("ApiService", "Flight offers error: ${e.message}", e)
            null
        }
    }

    // ----------------------------- HOTEL SERVICE --------------------------------------
    suspend fun getCityCode(auth: String, keyword: String): String? {
        return try {
            val response: CityResponse = client.get("v1/reference-data/locations/cities") {
                header("Authorization", auth)
                parameter("keyword", keyword)
                parameter("max", 1)
            }.body()
            response.data?.firstOrNull()?.iataCode
        } catch (e: Exception) {
            Log.e("ApiService", "City code error for '$keyword': ${e.message}", e)
            null
        }
    }

    suspend fun getHotelOffers(auth: String, cityCode: String, checkInDate: String, checkOutDate: String, adults: Int = 1): HotelResponse? {
        try {
            // --- Call 1: Get Hotel IDs from City Code ---
            Log.d("ApiService", "Getting hotel list for city: $cityCode")

            val hotelListResponse = client.get("v1/reference-data/locations/hotels/by-city") {
                header("Authorization", auth)
                parameter("cityCode", cityCode)
                parameter("radius", 20) // Wideddn search radius for more options
                parameter("radiusUnit", "KM")
            }

            if (hotelListResponse.status.value != 200) {
                Log.e("ApiService", "Hotel List API (call 1) failed: ${hotelListResponse.status}")
                return null
            }

            val hotelListData = hotelListResponse.body<HotelListResponse>()
            // Extract the hotel IDs from the first response
            val hotelIds = hotelListData.data?.mapNotNull { it.hotelId }

            if (hotelIds.isNullOrEmpty()) {
                Log.w("ApiService", "No hotels found for city: $cityCode")
                return HotelResponse(data = emptyList()) // Return an empty, non-null response
            }

            // --- Call 2: Get Actual Hotel Offers using Hotel IDs ---

            // Amadeus API expects a comma-separated string of IDs
            // We take 10 to avoid making the request URL too long
            val hotelIdsString = hotelIds.take(10).joinToString(",")
            Log.d("ApiService", "Getting offers for ${hotelIds.size} hotels (using $hotelIdsString)...")

            val httpResponse = client.get("v3/shopping/hotel-offers") {
                header("Authorization", auth)
                parameter("hotelIds", hotelIdsString)
                parameter("currency", "USD") // v3 endpoint uses 'currency'
                parameter("bestRateOnly", "true")
            }

            if (httpResponse.status.value != 200) {
                val errorBody = httpResponse.bodyAsText()
                Log.e("ApiService", "Hotel Offers API (call 2) failed: ${httpResponse.status} - $errorBody")
                return null
            }

            // Parse the REAL response from the /hotel-offers endpoint
            // This assumes your HotelResponse data class matches the v3 endpoint's structure
            val realHotelResponse = httpResponse.body<HotelResponse>()
            Log.d("ApiService", "✅ Successfully received ${realHotelResponse.data} real hotel offers.")
            return realHotelResponse

        } catch (e: Exception) {
            Log.e("ApiService", "🚨 Hotel search error: ${e.message}", e)
            return null
        }
    }

    // ------------------------ AUTOCOMPLETE SERVICE -------------------------------------
    fun getAirportCode(keyword: String): String? {
        return try {
            val citySuggestion = CityList.availableCities.find {
                it.cityName?.equals(keyword, ignoreCase = true) == true
            }

            if (citySuggestion != null && citySuggestion.airportCode != null) {
                return citySuggestion.airportCode
            }

            Log.w("ApiService", " No airport code found in CityList for: '$keyword'")
            null

        } catch (e: Exception) {
            Log.e("ApiService", "Airport code lookup error for '$keyword': ${e.message}", e)
            null
        }
    }
}