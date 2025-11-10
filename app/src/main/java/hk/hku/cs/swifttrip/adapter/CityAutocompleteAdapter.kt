package hk.hku.cs.swifttrip.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import hk.hku.cs.swifttrip.utils.CityList
import hk.hku.cs.swifttrip.utils.CitySuggestion

class CityAutocompleteAdapter(
    context: Context
) : ArrayAdapter<CitySuggestion>(context, R.layout.simple_dropdown_item_1line), Filterable {

    private var filteredCities: List<CitySuggestion> = emptyList()
    private val allCities = CityList.availableCities

    override fun getCount(): Int = filteredCities.size

    override fun getItem(position: Int): CitySuggestion? = filteredCities.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.simple_dropdown_item_1line, parent, false)

        val textView = view.findViewById<TextView>(R.id.text1)
        textView.text = getItem(position)?.cityName ?: ""

        return view
    }

    override fun getFilter(): Filter = cityFilter

    private val cityFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            if (constraint.isNullOrBlank()) {
                results.values = emptyList<CitySuggestion>()
                results.count = 0
                return results
            }

            val query = constraint.toString().trim().lowercase()

            if (query.length < 3) {
                results.values = emptyList<CitySuggestion>()
                results.count = 0
                return results
            }

            val matches = allCities
                .filter { it.cityName?.lowercase()?.contains(query) == true  }
                .take(3)

            results.values = matches
            results.count = matches.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCities = (results?.values as? List<CitySuggestion>) ?: emptyList()
            notifyDataSetChanged()
        }
    }
}