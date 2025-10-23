package hk.hku.cs.swifttrip

import java.text.SimpleDateFormat
import java.util.*

// Filter criteria data class
data class FlightFilterCriteria(
    var allowNonstop: Boolean = true,
    var allow1Stop: Boolean = true,
    var allow2PlusStops: Boolean = true,
    var minPrice: Float = 0f,
    var maxPrice: Float = 2000f,
    var allowMorning: Boolean = true,    // 6-12
    var allowAfternoon: Boolean = true,  // 12-18
    var allowEvening: Boolean = true,    // 18-24
    var allowNight: Boolean = true       // 0-6
)

// Sort options enum
enum class FlightSortOption {
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    DURATION_SHORTEST,
    DEPARTURE_MORNING,
    ARRIVAL_MORNING
}

enum class HotelSortOption {
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    RATING_HIGH_TO_LOW,
    NAME_A_TO_Z
}

// Flight utilities
object FlightUtils {

    fun filterFlights(flights: List<FlightOffer>, criteria: FlightFilterCriteria): List<FlightOffer> {
        return flights.filter { flight ->
            // Filter by stops
            val stops = flight.getNumberOfStops()
            val stopsMatch = when (stops) {
                0 -> criteria.allowNonstop
                1 -> criteria.allow1Stop
                else -> criteria.allow2PlusStops
            }

            // Filter by price
            val price = flight.price?.total?.toFloatOrNull() ?: 0f
            val priceMatch = price in criteria.minPrice..criteria.maxPrice

            // Filter by departure time
            val departureTime = flight.getDepartureTime()
            val timeMatch = if (departureTime != null) {
                val hour = departureTime.get(Calendar.HOUR_OF_DAY)
                when {
                    hour in 6..11 -> criteria.allowMorning
                    hour in 12..17 -> criteria.allowAfternoon
                    hour in 18..23 -> criteria.allowEvening
                    else -> criteria.allowNight
                }
            } else {
                true // If we can't determine time, don't filter it out
            }

            stopsMatch && priceMatch && timeMatch
        }
    }

    fun sortFlights(flights: List<FlightOffer>, sortOption: FlightSortOption): List<FlightOffer> {
        return when (sortOption) {
            FlightSortOption.PRICE_LOW_TO_HIGH -> {
                flights.sortedBy { it.price?.total?.toFloatOrNull() ?: Float.MAX_VALUE }
            }
            FlightSortOption.PRICE_HIGH_TO_LOW -> {
                flights.sortedByDescending { it.price?.total?.toFloatOrNull() ?: 0f }
            }
            FlightSortOption.DURATION_SHORTEST -> {
                flights.sortedBy { it.getTotalDurationMinutes() }
            }
            FlightSortOption.DEPARTURE_MORNING -> {
                flights.sortedBy {
                    val time = it.getDepartureTime()
                    if (time != null) {
                        val hour = time.get(Calendar.HOUR_OF_DAY)
                        if (hour >= 6) hour else hour + 24 // Put morning flights first
                    } else {
                        Int.MAX_VALUE
                    }
                }
            }
            FlightSortOption.ARRIVAL_MORNING -> {
                flights.sortedBy {
                    val time = it.getArrivalTime()
                    if (time != null) {
                        val hour = time.get(Calendar.HOUR_OF_DAY)
                        if (hour >= 6) hour else hour + 24 // Put morning arrivals first
                    } else {
                        Int.MAX_VALUE
                    }
                }
            }
        }
    }
}

// Hotel utilities
object HotelUtils {

    fun sortHotels(hotels: List<Hotel>, sortOption: HotelSortOption): List<Hotel> {
        return when (sortOption) {
            HotelSortOption.PRICE_LOW_TO_HIGH -> {
                hotels.sortedBy { extractPrice(it.price) }
            }
            HotelSortOption.PRICE_HIGH_TO_LOW -> {
                hotels.sortedByDescending { extractPrice(it.price) }
            }
            HotelSortOption.RATING_HIGH_TO_LOW -> {
                hotels.sortedByDescending { extractRating(it.rating) }
            }
            HotelSortOption.NAME_A_TO_Z -> {
                hotels.sortedBy { it.name }
            }
        }
    }

    private fun extractPrice(priceString: String): Float {
        // Extract numeric value from strings like "$120/night" or "Price not available"
        val regex = """\$?(\d+\.?\d*)""".toRegex()
        val match = regex.find(priceString)
        return match?.groupValues?.get(1)?.toFloatOrNull() ?: Float.MAX_VALUE
    }

    private fun extractRating(ratingString: String): Int {
        // Count the number of star emojis
        return ratingString.count { it == '⭐' }
    }
}

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

private fun parseDurationToMinutes(duration: String): Int {
    // Parse ISO 8601 duration format: PT2H30M
    var totalMinutes = 0

    val hoursRegex = """(\d+)H""".toRegex()
    val minutesRegex = """(\d+)M""".toRegex()

    hoursRegex.find(duration)?.groupValues?.get(1)?.toIntOrNull()?.let {
        totalMinutes += it * 60
    }

    minutesRegex.find(duration)?.groupValues?.get(1)?.toIntOrNull()?.let {
        totalMinutes += it
    }

    return totalMinutes
}

private fun parseIsoDateTime(isoString: String): Calendar? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(isoString)
        if (date != null) {
            Calendar.getInstance().apply { time = date }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}