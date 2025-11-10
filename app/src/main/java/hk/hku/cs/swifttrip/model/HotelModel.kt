import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ------------------- HOTEL API MODELING (HOTEL LIST) ----------------------
// Amadeus /v1/reference-data/locations/hotels/by-city

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
    @SerialName("dupeId") val dupeId: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("cityCode") val cityCode: String? = null,
    @SerialName("geoCode") val geoCode: HotelGeoCode? = null,
    @SerialName("address") val address: HotelAddress? = null,
    @SerialName("contact") val contact: HotelContact? = null, // Re-used in v3
    @SerialName("distance") val distance: HotelDistance? = null,
    @SerialName("lastUpdate") val lastUpdate: String? = null
)

@Serializable
data class HotelAddress(
    @SerialName("lines") val lines: List<String>? = null,
    @SerialName("cityName") val cityName: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("postalCode") val postalCode: String? = null
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
data class HotelListMeta(
    @SerialName("count") val count: Int? = null,
    @SerialName("links") val links: HotelListLinks? = null
)

@Serializable
data class HotelListLinks(
    @SerialName("self") val self: String? = null
)

// ------------------ HOTEL API MODELING (HOTEL OFFER) -------------
// For Amadeus /v3/shopping/hotel-offers

@Serializable
data class HotelResponse(
    @SerialName("data") val data: List<HotelOffer>? = null
)

@Serializable
data class HotelOffer(
    @SerialName("type") val type: String? = null,
    @SerialName("hotel") val hotel: HotelInfo? = null,
    @SerialName("available") val available: Boolean? = null,
    @SerialName("offers") val offers: List<HotelOfferDetail>? = null,
    @SerialName("self") val self: String? = null
)

@Serializable
data class HotelInfo(
    @SerialName("type") val type: String? = null,
    @SerialName("hotelId") val hotelId: String? = null,
    @SerialName("chainCode") val chainCode: String? = null,
    @SerialName("dupeId") val dupeId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("cityCode") val cityCode: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("contact") val contact: HotelContact? = null // Re-used from v1
)

@Serializable
data class HotelContact(
    @SerialName("phone") val phone: String? = null,
    @SerialName("fax") val fax: String? = null
)

@Serializable
data class HotelOfferDetail(
    @SerialName("id") val id: String? = null,
    @SerialName("checkInDate") val checkInDate: String? = null,
    @SerialName("checkOutDate") val checkOutDate: String? = null,
    @SerialName("rateCode") val rateCode: String? = null,
    @SerialName("boardType") val boardType: String? = null,
    @SerialName("room") val room: HotelRoom? = null,
    @SerialName("guests") val guests: HotelGuests? = null,
    @SerialName("price") val price: HotelPrice? = null,
    @SerialName("policies") val policies: HotelPolicies? = null,
    @SerialName("description") val description: TextDescription? = null,
    @SerialName("roomInformation") val roomInformation: RoomInformation? = null,
    @SerialName("self") val self: String? = null
)

@Serializable
data class HotelRoom(
    @SerialName("type") val type: String? = null,
    @SerialName("description") val description: TextDescription? = null
)

@Serializable
data class RoomInformation(
    @SerialName("description") val description: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("name") val name: TextDescription? = null
)

@Serializable
data class TextDescription(
    @SerialName("text") val text: String? = null,
    @SerialName("lang") val lang: String? = null
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
    @SerialName("taxes") val taxes: List<HotelTax>? = null,
    @SerialName("variations") val variations: HotelPriceVariations? = null
)

@Serializable
data class HotelTax(
    @SerialName("amount") val amount: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("included") val included: Boolean? = false
)

@Serializable
data class HotelPriceVariations(
    @SerialName("average") val average: PriceVariation? = null,
    @SerialName("changes") val changes: List<PriceChange>? = null
)

@Serializable
data class PriceVariation(
    @SerialName("total") val total: String? = null,
    @SerialName("base") val base: String? = null
)

@Serializable
data class PriceChange(
    @SerialName("startDate") val startDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("total") val total: String? = null,
    @SerialName("base") val base: String? = null
)

@Serializable
data class HotelPolicies(
    @SerialName("refundable") val refundable: RefundablePolicy? = null
)

@Serializable
data class RefundablePolicy(
    @SerialName("cancellationRefund") val cancellationRefund: String? = null
)

// -------------------- LOCATION & AIRPORT API MODELING -------------
// Amadeus /v1/reference-data/locations/cities and Airport lookup

@Serializable
data class CityResponse(
    val data: List<CityData>?
)

@Serializable
data class CityData(
    val iataCode: String?
)

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
    @SerialName("geoCode") val geoCode: GeoCode? = null, // Re-uses common GeoCode
    @SerialName("address") val address: AirportAddress? = null,
    @SerialName("analytics") val analytics: Analytics? = null
)

@Serializable
data class AirportSelf(
    @SerialName("href") val href: String? = null,
    @SerialName("methods") val methods: List<String>? = null
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
