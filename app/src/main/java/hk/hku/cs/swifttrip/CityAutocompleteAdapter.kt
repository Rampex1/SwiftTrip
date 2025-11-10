package hk.hku.cs.swifttrip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class CityAutocompleteAdapter(
    context: Context
) : ArrayAdapter<CitySuggestion>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private var suggestions: List<CitySuggestion> = emptyList()
    private val allCities = CityList.topCities

    override fun getCount(): Int = suggestions.size

    override fun getItem(position: Int): CitySuggestion? = suggestions.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val suggestion = getItem(position)
        textView.text = suggestion?.cityName ?: ""
        
        return view
    }

    override fun getFilter(): Filter {
        return cityFilter
    }

    private val cityFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            
            if (constraint == null || constraint.isEmpty()) {
                results.values = emptyList<CitySuggestion>()
                results.count = 0
                return results
            }

            val query = constraint.toString().trim().lowercase()
            
            // Only search if query has at least 3 characters
            if (query.length < 3) {
                results.values = emptyList<CitySuggestion>()
                results.count = 0
                return results
            }

            // Filter cities from hardcoded list
            val filteredCities = allCities
                .filter { city ->
                    city.cityName?.lowercase()?.contains(query.lowercase()) == true
                }
                .take(3) // Limit to 3 results

            results.values = filteredCities
            results.count = filteredCities.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.values != null) {
                suggestions = results.values as List<CitySuggestion>
                notifyDataSetChanged()
            } else {
                suggestions = emptyList()
                notifyDataSetInvalidated()
            }
        }
    }
}

