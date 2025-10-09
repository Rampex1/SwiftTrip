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
            val response: TokenResponse = client.post("https://test.api.amadeus.com/v1/security/oauth2/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret")
            }.body()
            response.accessToken
        } catch (e: Exception) {
            Log.e("ApiService", "Token fetch error: ${e.message}", e)  // Improved logging with stack trace
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
            if (httpResponse.status.value != 200) {
                val errorBody = httpResponse.bodyAsText()  // Fixed: Read full body as string
                Log.e("ApiService", "Flight offers failed with status ${httpResponse.status}: $errorBody")
                return null
            }
            httpResponse.body()  // Deserialize only on success
        } catch (e: Exception) {
            Log.e("ApiService", "Flight offers error: ${e.message}", e)
            null
        }
    }
}