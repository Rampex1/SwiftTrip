package hk.hku.cs.swifttrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlightAdapter(private val flights: List<Flight>) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flights[position]
        holder.bind(flight)
    }

    override fun getItemCount(): Int = flights.size

    class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val airlineText: TextView = itemView.findViewById(R.id.airlineText)
        private val flightPriceText: TextView = itemView.findViewById(R.id.flightPriceText)
        private val departureTimeText: TextView = itemView.findViewById(R.id.departureTimeText)
        private val departureAirportText: TextView = itemView.findViewById(R.id.departureAirportText)
        private val arrivalTimeText: TextView = itemView.findViewById(R.id.arrivalTimeText)
        private val arrivalAirportText: TextView = itemView.findViewById(R.id.arrivalAirportText)
        private val durationText: TextView = itemView.findViewById(R.id.durationText)
        private val stopsText: TextView = itemView.findViewById(R.id.stopsText)
        private val flightClassText: TextView = itemView.findViewById(R.id.flightClassText)
        private val seatsLeftText: TextView = itemView.findViewById(R.id.seatsLeftText)

        fun bind(flight: Flight) {
            airlineText.text = flight.airline
            flightPriceText.text = flight.price
            departureTimeText.text = flight.departureTime
            departureAirportText.text = flight.departureAirport
            arrivalTimeText.text = flight.arrivalTime
            arrivalAirportText.text = flight.arrivalAirport
            durationText.text = flight.duration
            stopsText.text = flight.stops
            flightClassText.text = flight.flightClass
            seatsLeftText.text = flight.seatsLeft
        }
    }
}
