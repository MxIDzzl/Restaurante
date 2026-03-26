package com.example.apprestaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RestaurantAdapter(
    private val onRestaurantClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private val restaurants = mutableListOf<Restaurant>()

    fun submitList(newRestaurants: List<Restaurant>) {
        restaurants.clear()
        restaurants.addAll(newRestaurants)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount(): Int = restaurants.size

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        private val tvCuisine: TextView = itemView.findViewById(R.id.tvRestaurantCuisine)
        private val tvHours: TextView = itemView.findViewById(R.id.tvRestaurantHours)

        fun bind(restaurant: Restaurant) {
            tvName.text = restaurant.name
            tvCuisine.text = restaurant.cuisine
            tvHours.text = restaurant.attentionHours

            itemView.setOnClickListener {
                onRestaurantClick(restaurant)
            }
        }
    }
}
