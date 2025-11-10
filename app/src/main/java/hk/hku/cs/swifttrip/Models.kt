package hk.hku.cs.swifttrip

import java.util.Calendar

// Search Info
data class SearchCriteria(
    val origin: String,
    val destination: String,
    val departureDate: Calendar?,
    val returnDate: Calendar?,
    val passengers: String,
    val timestamp: Long
)

// Hotel Info
data class Hotel(
    val name: String,
    val rating: String,
    val reviews: String,
    val location: String,
    val amenities: String,
    val price: String,
    val availability: String
)

// City suggestion for Autocomplete
data class CitySuggestion(
    val cityName: String?
)
