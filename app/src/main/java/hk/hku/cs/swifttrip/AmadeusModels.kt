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
data class Aircraft(@SerialName("code") val code: String? = null)

@Serializable
data class Operating(@SerialName("carrierCode") val carrierCode: String? = null)

// Hotel Request
@Serializable
data class HotelSearchRequest(
    val cityCode: String,
    val checkInDate: String,
    val checkOutDate: String,
    val roomQuantity: Int = 1,
    val adults: Int = 1,
    val currencyCode: String = "USD"
)

// Hotel Response
@Serializable
data class HotelResponse(
    val data: List<HotelOffer>?,
    val errors: List<AmadeusError>? = null
)

@Serializable
data class HotelOffer(
    val type: String?,
    val hotel: HotelInfo?,
    val offers: List<HotelOfferDetail>?
)

@Serializable
data class HotelInfo(
    val hotelId: String?,
    val name: String?,
    val rating: Int?,
    val contact: HotelContact?,
    val address: HotelAddress?
)

@Serializable
data class HotelContact(
    val phone: String?,
    val fax: String?
)

@Serializable
data class HotelAddress(
    val lines: List<String>?,
    val cityName: String?,
    val countryCode: String?,
    val postalCode: String?
)

@Serializable
data class HotelOfferDetail(
    val id: String?,
    val checkInDate: String?,
    val checkOutDate: String?,
    val room: HotelRoom?,
    val guests: HotelGuests?,
    val price: HotelPrice?,
    val policies: HotelPolicies?
)

@Serializable
data class HotelRoom(
    val type: String?,
    val typeEstimated: HotelRoomType?
)

@Serializable
data class HotelRoomType(
    val category: String?,
    val beds: Int?,
    val bedType: String?
)

@Serializable
data class HotelGuests(
    val adults: Int?
)

@Serializable
data class HotelPrice(
    val currency: String?,
    val total: String?,
    val base: String?,
    val taxes: List<HotelTax>?
)

@Serializable
data class HotelTax(
    val code: String?,
    val amount: String?
)

@Serializable
data class HotelPolicies(
    val paymentType: String?,
    val cancellation: HotelCancellation?
)

@Serializable
data class HotelCancellation(
    val type: String?,
    val amount: String?
)

// Airport Data Models
@Serializable
data class AirportResponse(
    @SerialName("data") val data: List<AirportData>? = null,
    @SerialName("meta") val meta: Meta? = null
)

@Serializable
data class AirportData(
    @SerialName("type") val type: String? = null,
    @SerialName("subType") val subType: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("detailedName") val detailedName: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("self") val self: AirportSelf? = null,
    @SerialName("timeZoneOffset") val timeZoneOffset: String? = null,
    @SerialName("iataCode") val iataCode: String? = null,
    @SerialName("geoCode") val geoCode: GeoCode? = null,
    @SerialName("address") val address: AirportAddress? = null,
    @SerialName("analytics") val analytics: Analytics? = null
)

@Serializable
data class AirportSelf(
    @SerialName("href") val href: String? = null,
    @SerialName("methods") val methods: List<String>? = null
)

@Serializable
data class GeoCode(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null
)

@Serializable
data class AirportAddress(
    @SerialName("cityName") val cityName: String? = null,
    @SerialName("cityCode") val cityCode: String? = null,
    @SerialName("countryName") val countryName: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("regionCode") val regionCode: String? = null
)

@Serializable
data class Analytics(
    @SerialName("travelers") val travelers: Travelers? = null
)

@Serializable
data class Travelers(
    @SerialName("score") val score: Int? = null
)

@Serializable
data class Meta(
    @SerialName("count") val count: Int? = null,
    @SerialName("links") val links: MetaLinks? = null
)

@Serializable
data class MetaLinks(
    @SerialName("self") val self: String? = null
)