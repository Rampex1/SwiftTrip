package hk.hku.cs.swifttrip

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class ResultsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var routeText: TextView
    private lateinit var datesText: TextView
    private lateinit var passengersText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var resultsCountText: TextView
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var sortButton: MaterialButton
    private lateinit var filterButton: MaterialButton

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var currentTab = 0 // 0 = Flights, 1 = Hotels, 2 = Packages

    // Add this: Store the parsed flight and hotel responses
    private var flightResponse: FlightResponse? = null
    private var hotelResponse: HotelResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Add this: Parse the flight and hotel JSON extras early
        val flightJson = intent.getStringExtra("flightResponseJson")
        flightResponse = if (flightJson != null) {
            try {
                Gson().fromJson(flightJson, FlightResponse::class.java)
            } catch (e: Exception) {
                null  // Handle deserialization failure
            }
        } else {
            null
        }

        val hotelJson = intent.getStringExtra("hotelResponseJson")
        hotelResponse = if (hotelJson != null) {
            try {
                Gson().fromJson(hotelJson, HotelResponse::class.java)
            } catch (e: Exception) {
                null  // Handle deserialization failure
            }
        } else {
            null
        }

        initializeViews()
        setupToolbar()
        loadSearchData()
        setupTabs()
        setupButtons()
        loadFlightResults()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        routeText = findViewById(R.id.routeText)
        datesText = findViewById(R.id.datesText)
        passengersText = findViewById(R.id.passengersText)
        tabLayout = findViewById(R.id.tabLayout)
        resultsCountText = findViewById(R.id.resultsCountText)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        sortButton = findViewById(R.id.sortButton)
        filterButton = findViewById(R.id.filterButton)

        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSearchData() {
        // Get search data from intent
        val fromLocation = intent.getStringExtra("fromLocation") ?: "New York"
        val toLocation = intent.getStringExtra("toLocation") ?: "Paris"
        val departureDate = intent.getLongExtra("departureDate", 0)
        val returnDate = intent.getLongExtra("returnDate", 0)
        val passengers = intent.getStringExtra("passengers") ?: "1 Adult"

        // Display search info
        routeText.text = "$fromLocation → $toLocation"

        val departureCal = Calendar.getInstance().apply { timeInMillis = departureDate }
        val returnCal = Calendar.getInstance().apply { timeInMillis = returnDate }
        datesText.text = "${dateFormat.format(departureCal.time)} - ${dateFormat.format(returnCal.time)}"

        passengersText.text = passengers
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                when (currentTab) {
                    0 -> loadFlightResults()
                    1 -> loadHotelResults()
                    2 -> loadPackageResults()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupButtons() {
        sortButton.setOnClickListener {
            Toast.makeText(this, "Sort options coming soon", Toast.LENGTH_SHORT).show()
        }

        filterButton.setOnClickListener {
            Toast.makeText(this, "Filter options coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFlightResults() {
        val flights: List<Flight> = if (flightResponse?.data?.isNotEmpty() == true) {
            // Use real data: Map FlightOffer to Flight
            Log.d("ResultsActivity", "Using real flight data: ${flightResponse!!.data!!.size} offers")
            flightResponse!!.data!!.map { offer ->
                mapToFlight(offer)
            }
        } else {
            // Fallback to mocks if no real data
            Log.d("ResultsActivity", "Using mock flight data")
            generateMockFlights()
        }

        resultsCountText.text = "${flights.size} flights found"
        resultsRecyclerView.adapter = FlightAdapter(flights)
    }

    // Add this: Mapping function from FlightOffer to your mock Flight class
    private fun mapToFlight(offer: FlightOffer): Flight {
        val outboundItin = offer.itineraries?.getOrNull(0)  // First itinerary (outbound)
        val firstSegment = outboundItin?.segments?.getOrNull(0)  // First segment for basic info

        val airline = firstSegment?.carrierCode ?: "Unknown"  // e.g., "AA" (expand to full name if needed)
        val depTime = firstSegment?.departure?.at?.substring(11, 16) ?: "N/A"  // Extract time from ISO datetime, e.g., "10:30"
        val depAirport = firstSegment?.departure?.iataCode ?: "N/A"
        val arrTime = firstSegment?.arrival?.at?.substring(11, 16) ?: "N/A"
        val arrAirport = firstSegment?.arrival?.iataCode ?: "N/A"
        val duration = outboundItin?.segments?.size?.let { if (it > 1) "${it - 1} stop(s)" else "Non-stop" } ?: "N/A"
        val price = offer.price?.total ?: "N/A"
        val flightClass = "Economy"  // Assume default; pull from offer if available
        val availability = "Available"  // Amadeus offers are available by default; customize if needed

        return Flight(airline, depTime, depAirport, arrTime, arrAirport, duration, "", price, flightClass, availability)
    }

    // Add this: Mapping function from HotelOffer to your mock Hotel class
    private fun mapToHotel(offer: HotelOffer): Hotel {
        val hotelInfo = offer.hotel
        val firstOffer = offer.offers?.firstOrNull()
        
        val name = hotelInfo?.name ?: "Unknown Hotel"
        val rating = hotelInfo?.rating?.let { "⭐".repeat(it) } ?: "⭐⭐⭐"
        val reviews = "(4.5/5 - 150 reviews)" // Default since Amadeus doesn't provide review count
        val location = hotelInfo?.address?.let { addr ->
            "📍 ${addr.cityName ?: "Unknown City"}, ${addr.lines?.firstOrNull() ?: ""}"
        } ?: "📍 Location not available"
        val amenities = "WiFi • Breakfast • Pool" // Default amenities
        val price = firstOffer?.price?.total?.let { "$$it/night" } ?: "Price not available"
        val availability = "Available" // Default availability
        
        return Hotel(name, rating, reviews, location, amenities, price, availability)
    }

    private fun loadHotelResults() {
        val hotels: List<Hotel> = if (hotelResponse?.data?.isNotEmpty() == true) {
            // Use real data: Map HotelOffer to Hotel
            Log.d("ResultsActivity", "Using real hotel data: ${hotelResponse!!.data!!.size} offers")
            hotelResponse!!.data!!.map { offer ->
                mapToHotel(offer)
            }
        } else {
            // Fallback to mocks if no real data
            Log.d("ResultsActivity", "Using mock hotel data")
            generateMockHotels()
        }

        resultsCountText.text = "${hotels.size} hotels found"
        resultsRecyclerView.adapter = HotelAdapter(hotels)
    }

    private fun loadPackageResults() {
        resultsCountText.text = "Package deals coming soon"
        resultsRecyclerView.adapter = null
        Toast.makeText(this, "Package deals feature under development", Toast.LENGTH_SHORT).show()
    }

    private fun generateMockFlights(): List<Flight> {
        return listOf(
            Flight("Emirates", "10:30 AM", "JFK", "7:00 PM", "CDG", "8h 30m", "Non-stop", "$850", "Economy", "5 seats left"),
            Flight("Air France", "2:15 PM", "JFK", "4:45 AM +1", "CDG", "7h 30m", "Non-stop", "$920", "Economy", "12 seats left"),
            Flight("Delta", "8:45 PM", "JFK", "10:15 AM +1", "CDG", "8h 30m", "Non-stop", "$1,050", "Economy", "8 seats left"),
            Flight("United Airlines", "6:00 AM", "JFK", "8:30 PM", "CDG", "9h 30m", "1 stop", "$680", "Economy", "15 seats left"),
            Flight("British Airways", "11:20 AM", "JFK", "12:50 AM +1", "CDG", "10h 30m", "1 stop (LHR)", "$720", "Economy", "10 seats left"),
            Flight("Lufthansa", "4:30 PM", "JFK", "7:15 AM +1", "CDG", "11h 45m", "1 stop (FRA)", "$650", "Economy", "20 seats left"),
            Flight("KLM", "9:15 AM", "JFK", "11:45 PM", "CDG", "9h 30m", "1 stop (AMS)", "$695", "Economy", "18 seats left"),
            Flight("Swiss Air", "1:00 PM", "JFK", "4:30 AM +1", "CDG", "12h 30m", "1 stop (ZRH)", "$710", "Economy", "7 seats left"),
            Flight("Turkish Airlines", "5:45 PM", "JFK", "3:15 PM +1", "CDG", "16h 30m", "1 stop (IST)", "$590", "Economy", "25 seats left"),
            Flight("Iberia", "7:30 AM", "JFK", "10:00 PM", "CDG", "11h 30m", "1 stop (MAD)", "$640", "Economy", "14 seats left")
        )
    }

    private fun generateMockHotels(): List<Hotel> {
        return listOf(
            Hotel("Le Grand Hotel Paris", "⭐⭐⭐⭐⭐", "(4.8/5 - 320 reviews)", "📍 Champs-Élysées, 0.2km to Arc de Triomphe", "WiFi • Pool • Spa • Gym", "$250/night", "2 rooms left"),
            Hotel("Hotel de la Place", "⭐⭐⭐⭐", "(4.5/5 - 180 reviews)", "📍 Latin Quarter, 0.3km to Notre-Dame", "WiFi • Breakfast • Bar", "$120/night", "5 rooms left"),
            Hotel("Paris Luxury Suites", "⭐⭐⭐⭐⭐", "(4.9/5 - 450 reviews)", "📍 Opera District, 0.1km to Galeries Lafayette", "WiFi • Pool • Restaurant • Concierge", "$320/night", "1 room left"),
            Hotel("Montmartre Inn", "⭐⭐⭐", "(4.2/5 - 95 reviews)", "📍 Montmartre, 0.5km to Sacré-Cœur", "WiFi • Breakfast", "$85/night", "8 rooms left"),
            Hotel("Riverside Hotel Paris", "⭐⭐⭐⭐", "(4.6/5 - 220 reviews)", "📍 Seine Riverbank, 0.4km to Eiffel Tower", "WiFi • River View • Restaurant", "$180/night", "4 rooms left"),
            Hotel("Budget Stay Paris", "⭐⭐", "(3.8/5 - 60 reviews)", "📍 Outer District, 2km to city center", "WiFi • Kitchen", "$55/night", "12 rooms left"),
            Hotel("Boutique Hotel Marais", "⭐⭐⭐⭐", "(4.7/5 - 155 reviews)", "📍 Le Marais, 0.3km to Place des Vosges", "WiFi • Breakfast • Garden", "$145/night", "3 rooms left"),
            Hotel("Palace de Paris", "⭐⭐⭐⭐⭐", "(4.9/5 - 580 reviews)", "📍 1st Arrondissement, 0.1km to Louvre", "WiFi • Pool • Spa • Fine Dining • Butler", "$450/night", "2 rooms left"),
            Hotel("Cozy Corner Hotel", "⭐⭐⭐", "(4.0/5 - 75 reviews)", "📍 Belleville, 1.5km to attractions", "WiFi • Breakfast", "$70/night", "10 rooms left"),
            Hotel("Eiffel View Hotel", "⭐⭐⭐⭐", "(4.4/5 - 200 reviews)", "📍 Trocadéro, 0.3km to Eiffel Tower", "WiFi • Breakfast • Terrace", "$165/night", "6 rooms left")
        )
    }
}