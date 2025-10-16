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
    private lateinit var fromLocationEdit: TextInputEditText
    private lateinit var toLocationEdit: TextInputEditText
    private lateinit var departureDateEdit: TextInputEditText
    private lateinit var returnDateEdit: TextInputEditText
    private lateinit var passengersDropdown: AutoCompleteTextView
    private lateinit var searchButton: MaterialButton
    private lateinit var adultPlusButton: MaterialButton
    private lateinit var adultMinusButton: MaterialButton
    private lateinit var childPlusButton: MaterialButton
    private lateinit var childMinusButton: MaterialButton
    private lateinit var tvAdult: TextView
    private lateinit var tvChild: TextView



    private lateinit var apiService: ApiService

    // Date formatting
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Initial passenger counts
    private var adultCount = 1
    private var childCount = 0

    // Selected dates
    private var departureDate: Calendar? = null
    private var returnDate: Calendar? = null

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
    }
    private fun initializeViews() {
        fromLocationEdit = findViewById(R.id.fromLocationEdit)
        toLocationEdit = findViewById(R.id.toLocationEdit)
        departureDateEdit = findViewById(R.id.departureDateEdit)
        returnDateEdit = findViewById(R.id.returnDateEdit)
        adultPlusButton = findViewById(R.id.adultPlusButton)
        adultMinusButton = findViewById(R.id.adultMinusButton)
        childPlusButton = findViewById(R.id.childPlusButton)
        childMinusButton = findViewById(R.id.childMinusButton)
        tvAdult = findViewById(R.id.tvAdult)
        tvChild = findViewById(R.id.tvChild)
        searchButton = findViewById(R.id.searchButton)

    }



    private fun calculatePassengers() {
        adultPlusButton.setOnClickListener {
            adultCount = adultCount + 1
            adultMinusButton.setBackgroundColor("#2196F3".toColorInt())
            updatePassText("Adult")
        }
        adultMinusButton.setOnClickListener {
            if (adultCount > 1){
                adultCount = adultCount - 1
                if (adultCount == 1) {
                    adultMinusButton.setBackgroundColor("#777679".toColorInt())
                }
            }
            updatePassText("Adult")
        }
        childPlusButton.setOnClickListener {
            childCount = childCount + 1
            childMinusButton.setBackgroundColor("#2196F3".toColorInt())
            updatePassText("Child")
        }
        childMinusButton.setOnClickListener {
            if (childCount > 0){
                childCount = childCount - 1
                if (childCount == 0) {
                    childMinusButton.setBackgroundColor("#777679".toColorInt())
                }
            }
            updatePassText("Child")
        }


    }
    private fun updatePassText(string: String){
        if (string == "Adult"){
            if (adultCount == 1){
                tvAdult.text = adultCount.toString() + " Adult"
            }
            else {
                tvAdult.text = adultCount.toString() + " Adults"
            }
        }
        else{
            if (childCount == 1){
                tvChild.text = childCount.toString() + " Child"
            }
            else{
                tvChild.text = childCount.toString() + " Children"
            }
        }
    }

    private fun setupDatePickers() {
        // Departure date picker
        departureDateEdit.setOnClickListener {
            showDatePicker { selectedDate ->
                departureDate = selectedDate
                departureDateEdit.setText(dateFormat.format(selectedDate.time))

                // Clear return date if it's before departure date
                returnDate?.let { returnCal ->
                    if (returnCal.before(selectedDate)) {
                        returnDate = null
                        returnDateEdit.setText("")
                    }
                }
            }
        }

        // Return date picker
        returnDateEdit.setOnClickListener {
            val minDate = departureDate?.timeInMillis ?: System.currentTimeMillis()
            showDatePicker(minDate = minDate) { selectedDate ->
                returnDate = selectedDate
                returnDateEdit.setText(dateFormat.format(selectedDate.time))
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
        val fromLocation = fromLocationEdit.text.toString().trim()
        val toLocation = toLocationEdit.text.toString().trim()
        val departureText = departureDateEdit.text.toString().trim()
        val returnText = returnDateEdit.text.toString().trim()

        // For whatever reason, the API does not accept traveller counts >= 10.
        // As such, have implemented this as a temporary measure
        // Will go back and implement logic so that grey appears when attempted,
        // but for now error message - kk
        if(childCount + adultCount > 9){
                Toast.makeText(this, "Passenger count must be less than 10", Toast.LENGTH_SHORT).show()
                return false
        }



        // Validate from location
        if (fromLocation.isEmpty()) {
            fromLocationEdit.error = "Please enter departure location"
            fromLocationEdit.requestFocus()
            return false
        }

        // Validate to location
        if (toLocation.isEmpty()) {
            toLocationEdit.error = "Please enter destination"
            toLocationEdit.requestFocus()
            return false
        }

        // Check if locations are different
        if (fromLocation.equals(toLocation, ignoreCase = true)) {
            toLocationEdit.error = "Destination must be different from departure location"
            toLocationEdit.requestFocus()
            return false
        }

        // Validate departure date
        if (departureText.isEmpty()) {
            Toast.makeText(this, "Please select departure date", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate return date
        if (returnText.isEmpty()) {
            Toast.makeText(this, "Please select return date", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate date order
        if (departureDate != null && returnDate != null && returnDate!!.before(departureDate)) {
            Toast.makeText(this, "Return date cannot be before departure date", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performSearch() {
        // Disable search button to prevent multiple clicks
        searchButton.isEnabled = false
        searchButton.text = "Searching..."

        // Get form data
        val searchData = getSearchData()

        // Show loading toast
        Toast.makeText(this, "Searching for flights and hotels...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            // Show network status
            Toast.makeText(this@MainActivity, "Checking network connectivity...", Toast.LENGTH_SHORT).show()
            
            // Fetch both flights and hotels in parallel
            val flightResponse = getFlightSearchData(searchData)
            val hotelResponse = getHotelSearchData(searchData)
            
            searchButton.isEnabled = true
            searchButton.text = "Search Flights & Hotels"
            
            // If both API calls failed, show a message but still navigate with mock data
            if (flightResponse == null && hotelResponse == null) {
                Toast.makeText(this@MainActivity, "Network unavailable - showing sample results", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Search completed successfully", Toast.LENGTH_SHORT).show()
            }
            
            // Navigate to results activity
            showSearchResults(searchData, flightResponse, hotelResponse)
        }
    }

    private fun showSearchResults(searchData: SearchData, flightResponse: FlightResponse?, hotelResponse: HotelResponse?) {
        // Navigate to ResultsActivity
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("fromLocation", searchData.fromLocation)
        intent.putExtra("toLocation", searchData.toLocation)
        intent.putExtra("departureDate", searchData.departureDate?.timeInMillis ?: 0L)
        intent.putExtra("returnDate", searchData.returnDate?.timeInMillis ?: 0L)
        intent.putExtra("passengers", searchData.passengers)
        // Pass flight response as JSON string extra (using Gson for serialization)
        intent.putExtra("flightResponseJson", Gson().toJson(flightResponse))
        // Pass hotel response as JSON string extra
        intent.putExtra("hotelResponseJson", Gson().toJson(hotelResponse))
        startActivity(intent)
    }

    private suspend fun getFlightSearchData(sd: SearchData): FlightResponse? {
        Log.d("MainActivity", "=== ATTEMPTING TO GET FLIGHT DATA ===")
        Log.d("MainActivity", "Search data: from=${sd.fromLocation}, to=${sd.toLocation}")

        val token = withContext(Dispatchers.IO) {
            Log.d("MainActivity", "Calling getAmadeusAccessToken()...")
            val result = apiService.getAmadeusAccessToken()
            Log.d("MainActivity", "Token result: ${if (result != null) "SUCCESS (${result.take(10)}...)" else "NULL"}")
            result
        }

        if (token == null) {
            Log.e("MainActivity", "=== TOKEN IS NULL - USING MOCK DATA ===")
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Using mock flight data (API unavailable)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        Log.d("MainActivity", "=== TOKEN SUCCESS - PROCEEDING WITH API CALLS ===")

        val auth = "Bearer $token"
        val depDateStr = apiDateFormat.format(sd.departureDate?.time ?: return null)
        val retDateStr = apiDateFormat.format(sd.returnDate?.time ?: return null)
        val adults = adultCount

        // ✅ CHANGED: Use airport code lookup instead of city code
        val originCode = withContext(Dispatchers.IO) {
            apiService.getAirportCode(auth, sd.fromLocation)
        }
        val destCode = withContext(Dispatchers.IO) {
            apiService.getAirportCode(auth, sd.toLocation)
        }

        Log.d("MainActivity", "📍 Airport codes - Origin: $originCode, Destination: $destCode")

        if (originCode == null || destCode == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                    "Could not find airport codes for: ${sd.fromLocation} or ${sd.toLocation}",
                    Toast.LENGTH_LONG
                ).show()
            }
            return null
        }

        val response = withContext(Dispatchers.IO) {
            apiService.getFlightOffers(auth, originCode, destCode, depDateStr, retDateStr, adults)
        }

        // ✅ ADDED: Log the flight response
        Log.d("MainActivity", "=== FLIGHT SEARCH RESPONSE ===")
        if (response != null) {
            Log.d("MainActivity", "✅ SUCCESS - Found ${response.data?.size ?: 0} flights")
            response.data?.take(2)?.forEachIndexed { index, flight ->
                Log.d("MainActivity", "Flight ${index + 1}: ${flight.price?.currency} ${flight.price?.total}")
            }
        } else {
            Log.d("MainActivity", "❌ FAILED - No flight response")
        }

        if (response == null || response.data == null) {
            withContext(Dispatchers.Main) {
                if (response == null) {
                    Toast.makeText(this@MainActivity, "Flight search failed (check logs for error).", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "No flights found for this route/dates.", Toast.LENGTH_SHORT).show()
                }
            }
            return null
        }

        return response
    }

    private suspend fun getHotelSearchData(sd: SearchData): HotelResponse? {
        val token = withContext(Dispatchers.IO) {
            apiService.getAmadeusAccessToken()
        }

        if (token == null) {
            Log.e("MainActivity", "Failed to fetch access token, returning mock hotel data")
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Using mock hotel data (API unavailable)", Toast.LENGTH_SHORT).show()
            }
            return apiService.createMockHotelResponse()
        }

        val auth = "Bearer $token"
        val checkInDate = apiDateFormat.format(sd.departureDate?.time ?: return null)
        val checkOutDate = apiDateFormat.format(sd.returnDate?.time ?: return null)
        val adults = adultCount

        val destCode = withContext(Dispatchers.IO) { apiService.getCityCode(auth, sd.toLocation) }

        if (destCode == null) {
            return null
        }

        val response = withContext(Dispatchers.IO) { 
            apiService.getHotelOffers(auth, destCode, checkInDate, checkOutDate, adults) 
        }

        if (response == null || response.data == null) {
            if (response == null) {
                Log.e("MainActivity", "Hotel search failed")
            } else {
                Log.e("MainActivity", "No hotels found for this destination/dates")
            }
            return null
        }

        return response
    }

    private fun getSearchData(): SearchData {
        return SearchData(
            fromLocation = fromLocationEdit.text.toString().trim(),
            toLocation = toLocationEdit.text.toString().trim(),
            departureDate = departureDate,
            returnDate = returnDate,
            passengers = tvAdult.text.toString() + ", " + tvChild.text.toString(),
            timestamp = System.currentTimeMillis()
        )
    }
}