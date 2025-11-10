package hk.hku.cs.swifttrip.utils

import FlightOffer
import Hotel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FlightFilterCriteria(
    var allowNonstop: Boolean = true,
    var allow1Stop: Boolean = true,
    var allow2PlusStops: Boolean = true,
    var minPrice: Float = 0f,
    var maxPrice: Float = 2000f,
    var allowMorning: Boolean = true,
    var allowAfternoon: Boolean = true,
    var allowEvening: Boolean = true,
    var allowNight: Boolean = true
)

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

object FlightUtils {

    fun filterFlights(flights: List<FlightOffer>, criteria: FlightFilterCriteria): List<FlightOffer> {
        return flights.filter { flight ->
            matchesStopCriteria(flight, criteria) &&
            matchesPriceCriteria(flight, criteria) &&
            matchesTimeCriteria(flight, criteria)
        }
    }

    private fun matchesStopCriteria(flight: FlightOffer, criteria: FlightFilterCriteria): Boolean {
        val stops = flight.getNumberOfStops()
        return when (stops) {
            0 -> criteria.allowNonstop
            1 -> criteria.allow1Stop
            else -> criteria.allow2PlusStops
        }
    }

    private fun matchesPriceCriteria(flight: FlightOffer, criteria: FlightFilterCriteria): Boolean {
        val price = flight.price?.total?.toFloatOrNull() ?: 0f
        return price in criteria.minPrice..criteria.maxPrice
    }

    private fun matchesTimeCriteria(flight: FlightOffer, criteria: FlightFilterCriteria): Boolean {
        val departureTime = flight.getDepartureTime() ?: return true
        val hour = departureTime.get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..11 -> criteria.allowMorning
            hour in 12..17 -> criteria.allowAfternoon
            hour in 18..23 -> criteria.allowEvening
            else -> criteria.allowNight
        }
    }

    fun removeDuplicateFlights(flights: List<FlightOffer>): List<FlightOffer> {
        val seenFlightNumbers = mutableSetOf<String>()
        val uniqueFlights = mutableListOf<FlightOffer>()

        for (flight in flights) {
            val flightNumbers = extractFlightNumbers(flight) ?: continue
            if (seenFlightNumbers.add(flightNumbers)) {
                uniqueFlights.add(flight)
            }
        }

        return uniqueFlights
    }

    private fun extractFlightNumbers(flight: FlightOffer): String? {
        return flight.itineraries
            ?.firstOrNull()
            ?.segments
            ?.joinToString(",") { "${it.carrierCode ?: ""}${it.number ?: ""}" }
            ?.takeIf { it.isNotBlank() }
    }

    private fun getSortKeyForPrice(flight: FlightOffer): Float {
        return flight.price?.total?.toFloatOrNull() ?: Float.MAX_VALUE
    }

    private fun getSortKeyForDepartureTime(flight: FlightOffer): Int {
        val time = flight.getDepartureTime() ?: return Int.MAX_VALUE
        val hour = time.get(Calendar.HOUR_OF_DAY)
        return if (hour >= 6) hour else hour + 24
    }

    private fun getSortKeyForArrivalTime(flight: FlightOffer): Int {
        val time = flight.getArrivalTime() ?: return Int.MAX_VALUE
        val hour = time.get(Calendar.HOUR_OF_DAY)
        return if (hour >= 6) hour else hour + 24
    }

    fun sortFlights(flights: List<FlightOffer>, sortOption: FlightSortOption): List<FlightOffer> {
        return when (sortOption) {
            FlightSortOption.PRICE_LOW_TO_HIGH -> flights.sortedBy { getSortKeyForPrice(it) }
            FlightSortOption.PRICE_HIGH_TO_LOW -> flights.sortedByDescending { getSortKeyForPrice(it) }
            FlightSortOption.DURATION_SHORTEST -> flights.sortedBy { it.getTotalDurationMinutes() }
            FlightSortOption.DEPARTURE_MORNING -> flights.sortedBy { getSortKeyForDepartureTime(it) }
            FlightSortOption.ARRIVAL_MORNING -> flights.sortedBy { getSortKeyForArrivalTime(it) }
        }
    }
}


object HotelUtils {
    fun sortHotels(hotels: List<Hotel>, sortOption: HotelSortOption): List<Hotel> {
        return when (sortOption) {
            HotelSortOption.PRICE_LOW_TO_HIGH -> hotels.sortedBy { parsePrice(it.price) }
            HotelSortOption.PRICE_HIGH_TO_LOW -> hotels.sortedByDescending { parsePrice(it.price) }
            HotelSortOption.NAME_A_TO_Z -> hotels.sortedBy { it.name }
            HotelSortOption.RATING_HIGH_TO_LOW -> TODO()
        }
    }

    private fun parsePrice(priceString: String): Float {
        val regex = """\$?(\d+\.?\d*)""".toRegex()
        return regex.find(priceString)?.groupValues?.get(1)?.toFloatOrNull() ?: Float.MAX_VALUE
    }
}

fun parseDurationToMinutes(duration: String): Int {
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

fun parseIsoDateTime(isoString: String): Calendar? {
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
