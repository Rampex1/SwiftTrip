package hk.hku.cs.swifttrip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Token (already have)
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

// City Response (for resolving codes)
@Serializable
data class CityResponse(
    val data: List<CityData>?
)
@Serializable
data class CityData(
    val iataCode: String?
)

// Flight Request
@Serializable
data class FlightSearchRequest(
    val currencyCode: String = "USD",
    val originDestinations: List<OriginDestination>,
    val travelers: List<Traveler>,
    val sources: List<String>,
    val searchCriteria: SearchCriteria = SearchCriteria(50)
)
@Serializable
data class OriginDestination(
    val id: String,
    val originLocationCode: String,
    val destinationLocationCode: String,
    val departureDateTimeRange: DateTimeRange
)
@Serializable
data class DateTimeRange(
    val date: String  // "YYYY-MM-DD"
)
@Serializable
data class Traveler(
    val id: String,
    val travelerType: String  // "ADULT" or "CHILD"
)
@Serializable
data class SearchCriteria(
    val maxFlightOffers: Int
)

// Flight Response (simplified; expand as needed)
@Serializable
data class FlightResponse(
    val data: List<FlightOffer>?,
    val errors: List<AmadeusError>? = null
)

@Serializable
data class AmadeusError(
    val status: Int? = null,
    val code: String? = null,
    val title: String? = null,
    val detail: String? = null
)

@Serializable
data class FlightOffer(
    val id: String?,
    val source: String?,
    val price: Price?,
    val itineraries: List<Itinerary>?
)
@Serializable
data class Price(val total: String?)
@Serializable
data class Itinerary(val segments: List<Segment>?)
@Serializable
data class Segment(
    val departure: Airport?,
    val arrival: Airport?,
    val carrierCode: String?,
    val number: String?
)
@Serializable
data class Airport(val iataCode: String?, val at: String?)  // "at" is datetime