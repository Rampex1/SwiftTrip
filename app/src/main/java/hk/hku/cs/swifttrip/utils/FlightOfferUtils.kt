package hk.hku.cs.swifttrip.utils

import FlightOffer
import Itinerary
import java.util.Calendar

// Extension functions for FlightOffer
fun FlightOffer.getNumberOfStops(): Int {
    val firstSegment = itineraries?.firstOrNull()?.segments
    return if (firstSegment != null) {
        (firstSegment.size - 1).coerceAtLeast(0)
    } else {
        0
    }
}

fun FlightOffer.getTotalDurationMinutes(): Int {
    val duration = itineraries?.firstOrNull()?.duration
    return if (duration != null) {
        parseDurationToMinutes(duration)
    } else {
        Int.MAX_VALUE
    }
}

fun FlightOffer.getDepartureTime(): Calendar? {
    val departureAt = itineraries?.firstOrNull()?.segments?.firstOrNull()?.departure?.at
    return if (departureAt != null) {
        parseIsoDateTime(departureAt)
    } else {
        null
    }
}

fun FlightOffer.getArrivalTime(): Calendar? {
    val arrivalAt = itineraries?.firstOrNull()?.segments?.lastOrNull()?.arrival?.at
    return if (arrivalAt != null) {
        parseIsoDateTime(arrivalAt)
    } else {
        null
    }
}
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
