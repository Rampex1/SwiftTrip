package hk.hku.cs.swifttrip

import java.util.Calendar

data class SearchCriteria(
    val origin: String,
    val destination: String,
    val departureDate: Calendar?,
    val returnDate: Calendar?,
    val passengers: String,
    val timestamp: Long
)

data class Flight(
    val airline: String,
    val departureTime: String,
    val departureAirport: String,
    val arrivalTime: String,
    val arrivalAirport: String,
    val duration: String,
    val stops: String,
    val price: String,
    val flightClass: String,
    val seatsLeft: String
)

data class Hotel(
    val name: String,
    val rating: String,
    val reviews: String,
    val location: String,
    val amenities: String,
    val price: String,
    val availability: String
)

data class CitySuggestion(
    val cityName: String?
)