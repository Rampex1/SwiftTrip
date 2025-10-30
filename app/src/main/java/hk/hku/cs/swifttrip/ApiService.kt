package hk.hku.cs.swifttrip
import android.util.Log
import com.google.gson.Gson
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.readUTF8Line

class ApiService {
    val clientId: String = "X5JXchc2GfwloKA9uZbaZj8x8GgAMDZr"
    val clientSecret: String = "cJaOAOqa4ucPCYpG"

    // Lazy-initialized Ktor client for HTTP requests
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

    /**
     * Fetches the Amadeus access token using a POST request to the OAuth endpoint.
     * This function is marked as suspend to allow asynchronous execution within a coroutine scope.
     * It returns the access token as a String if successful, or null on failure (e.g., network error or invalid credentials).
     *
     * @return The access token string, or null if the request fails.
     */
    suspend fun getAmadeusAccessToken(): String? {
        return try {
            Log.d("ApiService", "=== STARTING TOKEN REQUEST ===")
            Log.d("ApiService", "Client ID: $clientId")
            Log.d("ApiService", "Client Secret: ${clientSecret.take(4)}****") // Only show first 4 chars for security
            Log.d("ApiService", "Request URL: https://test.api.amadeus.com/v1/security/oauth2/token")
            
            // Test basic connectivity first
            Log.d("ApiService", "Testing basic connectivity...")
            try {
                val testResponse = client.get("https://httpbin.org/get") {
                    timeout {
                        requestTimeoutMillis = 5000
                    }
                }
                Log.d("ApiService", "Basic connectivity test: ${testResponse.status}")
            } catch (e: Exception) {
                Log.e("ApiService", "Basic connectivity test failed: ${e.message}")
            }
            
            val httpResponse = client.post("https://test.api.amadeus.com/v1/security/oauth2/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret")
                timeout {
                    requestTimeoutMillis = 10000
                }
            }
            
            Log.d("ApiService", "=== TOKEN RESPONSE RECEIVED ===")
            Log.d("ApiService", "Response status: ${httpResponse.status}")
            Log.d("ApiService", "Response headers: ${httpResponse.headers}")
            
            if (httpResponse.status.value != 200) {
                val errorBody = httpResponse.bodyAsText()
                Log.e("ApiService", "=== TOKEN REQUEST FAILED ===")
                Log.e("ApiService", "Status: ${httpResponse.status}")
                Log.e("ApiService", "Error body: $errorBody")
                return null
            }
            
            val response: TokenResponse = httpResponse.body()
            Log.d("ApiService", "=== TOKEN SUCCESS ===")
            Log.d("ApiService", "Access token received: ${response.accessToken?.take(10)}...")
            response.accessToken
        } catch (e: Exception) {
            Log.e("ApiService", "=== TOKEN EXCEPTION ===")
            Log.e("ApiService", "Exception type: ${e.javaClass.simpleName}")
            Log.e("ApiService", "Exception message: ${e.message}")
            Log.e("ApiService", "Exception stack trace:", e)
            null
        }
    }

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


    suspend fun getFlightOffers(auth: String, originCode: String, destCode: String, depDateStr: String, retDateStr: String, adults: Int): FlightResponse? {
        return try {
            Log.d("ApiService", "✈️ Flight search: $originCode -> $destCode, $depDateStr to $retDateStr, $adults adults")

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

            Log.d("ApiService", "📡 Flight API response status: ${httpResponse.status}")

            // ✅ SIMPLE: Just log the raw JSON
            val rawResponse = httpResponse.bodyAsText()
            Log.d("ApiService", "📄 FULL API RESPONSE JSON:")
            // Split long responses to avoid log truncation
            rawResponse.chunked(4000).forEachIndexed { index, chunk ->
                Log.d("ApiService", "JSON Part ${index + 1}: $chunk")
            }

            if (httpResponse.status.value != 200) {
                Log.e("ApiService", "❌ Flight offers failed: $rawResponse")
                return null
            }

            val response = httpResponse.body<FlightResponse>()
            Log.d("ApiService", "✅ Parsed ${response.data?.size ?: 0} flight offers")
            response.data?.forEachIndexed { index, flight ->
                Log.d("ApiService", "┌─── Flight ${index + 1} ───")
                Log.d("ApiService", "│ ID: ${flight.id}")
                Log.d("ApiService", "│ 💰 Price: ${flight.price?.currency} ${flight.price?.total}")
                Log.d("ApiService", "│ 💺 Available Seats: ${flight.numberOfBookableSeats ?: 0}")
                Log.d("ApiService", "│ ✈️ Airline: ${flight.validatingAirlineCodes?.joinToString() ?: "Unknown"}")

                // Display complete itinerary
                flight.getCompleteItinerary().forEach { line ->
                    Log.d("ApiService", "│ $line")
                }

                Log.d("ApiService", "└──────────────────")
            }
            response
        } catch (e: Exception) {
            Log.e("ApiService", "🚨 Flight offers error: ${e.message}", e)
            null
        }
    }

    suspend fun getHotelOffers(auth: String, cityCode: String, checkInDate: String, checkOutDate: String, adults: Int = 1): HotelResponse? {
        return try {
            Log.d("ApiService", "🏨 Simple hotel search in $cityCode")

            val hotelListResponse = client.get("v1/reference-data/locations/hotels/by-city") {
                header("Authorization", auth)
                parameter("cityCode", cityCode)
                parameter("radius", 5)
                parameter("radiusUnit", "KM")
            }

            Log.d("ApiService", "📡 Hotel API response status: ${hotelListResponse.status}")

            if (hotelListResponse.status.value != 200) {
                Log.e("ApiService", "❌ Hotel API failed: ${hotelListResponse.status}")
                return null
            }

            val rawResponse = hotelListResponse.bodyAsText()
            Log.d("ApiService", "📄 Hotel API raw response: $rawResponse")

            val json = Json { ignoreUnknownKeys = true }
            val hotelListData = json.decodeFromString<HotelListResponse>(rawResponse)
            val hotels = hotelListData.data ?: emptyList()

            Log.d("ApiService", "✅ Found ${hotels.size} hotels")

            // Convert hotel list to hotel offers (without contact)
            val hotelOffers = hotels.take(10).map { hotel ->
                HotelOffer(
                    type = "hotel-offer",
                    hotel = HotelInfo(
                        hotelId = hotel.hotelId,
                        name = hotel.name,
                        rating = hotel.rating?.toIntOrNull(),
                        address = hotel.address
                        // contact is omitted
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER_${hotel.hotelId}",
                            checkInDate = checkInDate,
                            checkOutDate = checkOutDate,
                            price = HotelPrice(
                                currency = "USD",
                                total = (100 + (0..200).random()).toString(),
                                base = null,
                                taxes = null
                            ),
                            guests = HotelGuests(adults)
                        )
                    ),
                    available = true
                )
            }

            HotelResponse(data = hotelOffers)

        } catch (e: Exception) {
            Log.e("ApiService", "🚨 Hotel search error: ${e.message}", e)
            null
        }
    }

    suspend fun getAirportCode(auth: String, keyword: String): String? {
        return try {
            Log.d("ApiService", "🔍 Searching airport code for: '$keyword'")

            // First try with common airport mappings for better reliability
            val hardcodedCode = getHardcodedAirportCode(keyword)
            if (hardcodedCode != null) {
                Log.d("ApiService", "✅ Using hardcoded airport code: $hardcodedCode")
                return hardcodedCode
            }

            // If no hardcoded match, use API lookup
            val apiCode = getAirportCodeFromAPI(auth, keyword)
            if (apiCode != null) {
                Log.d("ApiService", "✅ Using API airport code: $apiCode")
                return apiCode
            }

            Log.w("ApiService", "❌ No airport code found for: '$keyword'")
            null

        } catch (e: Exception) {
            Log.e("ApiService", "🚨 Airport code error for '$keyword': ${e.message}", e)
            null
        }
    }

    /**
     * Common airport mappings for reliability
     */
    private fun getHardcodedAirportCode(keyword: String): String? {
        val airportMappings = mapOf(
            // US Cities
            "nyc" to "JFK", "new york" to "JFK", "new york city" to "JFK", "manhattan" to "JFK",
            "los angeles" to "LAX", "la" to "LAX", "lax" to "LAX",
            "chicago" to "ORD", "ord" to "ORD",
            "miami" to "MIA", "mia" to "MIA",
            "las vegas" to "LAS", "vegas" to "LAS",
            "san francisco" to "SFO", "sfo" to "SFO",
            "seattle" to "SEA", "sea" to "SEA",
            "boston" to "BOS", "bos" to "BOS",
            "washington" to "IAD", "dc" to "IAD", "washington dc" to "IAD",

            // European Cities
            "london" to "LHR", "lon" to "LHR", "lhr" to "LHR",
            "paris" to "CDG", "cdg" to "CDG",
            "frankfurt" to "FRA", "fra" to "FRA",
            "amsterdam" to "AMS", "ams" to "AMS",
            "rome" to "FCO", "fco" to "FCO",
            "madrid" to "MAD", "mad" to "MAD",
            "barcelona" to "BCN", "bcn" to "BCN",
            "munich" to "MUC", "muc" to "MUC",
            "zurich" to "ZRH", "zrh" to "ZRH",
            "dublin" to "DUB", "dub" to "DUB",

            // Asian Cities
            "tokyo" to "NRT", "nrt" to "NRT",
            "singapore" to "SIN", "sin" to "SIN",
            "hong kong" to "HKG", "hkg" to "HKG",
            "bangkok" to "BKK", "bkk" to "BKK",
            "seoul" to "ICN", "icn" to "ICN",
            "beijing" to "PEK", "pek" to "PEK",
            "shanghai" to "PVG", "pvg" to "PVG",
            "dubai" to "DXB", "dxb" to "DXB",

            // Australian Cities
            "sydney" to "SYD", "syd" to "SYD",
            "melbourne" to "MEL", "mel" to "MEL"
        )

        val normalizedKeyword = keyword.lowercase().trim()
        return airportMappings[normalizedKeyword]
    }

    private suspend fun getAirportCodeFromAPI(auth: String, keyword: String): String? {
        return try {
            Log.d("ApiService", "🌐 Calling Amadeus API for airport code: '$keyword'")

            val response: AirportResponse = client.get("v1/reference-data/locations") {
                header("Authorization", auth)
                parameter("keyword", keyword)
                parameter("subType", "AIRPORT")  // Critical: search only airports
                parameter("max", 5)  // Get more results to find the best match
            }.body()

            Log.d("ApiService", "📊 API returned ${response.data?.size ?: 0} airport results")

            // Log all results for debugging
            response.data?.forEachIndexed { index, airport ->
                Log.d("ApiService", "  Result ${index + 1}: ${airport.iataCode} - ${airport.name} (${airport.subType})")
            }

            // Strategy: Prefer AIRPORT type, then AIRPORT_CITY, then others
            val bestAirport = response.data?.find { it.subType == "AIRPORT" }
                ?: response.data?.find { it.subType == "AIRPORT_CITY" }
                ?: response.data?.firstOrNull()

            val selectedCode = bestAirport?.iataCode
            if (selectedCode != null) {
                Log.d("ApiService", "🎯 Selected airport: ${bestAirport.name} (${bestAirport.iataCode})")
            } else {
                Log.w("ApiService", "❌ No valid airport found in API response")
            }

            selectedCode

        } catch (e: Exception) {
            Log.e("ApiService", "🚨 API airport lookup failed for '$keyword': ${e.message}")
            null
        }
    }
    
    fun createMockHotelResponse(): HotelResponse {
        return HotelResponse(
            data = listOf(
                HotelOffer(
                    type = "hotel-offers",
                    hotel = HotelInfo(
                        hotelId = "MOCK001",
                        name = "Grand Hotel Paris",
                        rating = 4,
                        contact = HotelContact("+33 1 42 86 00 00", null),
                        address = HotelAddress(
                            lines = listOf("123 Champs-Élysées"),
                            cityName = "Paris",
                            countryCode = "FR",
                            postalCode = "75008"
                        )
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER001",
                            checkInDate = "2024-01-15",
                            checkOutDate = "2024-01-17",
                            room = HotelRoom("STANDARD_ROOM", HotelRoomType("STANDARD", 1, "QUEEN")),
                            guests = HotelGuests(2),
                            price = HotelPrice("USD", "250.00", "200.00", null),
                            policies = HotelPolicies("GUARANTEED", HotelCancellation("FULL_STAY", "0.00"))
                        )
                    )
                ),
                HotelOffer(
                    type = "hotel-offers",
                    hotel = HotelInfo(
                        hotelId = "MOCK002",
                        name = "Hotel de la Place",
                        rating = 3,
                        contact = HotelContact("+33 1 43 25 00 00", null),
                        address = HotelAddress(
                            lines = listOf("456 Rue de Rivoli"),
                            cityName = "Paris",
                            countryCode = "FR",
                            postalCode = "75001"
                        )
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER002",
                            checkInDate = "2024-01-15",
                            checkOutDate = "2024-01-17",
                            room = HotelRoom("STANDARD_ROOM", HotelRoomType("STANDARD", 1, "DOUBLE")),
                            guests = HotelGuests(2),
                            price = HotelPrice("USD", "180.00", "150.00", null),
                            policies = HotelPolicies("GUARANTEED", HotelCancellation("FULL_STAY", "0.00"))
                        )
                    )
                ),
                HotelOffer(
                    type = "hotel-offers",
                    hotel = HotelInfo(
                        hotelId = "MOCK003",
                        name = "Paris Luxury Suites",
                        rating = 5,
                        contact = HotelContact("+33 1 40 20 00 00", null),
                        address = HotelAddress(
                            lines = listOf("789 Opera District"),
                            cityName = "Paris",
                            countryCode = "FR",
                            postalCode = "75009"
                        )
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER003",
                            checkInDate = "2024-01-15",
                            checkOutDate = "2024-01-17",
                            room = HotelRoom("SUITE", HotelRoomType("SUITE", 1, "KING")),
                            guests = HotelGuests(2),
                            price = HotelPrice("USD", "320.00", "280.00", null),
                            policies = HotelPolicies("GUARANTEED", HotelCancellation("FULL_STAY", "0.00"))
                        )
                    )
                ),
                HotelOffer(
                    type = "hotel-offers",
                    hotel = HotelInfo(
                        hotelId = "MOCK004",
                        name = "Montmartre Inn",
                        rating = 3,
                        contact = HotelContact("+33 1 42 58 00 00", null),
                        address = HotelAddress(
                            lines = listOf("321 Montmartre"),
                            cityName = "Paris",
                            countryCode = "FR",
                            postalCode = "75018"
                        )
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER004",
                            checkInDate = "2024-01-15",
                            checkOutDate = "2024-01-17",
                            room = HotelRoom("STANDARD_ROOM", HotelRoomType("STANDARD", 1, "TWIN")),
                            guests = HotelGuests(2),
                            price = HotelPrice("USD", "85.00", "75.00", null),
                            policies = HotelPolicies("GUARANTEED", HotelCancellation("FULL_STAY", "0.00"))
                        )
                    )
                ),
                HotelOffer(
                    type = "hotel-offers",
                    hotel = HotelInfo(
                        hotelId = "MOCK005",
                        name = "Riverside Hotel Paris",
                        rating = 4,
                        contact = HotelContact("+33 1 45 78 00 00", null),
                        address = HotelAddress(
                            lines = listOf("654 Seine Riverbank"),
                            cityName = "Paris",
                            countryCode = "FR",
                            postalCode = "75007"
                        )
                    ),
                    offers = listOf(
                        HotelOfferDetail(
                            id = "OFFER005",
                            checkInDate = "2024-01-15",
                            checkOutDate = "2024-01-17",
                            room = HotelRoom("RIVER_VIEW", HotelRoomType("DELUXE", 1, "QUEEN")),
                            guests = HotelGuests(2),
                            price = HotelPrice("USD", "180.00", "160.00", null),
                            policies = HotelPolicies("GUARANTEED", HotelCancellation("FULL_STAY", "0.00"))
                        )
                    )
                )
            )
        )
    }
    suspend fun getLocationSuggestions(auth: String, keyword: String): LocationResponse? {
        return try {
            Log.d("ApiService", "Searching city suggestions for: '$keyword'")
            val response: LocationResponse = client.get("v1/reference-data/locations") {
                header("Authorization", auth)
                parameter("subType", "CITY")
                parameter("keyword", keyword)
                parameter("page[limit]", 10)  // Limit to 10 suggestions
            }.body()
            Log.d("ApiResponse", "Full data: ${Gson().toJson(response)}")
            Log.d("ApiService", "📊 API returned ${response.data?.size ?: 0} city suggestions")
            response
        } catch (e: Exception) {
            Log.e("ApiService", "City suggestions error for '$keyword': ${e.message}", e)
            null
        }
    }
}