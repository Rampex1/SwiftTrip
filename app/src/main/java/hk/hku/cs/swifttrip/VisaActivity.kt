package hk.hku.cs.swifttrip

import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

class VisaActivity : AppCompatActivity() {

    private lateinit var visaDropdown: AutoCompleteTextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var toVisaTextView: TextView
    private lateinit var fromVisaTextView: TextView
    private lateinit var toVisaCard: CardView
    private lateinit var fromVisaCard: CardView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var emptyStateView: View
    private lateinit var contentView: View

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val activityScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visa)
        initializeViews()
        setupToolbar()
        setupDropdown()
        showEmptyState()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        visaDropdown = findViewById(R.id.visaDropdown)
        toVisaTextView = findViewById(R.id.toVisaTextView)
        fromVisaTextView = findViewById(R.id.fromVisaTextView)
        toVisaCard = findViewById(R.id.toVisaCard)
        fromVisaCard = findViewById(R.id.fromVisaCard)
        progressIndicator = findViewById(R.id.progressIndicator)
        emptyStateView = findViewById(R.id.emptyStateView)
        contentView = findViewById(R.id.contentView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Visa Requirements"
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdown() {
        val countries = Locale.getISOCountries().map { code ->
            Locale("", code).displayCountry
        }.sorted()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            countries
        )

        val fromLocation = intent.getStringExtra("fromLocation") ?: ""
        val toLocation = intent.getStringExtra("toLocation") ?: ""

        visaDropdown.setAdapter(adapter)
        visaDropdown.threshold = 1 // Start showing suggestions after 1 character

        visaDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedCountry = parent.getItemAtPosition(position) as String
            showContent()
            showLoading(true)
            resetVisaInfo()

            callVisa(selectedCountry, getCountry(this, fromLocation), fromVisaTextView, fromVisaCard, true)
            callVisa(selectedCountry, getCountry(this, toLocation), toVisaTextView, toVisaCard, false)
        }
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        progressIndicator.visibility = View.GONE
    }

    private fun showContent() {
        emptyStateView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun resetVisaInfo() {
        toVisaTextView.text = "Loading..."
        fromVisaTextView.text = "Loading..."
        toVisaTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        fromVisaTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        toVisaCard.setCardBackgroundColor(Color.WHITE)
        fromVisaCard.setCardBackgroundColor(Color.WHITE)
    }

    fun getCountry(context: Context, location: String): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].countryName ?: location
            } else {
                location
            }
        } catch (e: Exception) {
            location
        }
    }

    private var loadingCount = 2

    private fun callVisa(
        userCountry: String,
        lookupCountry: String,
        visaTextView: TextView,
        cardView: CardView,
        isFirstCall: Boolean
    ) {
        activityScope.launch {
            try {
                val userCountryCode = Locale.getISOCountries().firstOrNull { code ->
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

                val prefix = if (isFirstCall)
                    "$lookupCountry:\n"
                else
                    "$lookupCountry:\n"

                when (status?.lowercase(Locale.ROOT)) {
                    "visa free" -> {
                        val text = if (duration != null)
                            prefix + "✓ Visa not required (up to $duration days)"
                        else
                            prefix + "✓ Visa not required"

                        visaTextView.text = text
                        visaTextView.setTextColor(Color.parseColor("#1B5E20"))
                        cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                    }
                    "evisa" -> {
                        visaTextView.text = prefix + "⚠ eVisa available"
                        visaTextView.setTextColor(Color.parseColor("#E65100"))
                        cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    }
                    "visa required" -> {
                        visaTextView.text = prefix + "✕ Visa required"
                        visaTextView.setTextColor(Color.parseColor("#B71C1C"))
                        cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    }
                    else -> {
                        visaTextView.text = prefix + "Information unavailable"
                        visaTextView.setTextColor(Color.GRAY)
                        cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                    }
                }
            } catch (e: Exception) {
                visaTextView.text = "$lookupCountry:\nFailed to fetch information"
                visaTextView.setTextColor(Color.GRAY)
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            } finally {
                loadingCount--
                if (loadingCount == 0) {
                    showLoading(false)
                    loadingCount = 2
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
        client.close()
    }
}