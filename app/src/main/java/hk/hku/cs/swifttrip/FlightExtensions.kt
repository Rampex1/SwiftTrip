package hk.hku.cs.swifttrip

// Extension functions for FlightOffer
fun FlightOffer.getOutboundRoute(): String {
    val itinerary = this.itineraries?.getOrNull(0) ?: return ""
    return getRouteSummary(itinerary)
}

fun FlightOffer.getReturnRoute(): String {
    val itinerary = this.itineraries?.getOrNull(1) ?: return ""
    return getRouteSummary(itinerary)
}

fun FlightOffer.getCompleteItinerary(): List<String> {
    val result = mutableListOf<String>()

    this.itineraries?.forEachIndexed { index, itinerary ->
        val direction = if (index == 0) "OUTBOUND" else "RETURN"
        result.add("=== $direction ===")
        result.addAll(getDetailedItinerary(itinerary))
        result.add("") // Empty line for spacing
    }

    return result
}

// Private helper functions
private fun getRouteSummary(itinerary: Itinerary): String {
    val segments = itinerary.segments ?: return ""
    val firstSegment = segments.firstOrNull() ?: return ""
    val lastSegment = segments.lastOrNull() ?: return ""

    val origin = firstSegment.departure?.iataCode ?: ""
    val destination = lastSegment.arrival?.iataCode ?: ""
    val stops = segments.size - 1

    val stopText = when (stops) {
        0 -> "Non-stop"
        1 -> "1 stop"
        else -> "$stops stops"
    }

    val duration = itinerary.duration ?: ""
    return "$origin → $destination • $stopText • $duration"
}

private fun getDetailedItinerary(itinerary: Itinerary): List<String> {
    val details = mutableListOf<String>()
    val segments = itinerary.segments ?: return details

    segments.forEachIndexed { index, segment ->
        val segmentNumber = index + 1
        val departure = segment.departure
        val arrival = segment.arrival

        val departureTime = departure?.at?.substring(11, 16) ?: ""
        val arrivalTime = arrival?.at?.substring(11, 16) ?: ""
        val departureDate = departure?.at?.substring(0, 10) ?: ""

        details.add("Segment $segmentNumber:")
        details.add("  ✈️ ${segment.carrierCode} ${segment.number}")
        details.add("  🛫 ${departure?.iataCode} (Terminal ${departure?.terminal ?: "-"})")
        details.add("     $departureDate $departureTime")
        details.add("  🛬 ${arrival?.iataCode} (Terminal ${arrival?.terminal ?: "-"})")
        details.add("     $arrivalTime • ${segment.duration}")
        details.add("  📍 ${segment.operating?.carrierCode ?: segment.carrierCode} • ${segment.aircraft?.code ?: "Unknown Aircraft"}")

        if (index < segments.size - 1) {
            details.add("  🔄 Connection")
        }
    }

    return details
}

// UI formatting functions
fun FlightOffer.formatFlightForDisplay(): String {
    val sb = StringBuilder()

    sb.append("💰 ${this.price?.currency} ${this.price?.total}\n")
    sb.append("✈️ ${this.validatingAirlineCodes?.joinToString() ?: "Multiple Airlines"}\n")
    sb.append("🛫 ${this.getOutboundRoute()}\n")
    sb.append("🛬 ${this.getReturnRoute()}\n")
    sb.append("💺 ${this.numberOfBookableSeats ?: 0} seats available")

    return sb.toString()
}

fun getFlightDetails(flight: FlightOffer): List<String> {
    val details = mutableListOf<String>()

    details.add("Flight Details")
    details.add("Price: ${flight.price?.currency} ${flight.price?.total}")
    details.add("Base Fare: ${flight.price?.base}")
    details.add("Total: ${flight.price?.grandTotal}")
    details.add("Airline: ${flight.validatingAirlineCodes?.joinToString() ?: "Multiple"}")
    details.add("Available Seats: ${flight.numberOfBookableSeats ?: 0}")
    details.add("")

    // Add complete itinerary
    details.addAll(flight.getCompleteItinerary())

    return details
}
