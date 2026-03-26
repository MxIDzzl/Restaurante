package com.example.apprestaurante

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

data class Restaurant(
    val id: Int,
    val name: String,
    val cuisine: String,
    val attentionHours: String
)

interface RestaurantApiService {
    @GET("restaurants")
    fun getRestaurants(@Header("Authorization") token: String): Call<JsonElement>
}

class HomeActivity : AppCompatActivity() {

    private lateinit var rvRestaurants: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar

    private val restaurantsAdapter = RestaurantAdapter { restaurant ->
        val intent = Intent(this, RestaurantMenuActivity::class.java).apply {
            putExtra(RestaurantMenuActivity.EXTRA_RESTAURANT_ID, restaurant.id)
            putExtra(RestaurantMenuActivity.EXTRA_RESTAURANT_NAME, restaurant.name)
            putExtra(RestaurantMenuActivity.EXTRA_RESTAURANT_CUISINE, restaurant.cuisine)
            putExtra(RestaurantMenuActivity.EXTRA_RESTAURANT_HOURS, restaurant.attentionHours)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvRestaurants = findViewById(R.id.rvRestaurants)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)

        rvRestaurants.layoutManager = LinearLayoutManager(this)
        rvRestaurants.adapter = restaurantsAdapter

        loadRestaurants()
    }

    private fun loadRestaurants() {
        showLoading(true)

        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("token", null)
        if (token.isNullOrBlank()) {
            showLoading(false)
            showEmptyState("No hay sesión activa")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RestaurantApiService::class.java)
            .getRestaurants("Bearer $token")
            .enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    showLoading(false)

                    if (!response.isSuccessful || response.body() == null) {
                        showEmptyState("No se pudieron cargar los restaurantes")
                        return
                    }

                    val restaurantsPayload = extractRestaurantsPayload(response.body()!!)
                    val restaurants = restaurantsPayload
                        .mapIndexedNotNull { index, element -> parseRestaurant(element, index + 1) }

                    if (restaurants.isEmpty()) {
                        showEmptyState("No hay restaurantes disponibles")
                    } else {
                        tvEmptyState.visibility = View.GONE
                        rvRestaurants.visibility = View.VISIBLE
                        restaurantsAdapter.submitList(restaurants)
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    showLoading(false)
                    showEmptyState("Error de conexión")
                    Toast.makeText(this@HomeActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun parseRestaurant(element: JsonElement): Restaurant? {
        if (!element.isJsonObject) return null

        val json = element.asJsonObject

        val id = readInt(json, "id", "restaurant_id", "restaurantId") ?: fallbackId
        val name = readString(json, "name", "nombre", "restaurant_name") ?: "Restaurante #$id"
        val cuisine = readString(json, "cuisine", "tipo_cocina", "category", "categoria")
            ?: "Cocina internacional"
        val hours = readString(json, "attention_hours", "horario", "hours", "attentionHours")
            ?: "Lunes a Domingo: 12:00 - 23:00"

        return Restaurant(id, name, cuisine, hours)
    }

    private fun readString(json: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            if (!json.has(key) || json.get(key).isJsonNull) continue

            val value = json.get(key)
            if (!value.isJsonPrimitive) continue

            return try {
                value.asString
            } catch (_: UnsupportedOperationException) {
                null
            }
        }
        return null
    }

    private fun readInt(json: JsonObject, vararg keys: String): Int? {
        for (key in keys) {
            if (!json.has(key) || json.get(key).isJsonNull) continue

            val value = json.get(key)
            if (!value.isJsonPrimitive) continue

            val primitive = value.asJsonPrimitive

            if (primitive.isNumber) {
                return try {
                    primitive.asInt
                } catch (_: NumberFormatException) {
                    null
                }
            }

            if (primitive.isString) {
                return primitive.asString.toIntOrNull()
            }
        }
        return null
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvRestaurants.visibility = if (isLoading) View.GONE else rvRestaurants.visibility
    }

    private fun showEmptyState(message: String) {
        rvRestaurants.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = message
    }
}
