package hk.hku.cs.swifttrip

import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

class VisaActivity : AppCompatActivity() {

    private lateinit var visaDropdown: AutoCompleteTextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var toVisaTextView: TextView
    private lateinit var fromVisaTextView: TextView


    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // CoroutineScope for API calls
    private val activityScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visa)
        initializeViews()
        setupToolbar()
        setupDropdown()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        visaDropdown = findViewById(R.id.visaDropdown)
        toVisaTextView = findViewById(R.id.toVisaTextView)
        fromVisaTextView = findViewById(R.id.fromVisaTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdown() {
        val countries = Locale.getISOCountries().map { code ->
            Locale("", code).displayCountry
        }.sorted()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            countries
        )
        val fromLocation = intent.getStringExtra("fromLocation") ?: ""
        val toLocation = intent.getStringExtra("toLocation") ?: ""



        visaDropdown.setAdapter(adapter)
        visaDropdown.setOnClickListener { visaDropdown.showDropDown() }
        visaDropdown.setOnItemClickListener { parent, _, position, _ ->
            toVisaTextView.text="..."
            fromVisaTextView.text="..."
            val selectedCountry = parent.getItemAtPosition(position) as String

            callVisa(selectedCountry, getCountry(this, fromLocation), toVisaTextView)
            callVisa(selectedCountry, getCountry(this, toLocation), fromVisaTextView)
        }
    }
    fun getCountry(context: Context, location: String): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].countryName ?: location // fallback to input if country not found
            } else {
                location // fallback
            }
        } catch (e: Exception) {
            location // fallback
        }
    }

    private fun callVisa(userCountry: String, lookupCountry: String, visaTextView: TextView) {
        activityScope.launch {
            try {
                val userCountryCode = Locale.getISOCountries().firstOrNull(){code ->
                    Locale("", code).displayCountry.equals(userCountry, ignoreCase = true)
                }
                val lookupCountryCode = Locale.getISOCountries().firstOrNull { code ->
                    Locale("", code).displayCountry.equals(lookupCountry, ignoreCase = true)
                }?.uppercase()
                val url = "https://rough-sun-2523.fly.dev/visa/$userCountryCode/$lookupCountryCode"
                val response: HttpResponse = client.get(url)
                val rawText = response.bodyAsText()
                val json = Json.parseToJsonElement(rawText).jsonObject
                val category = json["category"]?.jsonObject
                val status = category?.get("name")?.jsonPrimitive?.content
                val duration = json["dur"]?.jsonPrimitive?.intOrNull

                when (status?.lowercase(Locale.ROOT)) {
                    "visa free" -> {
                        val text = if (duration != null)
                            "For nationals of $userCountry, no visa is required for $lookupCountry (stay up to $duration days)"
                        else
                            "For nationals of $userCountry, no visa is required for $lookupCountry"

                        visaTextView.text = text
                        visaTextView.setTextColor(Color.parseColor("#2E7D32"))
                    }
                    "evisa" -> {
                        visaTextView.text = "For nationals of $userCountry, an eVisa is available for $lookupCountry"
                        visaTextView.setTextColor(Color.parseColor("#EF6C00"))
                    }
                    "visa required" -> {
                        visaTextView.text = "For nationals of $userCountry, a visa is required for $lookupCountry"
                        visaTextView.setTextColor(Color.parseColor("#C62828"))
                    }
                    else -> {
                        visaTextView.text = "Visa information unavailable for $lookupCountry"
                        visaTextView.setTextColor(Color.GRAY)
                    }
                }
            } catch (e: Exception) {
                visaTextView.text = "Failed to fetch visa info for $lookupCountry"
                visaTextView.setTextColor(Color.GRAY)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
        client.close()
    }
}
