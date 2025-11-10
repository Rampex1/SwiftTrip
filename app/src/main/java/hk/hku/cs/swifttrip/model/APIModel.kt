import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ------------ COMMON API MODELING ----------------

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

@Serializable
data class AmadeusError(
    val status: Int? = null,
    val code: String? = null,
    val title: String? = null,
    val detail: String? = null
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

@Serializable
data class GeoCode(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null
)

