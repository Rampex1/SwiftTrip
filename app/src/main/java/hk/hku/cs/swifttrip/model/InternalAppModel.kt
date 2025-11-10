import java.util.Calendar

// --------- INTERNAL APP MODELING -----------

data class SearchCriteria(
    val origin: String,
    val destination: String,
    val departureDate: Calendar?,
    val returnDate: Calendar?,
    val passengers: String,
    val timestamp: Long
)

data class Hotel(
    val name: String,
    val location: String,
    val price: String,
    val availability: String
)