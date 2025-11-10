package hk.hku.cs.swifttrip.adapter

import FlightOffer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import hk.hku.cs.swifttrip.R
import hk.hku.cs.swifttrip.utils.getOutboundRoute
import hk.hku.cs.swifttrip.utils.getReturnRoute

class FlightOfferAdapter(
    private val flights: List<FlightOffer>,
    private val onItemClick: (FlightOffer) -> Unit
) : RecyclerView.Adapter<FlightOfferAdapter.FlightOfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightOfferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flight, parent, false)
        return FlightOfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightOfferViewHolder, position: Int) {
        holder.bind(flights[position], onItemClick)
    }

    override fun getItemCount() = flights.size

    class FlightOfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.flightCard)
        private val priceText: TextView = itemView.findViewById(R.id.priceText)
        private val airlineText: TextView = itemView.findViewById(R.id.airlineText)
        private val outboundRouteText: TextView = itemView.findViewById(R.id.outboundRouteText)
        private val returnRouteText: TextView = itemView.findViewById(R.id.returnRouteText)
        private val seatsText: TextView = itemView.findViewById(R.id.seatsText)

        fun bind(flight: FlightOffer, onItemClick: (FlightOffer) -> Unit) {
            priceText.text = "${flight.price?.currency} ${flight.price?.total}"
            airlineText.text = flight.validatingAirlineCodes?.joinToString() ?: "Multiple Airlines"
            outboundRouteText.text = flight.getOutboundRoute()
            returnRouteText.text = flight.getReturnRoute()
            seatsText.text = "${flight.numberOfBookableSeats ?: 0} seats available"
            cardView.setOnClickListener { onItemClick(flight) }
        }
    }
}