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
    val searchCriteria: FlightSearchOptions = FlightSearchOptions(50)
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
    val date: String
)
@Serializable
data class Traveler(
    val id: String,
    val travelerType: String
)
@Serializable
data class FlightSearchOptions(
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
data class MetaLinks(
    @SerialName("self") val self: String? = null
)

@Serializable
data class HotelResponse(
    @SerialName("data") val data: List<HotelOffer>? = null
)

@Serializable
data class HotelOffer(
    @SerialName("type") val type: String? = null,
    @SerialName("hotel") val hotel: HotelInfo? = null,
    @SerialName("offers") val offers: List<HotelOfferDetail>? = null,
    @SerialName("available") val available: Boolean? = null
)

@Serializable
data class HotelInfo(
    @SerialName("hotelId") val hotelId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("contact") val contact: HotelContact? = null,  // Add this
    @SerialName("address") val address: HotelAddress? = null
)

@Serializable
data class HotelContact(
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null
)

@Serializable
data class HotelAddress(
    @SerialName("lines") val lines: List<String>? = null,
    @SerialName("cityName") val cityName: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("postalCode") val postalCode: String? = null
)

@Serializable
data class HotelOfferDetail(
    @SerialName("id") val id: String? = null,
    @SerialName("checkInDate") val checkInDate: String? = null,
    @SerialName("checkOutDate") val checkOutDate: String? = null,
    @SerialName("room") val room: HotelRoom? = null,
    @SerialName("guests") val guests: HotelGuests? = null,
    @SerialName("price") val price: HotelPrice? = null,
    @SerialName("policies") val policies: HotelPolicies? = null
)

@Serializable
data class HotelRoom(
    @SerialName("type") val type: String? = null,
    @SerialName("typeEstimated") val typeEstimated: HotelRoomType? = null
)

@Serializable
data class HotelRoomType(
    @SerialName("category") val category: String? = null,
    @SerialName("beds") val beds: Int? = null,
    @SerialName("bedType") val bedType: String? = null
)

@Serializable
data class HotelGuests(
    @SerialName("adults") val adults: Int? = null
)

@Serializable
data class HotelPrice(
    @SerialName("currency") val currency: String? = null,
    @SerialName("total") val total: String? = null,
    @SerialName("base") val base: String? = null,
    @SerialName("taxes") val taxes: List<HotelTax>? = null
)

@Serializable
data class HotelTax(
    @SerialName("amount") val amount: String? = null,
    @SerialName("code") val code: String? = null
)

@Serializable
data class HotelPolicies(
    @SerialName("paymentType") val paymentType: String? = null,
    @SerialName("cancellation") val cancellation: HotelCancellation? = null
)

@Serializable
data class HotelCancellation(
    @SerialName("type") val type: String? = null,
    @SerialName("amount") val amount: String? = null
)

@Serializable
data class HotelListResponse(
    @SerialName("data") val data: List<HotelListItem>? = null,
    @SerialName("meta") val meta: HotelListMeta? = null
)

@Serializable
data class HotelListItem(
    @SerialName("type") val type: String? = null,
    @SerialName("hotelId") val hotelId: String? = null,
    @SerialName("chainCode") val chainCode: String? = null,
    @SerialName("iataCode") val iataCode: String? = null,
    @SerialName("dupeId") val dupeId: Long? = null,  // Change from String to Long
    @SerialName("name") val name: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("cityCode") val cityCode: String? = null,
    @SerialName("geoCode") val geoCode: HotelGeoCode? = null,
    @SerialName("address") val address: HotelAddress? = null,
    @SerialName("contact") val contact: HotelContact? = null,
    @SerialName("distance") val distance: HotelDistance? = null,
    @SerialName("lastUpdate") val lastUpdate: String? = null
)

@Serializable
data class HotelListMeta(
    @SerialName("count") val count: Int? = null,
    @SerialName("links") val links: HotelListLinks? = null
)

@Serializable
data class HotelListLinks(
    @SerialName("self") val self: String? = null
)

@Serializable
data class HotelGeoCode(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null
)

@Serializable
data class HotelDistance(
    @SerialName("value") val value: Double? = null,
    @SerialName("unit") val unit: String? = null
)

@Serializable
data class Meta(
    @SerialName("count") val count: Int? = null,
    @SerialName("links") val links: Links? = null
)

@Serializable
data class Links(
    @SerialName("self") val self: String? = null
)