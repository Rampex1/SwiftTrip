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