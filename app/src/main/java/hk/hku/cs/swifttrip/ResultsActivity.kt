package hk.hku.cs.swifttrip

import android.content.Intent
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
    private lateinit var visaButton: MaterialButton

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
                Log.e("ResultsActivity", "Error parsing flight JSON: ${e.message}")
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
                Log.e("ResultsActivity", "Error parsing hotel JSON: ${e.message}")
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
        setupVisaButton()
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
        visaButton = findViewById(R.id.visaButton)

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
    private fun setupVisaButton(){
        visaButton.setOnClickListener {
            val fromLocation = intent.getStringExtra("fromLocation") ?: "New York"
            val toLocation = intent.getStringExtra("toLocation") ?: "Paris"
            val intent = Intent(this, VisaActivity::class.java)
            intent.putExtra("fromLocation", fromLocation)
            intent.putExtra("toLocation", toLocation)

            startActivity(intent) }
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
        val flights: List<FlightOffer> = if (flightResponse?.data?.isNotEmpty() == true) {
            // Use real FlightOffer data directly
            Log.d("ResultsActivity", "Using real flight data: ${flightResponse!!.data!!.size} offers")

            // Log detailed flight information
            flightResponse!!.data!!.forEachIndexed { index, flight ->
                Log.d("ResultsActivity", "┌─── REAL FLIGHT ${index + 1} ───")
                Log.d("ResultsActivity", "│ ${flight.formatFlightForDisplay()}")
                Log.d("ResultsActivity", "└──────────────────")
            }

            flightResponse!!.data!!
        } else {
            return
        }

        resultsCountText.text = "${flights.size} flights found"
        resultsRecyclerView.adapter = FlightOfferAdapter(flights) { flight ->
            showFlightDetails(flight)
        }
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

    // Add this function to show flight details
    private fun showFlightDetails(flight: FlightOffer) {
        val details = flight.getCompleteItinerary()
        val detailText = details.joinToString("\n")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Flight Details")
            .setMessage(detailText)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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


    private fun generateMockHotels(): List<Hotel> {
        return listOf(
            Hotel("Le Grand Hotel Paris", "⭐⭐⭐⭐⭐", "(4.8/5 - 320 reviews)", "📍 Champs-Élysées, 0.2km to Arc de Triomphe", "WiFi • Pool • Spa • Gym", "$250/night", "2 rooms left"),
            Hotel("Hotel de la Place", "⭐⭐⭐⭐", "(4.5/5 - 180 reviews)", "📍 Latin Quarter, 0.3km to Notre-Dame", "WiFi • Breakfast • Bar", "$120/night", "5 rooms left"),
            Hotel("Paris Luxury Suites", "⭐⭐⭐⭐⭐", "(4.9/5 - 450 reviews)", "📍 Opera District, 0.1km to Galeries Lafayette", "WiFi • Pool • Restaurant • Concierge", "$320/night", "1 room left"),
            Hotel("Montmartre Inn", "⭐⭐⭐", "(4.2/5 - 95 reviews)", "📍 Montmartre, 0.5km to Sacré-Cœur", "WiFi • Breakfast", "$85/night", "8 rooms left"),
            Hotel("Riverside Hotel Paris", "⭐⭐⭐⭐", "(4.6/5 - 220 reviews)", "📍 Seine Riverbank, 0.4km to Eiffel Tower", "WiFi • River View • Restaurant", "$180/night", "4 rooms left")
        )
    }
}