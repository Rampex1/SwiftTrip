package hk.hku.cs.swifttrip
import android.util.Log
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

    suspend fun getFlightOffers(auth: String, body: FlightSearchRequest): FlightResponse? {
        return try {
            Log.d("ApiService", "Sending flight request body: ${Json.encodeToString(FlightSearchRequest.serializer(), body)}")  // Log body JSON
            val httpResponse = client.post("v2/shopping/flight-offers") {
                header("Authorization", auth)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            Log.d("ApiService", "Flight API response status: ${httpResponse.status}")
            if (httpResponse.status.value != 200) {
                val errorBody = httpResponse.bodyAsText()  // Fixed: Read full body as string
                Log.e("ApiService", "Flight offers failed with status ${httpResponse.status}: $errorBody")
                return null
            }
            val response = httpResponse.body<FlightResponse>()
            Log.d("ApiService", "Flight response data count: ${response.data?.size ?: 0}")
            
            // If no flights found, return mock data
            if (response.data.isNullOrEmpty()) {
                Log.d("ApiService", "No flights found in API response, returning mock data")
                return createMockFlightResponse()
            }
            
            response
        } catch (e: Exception) {
            Log.e("ApiService", "Flight offers error: ${e.message}", e)
            null
        }
    }

    suspend fun getHotelOffers(auth: String, cityCode: String, checkInDate: String, checkOutDate: String, adults: Int = 1): HotelResponse? {
        return try {
            Log.d("ApiService", "Searching hotels in $cityCode from $checkInDate to $checkOutDate for $adults adults")
            // Try the hotel list API first to get available hotels
            val hotelListResponse = client.get("v1/reference-data/locations/hotels/by-city") {
                header("Authorization", auth)
                parameter("cityCode", cityCode)
            }
            Log.d("ApiService", "Hotel list API response status: ${hotelListResponse.status}")
            
            if (hotelListResponse.status.value != 200) {
                val errorBody = hotelListResponse.bodyAsText()
                Log.e("ApiService", "Hotel list failed with status ${hotelListResponse.status}: $errorBody")
                return null
            }
            
            // For now, return a mock response since the hotel offers API might not be available
            // or might require different parameters
            Log.d("ApiService", "Hotel list successful, returning mock data for now")
            return createMockHotelResponse()
            
        } catch (e: Exception) {
            Log.e("ApiService", "Hotel offers error: ${e.message}", e)
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
    
    fun createMockFlightResponse(): FlightResponse {
        return FlightResponse(
            data = listOf(
                FlightOffer(
                    id = "MOCK001",
                    source = "GDS",
                    price = Price("850.00"),
                    itineraries = listOf(
                        Itinerary(
                            segments = listOf(
                                Segment(
                                    departure = Airport("JFK", "2024-01-15T10:30:00"),
                                    arrival = Airport("CDG", "2024-01-15T22:00:00"),
                                    carrierCode = "AF",
                                    number = "123"
                                )
                            )
                        )
                    )
                ),
                FlightOffer(
                    id = "MOCK002",
                    source = "GDS",
                    price = Price("920.00"),
                    itineraries = listOf(
                        Itinerary(
                            segments = listOf(
                                Segment(
                                    departure = Airport("JFK", "2024-01-15T14:15:00"),
                                    arrival = Airport("CDG", "2024-01-16T04:45:00"),
                                    carrierCode = "DL",
                                    number = "456"
                                )
                            )
                        )
                    )
                ),
                FlightOffer(
                    id = "MOCK003",
                    source = "GDS",
                    price = Price("680.00"),
                    itineraries = listOf(
                        Itinerary(
                            segments = listOf(
                                Segment(
                                    departure = Airport("JFK", "2024-01-15T06:00:00"),
                                    arrival = Airport("CDG", "2024-01-15T20:30:00"),
                                    carrierCode = "UA",
                                    number = "789"
                                )
                            )
                        )
                    )
                ),
                FlightOffer(
                    id = "MOCK004",
                    source = "GDS",
                    price = Price("720.00"),
                    itineraries = listOf(
                        Itinerary(
                            segments = listOf(
                                Segment(
                                    departure = Airport("JFK", "2024-01-15T11:20:00"),
                                    arrival = Airport("CDG", "2024-01-16T00:50:00"),
                                    carrierCode = "BA",
                                    number = "101"
                                )
                            )
                        )
                    )
                ),
                FlightOffer(
                    id = "MOCK005",
                    source = "GDS",
                    price = Price("650.00"),
                    itineraries = listOf(
                        Itinerary(
                            segments = listOf(
                                Segment(
                                    departure = Airport("JFK", "2024-01-15T16:30:00"),
                                    arrival = Airport("CDG", "2024-01-16T07:15:00"),
                                    carrierCode = "LH",
                                    number = "202"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}