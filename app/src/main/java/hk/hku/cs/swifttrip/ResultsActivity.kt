package hk.hku.cs.swifttrip

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
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

    private var currentTab = 0 // 0 = Flights, 1 = Hotels

    // Store the parsed responses
    private var flightResponse: FlightResponse? = null
    private var hotelResponse: HotelResponse? = null

    // Store original data
    private var originalFlights: List<FlightOffer> = emptyList()
    private var originalHotels: List<Hotel> = emptyList()

    // Store filtered/sorted data
    private var displayedFlights: List<FlightOffer> = emptyList()
    private var displayedHotels: List<Hotel> = emptyList()

    // Filter and sort state
    private var flightFilterCriteria = FlightFilterCriteria()
    private var currentFlightSort: FlightSortOption? = null
    private var currentHotelSort: HotelSortOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Parse the flight and hotel JSON extras
        val flightJson = intent.getStringExtra("flightResponseJson")
        flightResponse = if (flightJson != null) {
            try {
                Gson().fromJson(flightJson, FlightResponse::class.java)
            } catch (e: Exception) {
                Log.e("ResultsActivity", "Error parsing flight JSON: ${e.message}")
                null
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
                null
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
        val fromLocation = intent.getStringExtra("fromLocation") ?: "New York"
        val toLocation = intent.getStringExtra("toLocation") ?: "Paris"
        val departureDate = intent.getLongExtra("departureDate", 0)
        val returnDate = intent.getLongExtra("returnDate", 0)
        val passengers = intent.getStringExtra("passengers") ?: "1 Adult"

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
            when (currentTab) {
                0 -> showFlightSortDialog()
                1 -> showHotelSortDialog()
                else -> Toast.makeText(this, "Sort not available for this tab", Toast.LENGTH_SHORT).show()
            }
        }

        filterButton.setOnClickListener {
            when (currentTab) {
                0 -> showFlightFilterDialog()
                1 -> Toast.makeText(this, "Hotel filters coming soon", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Filter not available for this tab", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFlightResults() {
        originalFlights = if (flightResponse?.data?.isNotEmpty() == true) {
            Log.d("ResultsActivity", "Using real flight data: ${flightResponse!!.data!!.size} offers")
            flightResponse!!.data!!
        } else {
            emptyList()
        }

        // Apply current filters and sort
        displayedFlights = originalFlights
        displayedFlights = FlightUtils.filterFlights(displayedFlights, flightFilterCriteria)
        currentFlightSort?.let {
            displayedFlights = FlightUtils.sortFlights(displayedFlights, it)
        }

        updateFlightDisplay()
    }

    private fun updateFlightDisplay() {
        resultsCountText.text = "${displayedFlights.size} flights found"
        resultsRecyclerView.adapter = FlightOfferAdapter(displayedFlights) { flight ->
            showFlightDetails(flight)
        }
    }

    private fun loadHotelResults() {
        originalHotels = if (hotelResponse?.data?.isNotEmpty() == true) {
            Log.d("ResultsActivity", "Using real hotel data: ${hotelResponse!!.data!!.size} offers")
            hotelResponse!!.data!!.map { offer -> mapToHotel(offer) }
        } else {
            Log.d("ResultsActivity", "Using mock hotel data")
            generateMockHotels()
        }

        // Apply current sort
        displayedHotels = originalHotels
        currentHotelSort?.let {
            displayedHotels = HotelUtils.sortHotels(displayedHotels, it)
        }

        updateHotelDisplay()
    }

    private fun updateHotelDisplay() {
        resultsCountText.text = "${displayedHotels.size} hotels found"
        resultsRecyclerView.adapter = HotelAdapter(displayedHotels)
    }

    private fun loadPackageResults() {
        resultsCountText.text = "Package deals coming soon"
        resultsRecyclerView.adapter = null
        Toast.makeText(this, "Package deals feature under development", Toast.LENGTH_SHORT).show()
    }

    // SORT DIALOGS
    private fun showFlightSortDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_sort_flights, null)
        dialog.setContentView(view)

        val radioGroup = view.findViewById<android.widget.RadioGroup>(R.id.sortFlightRadioGroup)
        val applyButton = view.findViewById<MaterialButton>(R.id.applySortButton)

        // Pre-select current sort option
        when (currentFlightSort) {
            FlightSortOption.PRICE_LOW_TO_HIGH -> radioGroup.check(R.id.sortPriceLowToHigh)
            FlightSortOption.PRICE_HIGH_TO_LOW -> radioGroup.check(R.id.sortPriceHighToLow)
            FlightSortOption.DURATION_SHORTEST -> radioGroup.check(R.id.sortDurationShortest)
            FlightSortOption.DEPARTURE_MORNING -> radioGroup.check(R.id.sortDepartureMorning)
            FlightSortOption.ARRIVAL_MORNING -> radioGroup.check(R.id.sortArrivalMorning)
            null -> {}
        }

        applyButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            currentFlightSort = when (selectedId) {
                R.id.sortPriceLowToHigh -> FlightSortOption.PRICE_LOW_TO_HIGH
                R.id.sortPriceHighToLow -> FlightSortOption.PRICE_HIGH_TO_LOW
                R.id.sortDurationShortest -> FlightSortOption.DURATION_SHORTEST
                R.id.sortDepartureMorning -> FlightSortOption.DEPARTURE_MORNING
                R.id.sortArrivalMorning -> FlightSortOption.ARRIVAL_MORNING
                else -> null
            }

            // Apply sort
            currentFlightSort?.let {
                displayedFlights = FlightUtils.sortFlights(displayedFlights, it)
                updateFlightDisplay()
                Toast.makeText(this, "Flights sorted", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHotelSortDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_sort_hotels, null)
        dialog.setContentView(view)

        val radioGroup = view.findViewById<android.widget.RadioGroup>(R.id.sortHotelRadioGroup)
        val applyButton = view.findViewById<MaterialButton>(R.id.applyHotelSortButton)

        // Pre-select current sort option
        when (currentHotelSort) {
            HotelSortOption.PRICE_LOW_TO_HIGH -> radioGroup.check(R.id.sortHotelPriceLowToHigh)
            HotelSortOption.PRICE_HIGH_TO_LOW -> radioGroup.check(R.id.sortHotelPriceHighToLow)
            HotelSortOption.RATING_HIGH_TO_LOW -> radioGroup.check(R.id.sortHotelRating)
            HotelSortOption.NAME_A_TO_Z -> radioGroup.check(R.id.sortHotelName)
            null -> {}
        }

        applyButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            currentHotelSort = when (selectedId) {
                R.id.sortHotelPriceLowToHigh -> HotelSortOption.PRICE_LOW_TO_HIGH
                R.id.sortHotelPriceHighToLow -> HotelSortOption.PRICE_HIGH_TO_LOW
                R.id.sortHotelRating -> HotelSortOption.RATING_HIGH_TO_LOW
                R.id.sortHotelName -> HotelSortOption.NAME_A_TO_Z
                else -> null
            }

            // Apply sort
            currentHotelSort?.let {
                displayedHotels = HotelUtils.sortHotels(displayedHotels, it)
                updateHotelDisplay()
                Toast.makeText(this, "Hotels sorted", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // FILTER DIALOG
    private fun showFlightFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filter_flights, null)
        dialog.setContentView(view)

        // Get UI elements
        val chipNonstop = view.findViewById<Chip>(R.id.chipNonstop)
        val chip1Stop = view.findViewById<Chip>(R.id.chip1Stop)
        val chip2PlusStops = view.findViewById<Chip>(R.id.chip2PlusStops)
        val priceRangeSlider = view.findViewById<RangeSlider>(R.id.priceRangeSlider)
        val priceRangeText = view.findViewById<TextView>(R.id.priceRangeText)
        val chipMorning = view.findViewById<Chip>(R.id.chipMorning)
        val chipAfternoon = view.findViewById<Chip>(R.id.chipAfternoon)
        val chipEvening = view.findViewById<Chip>(R.id.chipEvening)
        val chipNight = view.findViewById<Chip>(R.id.chipNight)
        val clearButton = view.findViewById<MaterialButton>(R.id.clearFiltersButton)
        val applyButton = view.findViewById<MaterialButton>(R.id.applyFiltersButton)

        // Set current filter state
        chipNonstop.isChecked = flightFilterCriteria.allowNonstop
        chip1Stop.isChecked = flightFilterCriteria.allow1Stop
        chip2PlusStops.isChecked = flightFilterCriteria.allow2PlusStops
        chipMorning.isChecked = flightFilterCriteria.allowMorning
        chipAfternoon.isChecked = flightFilterCriteria.allowAfternoon
        chipEvening.isChecked = flightFilterCriteria.allowEvening
        chipNight.isChecked = flightFilterCriteria.allowNight

        priceRangeSlider.values = listOf(
            flightFilterCriteria.minPrice,
            flightFilterCriteria.maxPrice
        )
        priceRangeText.text = "${flightFilterCriteria.minPrice.toInt()} - ${flightFilterCriteria.maxPrice.toInt()}"

        // Update price text on slider change
        priceRangeSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            priceRangeText.text = "${values[0].toInt()} - ${values[1].toInt()}"
        }

        // Clear filters button
        clearButton.setOnClickListener {
            flightFilterCriteria = FlightFilterCriteria()
            chipNonstop.isChecked = true
            chip1Stop.isChecked = true
            chip2PlusStops.isChecked = true
            chipMorning.isChecked = true
            chipAfternoon.isChecked = true
            chipEvening.isChecked = true
            chipNight.isChecked = true
            priceRangeSlider.values = listOf(0f, 2000f)
            priceRangeText.text = "$0 - $2000"
        }

        // Apply filters button
        applyButton.setOnClickListener {
            // Update filter criteria
            flightFilterCriteria.allowNonstop = chipNonstop.isChecked
            flightFilterCriteria.allow1Stop = chip1Stop.isChecked
            flightFilterCriteria.allow2PlusStops = chip2PlusStops.isChecked
            flightFilterCriteria.allowMorning = chipMorning.isChecked
            flightFilterCriteria.allowAfternoon = chipAfternoon.isChecked
            flightFilterCriteria.allowEvening = chipEvening.isChecked
            flightFilterCriteria.allowNight = chipNight.isChecked

            val values = priceRangeSlider.values
            flightFilterCriteria.minPrice = values[0]
            flightFilterCriteria.maxPrice = values[1]

            // Apply filters
            displayedFlights = FlightUtils.filterFlights(originalFlights, flightFilterCriteria)

            // Re-apply sort if active
            currentFlightSort?.let {
                displayedFlights = FlightUtils.sortFlights(displayedFlights, it)
            }

            updateFlightDisplay()
            Toast.makeText(this, "Filters applied: ${displayedFlights.size} flights found", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

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

    private fun mapToHotel(offer: HotelOffer): Hotel {
        val hotelInfo = offer.hotel
        val firstOffer = offer.offers?.firstOrNull()

        val name = hotelInfo?.name ?: "Unknown Hotel"
        val rating = hotelInfo?.rating?.let { "⭐".repeat(it) } ?: "⭐⭐⭐"
        val reviews = "(4.5/5 - 150 reviews)"
        val location = hotelInfo?.address?.let { addr ->
            "📍 ${addr.cityName ?: "Unknown City"}, ${addr.lines?.firstOrNull() ?: ""}"
        } ?: "📍 Location not available"
        val amenities = "WiFi • Breakfast • Pool"
        val price = firstOffer?.price?.total?.let { "$it/night" } ?: "Price not available"
        val availability = "Available"

        return Hotel(name, rating, reviews, location, amenities, price, availability)
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