package hk.hku.cs.swifttrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class FlightOfferAdapter(
    private val flights: List<FlightOffer>,
    private val onItemClick: (FlightOffer) -> Unit
) : RecyclerView.Adapter<FlightOfferAdapter.FlightOfferViewHolder>() {

    class FlightOfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.flightCard)
        val priceText: TextView = itemView.findViewById(R.id.priceText)
        val airlineText: TextView = itemView.findViewById(R.id.airlineText)
        val outboundRouteText: TextView = itemView.findViewById(R.id.outboundRouteText)
        val returnRouteText: TextView = itemView.findViewById(R.id.returnRouteText)
        val seatsText: TextView = itemView.findViewById(R.id.seatsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightOfferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flight, parent, false)
        return FlightOfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightOfferViewHolder, position: Int) {
        val flight = flights[position]

        // Use the extension functions to display proper routes
        holder.priceText.text = "${flight.price?.currency} ${flight.price?.total}"
        holder.airlineText.text = flight.validatingAirlineCodes?.joinToString() ?: "Multiple Airlines"
        holder.outboundRouteText.text = flight.getOutboundRoute()
        holder.returnRouteText.text = flight.getReturnRoute()
        holder.seatsText.text = "${flight.numberOfBookableSeats ?: 0} seats available"

        holder.cardView.setOnClickListener {
            onItemClick(flight)
        }
    }

    override fun getItemCount() = flights.size
}