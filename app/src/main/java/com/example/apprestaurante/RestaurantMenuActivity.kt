package com.example.apprestaurante

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class RestaurantMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        val restaurantName = intent.getStringExtra(EXTRA_RESTAURANT_NAME) ?: "Restaurante"
        val restaurantCuisine = intent.getStringExtra(EXTRA_RESTAURANT_CUISINE) ?: "Cocina"
        val restaurantHours = intent.getStringExtra(EXTRA_RESTAURANT_HOURS) ?: "Lunes a Domingo: 12:00 - 23:00"

        findViewById<TextView>(R.id.tvRestaurantTitle).text = restaurantName
        findViewById<TextView>(R.id.tvRestaurantCuisine).text = restaurantCuisine
        findViewById<TextView>(R.id.tvHoursDetail).text = restaurantHours

        findViewById<MaterialCardView>(R.id.cardNewReservation).setOnClickListener {
            // Aquí puede navegarse a una pantalla de crear reserva por restaurante.
        }

        findViewById<MaterialCardView>(R.id.cardMyReservations).setOnClickListener {
            // Aquí puede navegarse a una pantalla de reservas del restaurante seleccionado.
        }
    }

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
        const val EXTRA_RESTAURANT_CUISINE = "extra_restaurant_cuisine"
        const val EXTRA_RESTAURANT_HOURS = "extra_restaurant_hours"
    }
}
