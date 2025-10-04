package hk.hku.cs.swifttrip

import java.util.*

// Search data model
data class SearchData(
    val fromLocation: String,
    val toLocation: String,
    val departureDate: Calendar?,
    val returnDate: Calendar?,
    val passengers: String,
    val timestamp: Long
)

// Flight data model
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

// Hotel data model
data class Hotel(
    val name: String,
    val rating: String,
    val reviews: String,
    val location: String,
    val amenities: String,
    val price: String,
    val availability: String
)