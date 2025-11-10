import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --------------- FLIGHT API MODELING -------------------
// Amadeus /v2/shopping/flight-offers

@Serializable
data class FlightResponse(
    val data: List<FlightOffer>?,
    val errors: List<AmadeusError>? = null
)

@Serializable
data class FlightOffer(
    @SerialName("id") val id: String,
    @SerialName("source") val source: String? = null,
    @SerialName("price") val price: Price? = null,
    @SerialName("itineraries") val itineraries: List<Itinerary>? = null,
    @SerialName("validatingAirlineCodes") val validatingAirlineCodes: List<String>? = null,
    @SerialName("numberOfBookableSeats") val numberOfBookableSeats: Int? = null
)

@Serializable
data class Price(
    val currency: String?,
    val total: String?,
    val base: String?,
    val grandTotal: String?
)

@Serializable
data class Itinerary(
    @SerialName("duration") val duration: String? = null,
    @SerialName("segments") val segments: List<Segment>? = null
)

@Serializable
data class Segment(
    @SerialName("departure") val departure: AirportInfo? = null,
    @SerialName("arrival") val arrival: AirportInfo? = null,
    @SerialName("carrierCode") val carrierCode: String? = null,
    @SerialName("number") val number: String? = null,
    @SerialName("aircraft") val aircraft: Aircraft? = null,
    @SerialName("operating") val operating: Operating? = null,
    @SerialName("duration") val duration: String? = null
)

@Serializable
data class AirportInfo(
    @SerialName("iataCode") val iataCode: String? = null,
    @SerialName("terminal") val terminal: String? = null,
    @SerialName("at") val at: String? = null
)

@Serializable
data class Aircraft(
    @SerialName("code") val code: String? = null
)

@Serializable
data class Operating(
    @SerialName("carrierCode") val carrierCode: String? = null
)