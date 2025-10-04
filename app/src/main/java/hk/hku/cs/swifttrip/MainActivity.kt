package hk.hku.cs.swifttrip

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var fromLocationEdit: TextInputEditText
    private lateinit var toLocationEdit: TextInputEditText
    private lateinit var departureDateEdit: TextInputEditText
    private lateinit var returnDateEdit: TextInputEditText
    private lateinit var passengersDropdown: AutoCompleteTextView
    private lateinit var searchButton: MaterialButton

    // Date formatting
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

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

        initializeViews()
        setupPassengerDropdown()
        setupDatePickers()
        setupSearchButton()
    }

    private fun initializeViews() {
        fromLocationEdit = findViewById(R.id.fromLocationEdit)
        toLocationEdit = findViewById(R.id.toLocationEdit)
        departureDateEdit = findViewById(R.id.departureDateEdit)
        returnDateEdit = findViewById(R.id.returnDateEdit)
        passengersDropdown = findViewById(R.id.passengersDropdown)
        searchButton = findViewById(R.id.searchButton)
    }

    private fun setupPassengerDropdown() {
        val passengerOptions = arrayOf(
            "1 Adult",
            "2 Adults",
            "3 Adults",
            "4 Adults",
            "1 Adult, 1 Child",
            "2 Adults, 1 Child",
            "2 Adults, 2 Children",
            "3 Adults, 1 Child",
            "Family (2 Adults, 3 Children)"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, passengerOptions)
        passengersDropdown.setAdapter(adapter)
        passengersDropdown.setText("1 Adult", false)
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

        // Simulate API call delay
        searchButton.postDelayed({
            // Re-enable button
            searchButton.isEnabled = true
            searchButton.text = "Search Flights & Hotels"

            // Show results (placeholder for now)
            showSearchResults(searchData)
        }, 2000)

        // TODO: Implement actual API calls to flight and hotel services
        // TODO: Navigate to results activity
    }

    private fun getSearchData(): SearchData {
        return SearchData(
            fromLocation = fromLocationEdit.text.toString().trim(),
            toLocation = toLocationEdit.text.toString().trim(),
            departureDate = departureDate,
            returnDate = returnDate,
            passengers = passengersDropdown.text.toString(),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun showSearchResults(searchData: SearchData) {
        // Navigate to ResultsActivity
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("fromLocation", searchData.fromLocation)
        intent.putExtra("toLocation", searchData.toLocation)
        intent.putExtra("departureDate", searchData.departureDate?.timeInMillis ?: 0L)
        intent.putExtra("returnDate", searchData.returnDate?.timeInMillis ?: 0L)
        intent.putExtra("passengers", searchData.passengers)
        startActivity(intent)
    }
}