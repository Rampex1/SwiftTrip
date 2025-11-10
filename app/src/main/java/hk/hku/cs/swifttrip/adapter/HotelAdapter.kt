package hk.hku.cs.swifttrip.adapter

import Hotel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hk.hku.cs.swifttrip.R

class HotelAdapter(private val hotels: List<Hotel>) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotels[position]
        holder.bind(hotel)
    }

    override fun getItemCount(): Int = hotels.size

    class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hotelNameText: TextView = itemView.findViewById(R.id.hotelNameText)
        private val hotelPriceText: TextView = itemView.findViewById(R.id.hotelPriceText)
        private val hotelLocationText: TextView = itemView.findViewById(R.id.hotelLocationText)
        private val hotelAvailabilityText: TextView = itemView.findViewById(R.id.hotelAvailabilityText)

        fun bind(hotel: Hotel) {
            hotelNameText.text = hotel.name
            hotelPriceText.text = hotel.price
            hotelLocationText.text = hotel.location
            hotelAvailabilityText.text = hotel.availability
        }
    }
}