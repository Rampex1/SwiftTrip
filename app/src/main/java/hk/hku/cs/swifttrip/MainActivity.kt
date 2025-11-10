package hk.hku.cs.swifttrip

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import com.google.gson.Gson
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var originInput: AppCompatAutoCompleteTextView
    private lateinit var destinationInput: AppCompatAutoCompleteTextView
    private lateinit var departureDateInput: TextInputEditText
    private lateinit var returnDateInput: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var adultIncrementButton: MaterialButton
    private lateinit var adultDecrementButton: MaterialButton
    private lateinit var childIncrementButton: MaterialButton
    private lateinit var childDecrementButton: MaterialButton
    private lateinit var adultCountText: TextView
    private lateinit var childCountText: TextView

    private lateinit var apiService: ApiService
    private var originAdapter: CityAutocompleteAdapter? = null
    private var destinationAdapter: CityAutocompleteAdapter? = null

    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var adultCount = 1
    private var childCount = 0
    private var selectedDepartureDate: Calendar? = null
    private var selectedReturnDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        apiService = ApiService()

        initializeViews()
        setupDatePickers()
        setupSearchButton()
        calculatePassengers()
        setupAutocomplete()

    }
    private fun initializeViews() {
        originInput = findViewById(R.id.fromLocationEdit)
        destinationInput = findViewById(R.id.toLocationEdit)
        departureDateInput = findViewById(R.id.departureDateEdit)
        returnDateInput = findViewById(R.id.returnDateEdit)
        adultIncrementButton = findViewById(R.id.adultPlusButton)
        adultDecrementButton = findViewById(R.id.adultMinusButton)
        childIncrementButton = findViewById(R.id.childPlusButton)
        childDecrementButton = findViewById(R.id.childMinusButton)
        adultCountText = findViewById(R.id.tvAdult)
        childCountText = findViewById(R.id.tvChild)
        searchButton = findViewById(R.id.searchButton)
    }




    private fun calculatePassengers() {
        adultIncrementButton.setOnClickListener {
            adultCount++
            adultDecrementButton.setBackgroundColor("#2196F3".toColorInt())
            updatePassengerCountText()
        }
        adultDecrementButton.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                if (adultCount == 1) {
                    adultDecrementButton.setBackgroundColor("#777679".toColorInt())
                }
            }
            updatePassengerCountText()
        }
        childIncrementButton.setOnClickListener {
            childCount++
            childDecrementButton.setBackgroundColor("#2196F3".toColorInt())
            updatePassengerCountText()
        }
        childDecrementButton.setOnClickListener {
            if (childCount > 0) {
                childCount--
                if (childCount == 0) {
                    childDecrementButton.setBackgroundColor("#777679".toColorInt())
                }
            }
            updatePassengerCountText()
        }
    }

    private fun updatePassengerCountText() {
        adultCountText.text = if (adultCount == 1) "$adultCount Adult" else "$adultCount Adults"
        childCountText.text = if (childCount == 1) "$childCount Child" else "$childCount Children"
    }

    private fun setupDatePickers() {
        departureDateInput.setOnClickListener {
            showDatePicker { selectedDate ->
                selectedDepartureDate = selectedDate
                departureDateInput.setText(displayDateFormat.format(selectedDate.time))
                selectedReturnDate?.let { returnDate ->
                    if (returnDate.before(selectedDate)) {
                        selectedReturnDate = null
                        returnDateInput.setText("")
                    }
                }
            }
        }

        returnDateInput.setOnClickListener {
            val minDate = selectedDepartureDate?.timeInMillis ?: System.currentTimeMillis()
            showDatePicker(minDate = minDate) { selectedDate ->
                selectedReturnDate = selectedDate
                returnDateInput.setText(displayDateFormat.format(selectedDate.time))
            }
        }
    }

    private fun showDatePicker(
        minDate: Long = System.currentTimeMillis(),
        onDateSelected: (Calendar) -> Unit
    ) {
        val currentDate = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedDate)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date
        datePickerDialog.datePicker.minDate = minDate

        // Set maximum date (1 year from now)
        val maxDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        datePickerDialog.show()
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            if (validateInputs()) {
                performSearch()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val origin = originInput.text.toString().trim()
        val destination = destinationInput.text.toString().trim()
        val departureText = departureDateInput.text.toString().trim()
        val returnText = returnDateInput.text.toString().trim()

        if (childCount + adultCount > 9) {
            Toast.makeText(this, "Passenger count must be less than 10", Toast.LENGTH_SHORT).show()
            return false
        }

        if (origin.isEmpty()) {
            originInput.error = "Please enter departure location"
            originInput.requestFocus()
            return false
        }

        if (destination.isEmpty()) {
            destinationInput.error = "Please enter destination"
            destinationInput.requestFocus()
            return false
        }

        if (origin.equals(destination, ignoreCase = true)) {
            destinationInput.error = "Destination must be different from departure location"
            destinationInput.requestFocus()
            return false
        }

        if (departureText.isEmpty()) {
            Toast.makeText(this, "Please select departure date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (returnText.isEmpty()) {
            Toast.makeText(this, "Please select return date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedDepartureDate != null && selectedReturnDate != null && selectedReturnDate!!.before(selectedDepartureDate)) {
            Toast.makeText(this, "Return date cannot be before departure date", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performSearch() {
        // Disable search button to prevent multiple clicks
        searchButton.isEnabled = false
        searchButton.text = "Searching..."

        val criteria = buildSearchCriteria()
        Toast.makeText(this, "Searching for flights and hotels...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val flightResponse = searchFlights(criteria)
            val hotelResponse = searchHotels(criteria)
            
            searchButton.isEnabled = true
            searchButton.text = "Search Flights & Hotels"
            
            if (flightResponse == null && hotelResponse == null) {
                Toast.makeText(this@MainActivity, "Network unavailable - showing sample results", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Search completed successfully", Toast.LENGTH_SHORT).show()
            }
            
            navigateToResults(criteria, flightResponse, hotelResponse)
        }
    }

    private fun navigateToResults(criteria: SearchCriteria, flightResponse: FlightResponse?, hotelResponse: HotelResponse?) {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("fromLocation", criteria.origin)
        intent.putExtra("toLocation", criteria.destination)
        intent.putExtra("departureDate", criteria.departureDate?.timeInMillis ?: 0L)
        intent.putExtra("returnDate", criteria.returnDate?.timeInMillis ?: 0L)
        intent.putExtra("passengers", criteria.passengers)
        intent.putExtra("flightResponseJson", Gson().toJson(flightResponse))
        intent.putExtra("hotelResponseJson", Gson().toJson(hotelResponse))
        startActivity(intent)
    }

    private suspend fun searchFlights(criteria: SearchCriteria): FlightResponse? {
        val token = withContext(Dispatchers.IO) {
            apiService.getAmadeusAccessToken()
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Using mock flight data (API unavailable)", Toast.LENGTH_SHORT).show()
            }
            return null
        }

        val auth = "Bearer $token"
        val departureDateStr = apiDateFormat.format(criteria.departureDate?.time ?: return null)
        val returnDateStr = apiDateFormat.format(criteria.returnDate?.time ?: return null)

        val originCode = withContext(Dispatchers.IO) {
            apiService.getAirportCode(auth, criteria.origin)
        }
        val destinationCode = withContext(Dispatchers.IO) {
            apiService.getAirportCode(auth, criteria.destination)
        }

        if (originCode == null || destinationCode == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Could not find airport codes for: ${criteria.origin} or ${criteria.destination}",
                    Toast.LENGTH_LONG
                ).show()
            }
            return null
        }

        val response = withContext(Dispatchers.IO) {
            apiService.getFlightOffers(auth, originCode, destinationCode, departureDateStr, returnDateStr, adultCount)
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Flight search failed", Toast.LENGTH_SHORT).show()
            }
            return null
        }

        if (response.data == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No flights found for this route/dates", Toast.LENGTH_SHORT).show()
            }
            return null
        }

        val uniqueFlights = FlightUtils.removeDuplicateFlights(response.data!!)
        return FlightResponse(data = uniqueFlights, errors = response.errors)
    }

    private suspend fun searchHotels(criteria: SearchCriteria): HotelResponse? {
        val token = withContext(Dispatchers.IO) {
            apiService.getAmadeusAccessToken()
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Using mock hotel data (API unavailable)", Toast.LENGTH_SHORT).show()
            }
            return apiService.createMockHotelResponse()
        }

        val auth = "Bearer $token"
        val checkInDate = apiDateFormat.format(criteria.departureDate?.time ?: return null)
        val checkOutDate = apiDateFormat.format(criteria.returnDate?.time ?: return null)

        val cityCode = withContext(Dispatchers.IO) {
            apiService.getCityCode(auth, criteria.destination)
        } ?: return null

        return withContext(Dispatchers.IO) {
            apiService.getHotelOffers(auth, cityCode, checkInDate, checkOutDate, adultCount)
        }
    }

    private fun buildSearchCriteria(): SearchCriteria {
        return SearchCriteria(
            origin = originInput.text.toString().trim(),
            destination = destinationInput.text.toString().trim(),
            departureDate = selectedDepartureDate,
            returnDate = selectedReturnDate,
            passengers = "${adultCountText.text}, ${childCountText.text}",
            timestamp = System.currentTimeMillis()
        )
    }

    private fun setupAutocomplete() {
        originAdapter = CityAutocompleteAdapter(this)
        destinationAdapter = CityAutocompleteAdapter(this)

        originInput.setAdapter(originAdapter)
        destinationInput.setAdapter(destinationAdapter)

        originInput.setOnItemClickListener { _, _, position, _ ->
            originAdapter?.getItem(position)?.let {
                originInput.setText(it.cityName, false)
                originInput.dismissDropDown()
            }
        }

        destinationInput.setOnItemClickListener { _, _, position, _ ->
            destinationAdapter?.getItem(position)?.let {
                destinationInput.setText(it.cityName, false)
                destinationInput.dismissDropDown()
            }
        }
    }
}