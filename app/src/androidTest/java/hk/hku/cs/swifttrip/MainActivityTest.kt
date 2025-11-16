package hk.hku.cs.swifttrip

import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun testActivityLaunchesSuccessfully() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            assertNotNull("Activity should not be null", activity)
        }
        scenario.close()
    }

    @Test
    fun testInitialAdultCountIsOne() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val adultCountText = activity.findViewById<TextView>(R.id.tvAdult)
            assertEquals("Initial adult count should be 1", "1 Adult", adultCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testInitialChildCountIsZero() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val childCountText = activity.findViewById<TextView>(R.id.tvChild)
            assertEquals("Initial child count should be 0", "0 Children", childCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testAdultIncrementButton() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val incrementButton = activity.findViewById<MaterialButton>(R.id.adultPlusButton)
            val adultCountText = activity.findViewById<TextView>(R.id.tvAdult)

            incrementButton.performClick()
            assertEquals("Adult count should be 2 after increment", "2 Adults", adultCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testAdultDecrementButton() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val incrementButton = activity.findViewById<MaterialButton>(R.id.adultPlusButton)
            val decrementButton = activity.findViewById<MaterialButton>(R.id.adultMinusButton)
            val adultCountText = activity.findViewById<TextView>(R.id.tvAdult)

            // Increment to 2
            incrementButton.performClick()
            assertEquals("2 Adults", adultCountText.text.toString())

            // Decrement back to 1
            decrementButton.performClick()
            assertEquals("Adult count should be 1 after decrement", "1 Adult", adultCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testAdultCountCannotGoBelowOne() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val decrementButton = activity.findViewById<MaterialButton>(R.id.adultMinusButton)
            val adultCountText = activity.findViewById<TextView>(R.id.tvAdult)

            // Try to decrement from 1 (should stay at 1)
            decrementButton.performClick()
            decrementButton.performClick()

            assertEquals("Adult count should not go below 1", "1 Adult", adultCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testChildIncrementButton() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val incrementButton = activity.findViewById<MaterialButton>(R.id.childPlusButton)
            val childCountText = activity.findViewById<TextView>(R.id.tvChild)

            incrementButton.performClick()
            assertEquals("Child count should be 1 after increment", "1 Child", childCountText.text.toString())

            incrementButton.performClick()
            assertEquals("Child count should be 2 after second increment", "2 Children", childCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testChildDecrementButton() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val incrementButton = activity.findViewById<MaterialButton>(R.id.childPlusButton)
            val decrementButton = activity.findViewById<MaterialButton>(R.id.childMinusButton)
            val childCountText = activity.findViewById<TextView>(R.id.tvChild)

            // Increment to 1
            incrementButton.performClick()
            assertEquals("1 Child", childCountText.text.toString())

            // Decrement back to 0
            decrementButton.performClick()
            assertEquals("Child count should be 0 after decrement", "0 Children", childCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testChildCountCannotGoBelowZero() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val decrementButton = activity.findViewById<MaterialButton>(R.id.childMinusButton)
            val childCountText = activity.findViewById<TextView>(R.id.tvChild)

            // Try to decrement from 0 (should stay at 0)
            decrementButton.performClick()
            decrementButton.performClick()

            assertEquals("Child count should not go below 0", "0 Children", childCountText.text.toString())
        }
        scenario.close()
    }

    @Test
    fun testEmptyOriginShowsError() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val originInput = activity.findViewById<AppCompatAutoCompleteTextView>(R.id.fromLocationEdit)
            val destinationInput = activity.findViewById<AppCompatAutoCompleteTextView>(R.id.toLocationEdit)
            val searchButton = activity.findViewById<MaterialButton>(R.id.searchButton)

            // Fill only destination
            destinationInput.setText("New York")

            // Click search
            searchButton.performClick()

            // Check error
            assertNotNull("Origin should have error", originInput.error)
            assertTrue("Error message should mention departure location",
                originInput.error.toString().contains("departure location"))
        }
        scenario.close()
    }

    @Test
    fun testSameOriginAndDestinationShowsError() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val originInput = activity.findViewById<AppCompatAutoCompleteTextView>(R.id.fromLocationEdit)
            val destinationInput = activity.findViewById<AppCompatAutoCompleteTextView>(R.id.toLocationEdit)
            val searchButton = activity.findViewById<MaterialButton>(R.id.searchButton)

            // Set same location
            originInput.setText("Hong Kong")
            destinationInput.setText("Hong Kong")

            // Click search
            searchButton.performClick()

            // Check error
            assertNotNull("Destination should have error", destinationInput.error)
        }
        scenario.close()
    }

    @Test
    fun testDatePickersAreClickable() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val departureDateInput = activity.findViewById<TextInputEditText>(R.id.departureDateEdit)
            val returnDateInput = activity.findViewById<TextInputEditText>(R.id.returnDateEdit)

            assertTrue("Departure date should be clickable", departureDateInput.isClickable)
            assertTrue("Return date should be clickable", returnDateInput.isClickable)
        }
        scenario.close()
    }
}