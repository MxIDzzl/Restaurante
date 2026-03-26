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
import com.google.gson.JsonPrimitive
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

                    val restaurants = extractRestaurants(response.body()!!)

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
                    Toast.makeText(this@HomeActivity, t.message ?: "Error inesperado", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun extractRestaurants(root: JsonElement): List<Restaurant> {
        val sourceArray: JsonArray? = when {
            root.isJsonArray -> root.asJsonArray
            root.isJsonObject -> findRestaurantArray(root.asJsonObject)
            else -> null
        }

        if (sourceArray == null) return emptyList()

        return sourceArray.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            parseRestaurant(element.asJsonObject)
        }
    }

    private fun findRestaurantArray(json: JsonObject): JsonArray? {
        val arrayKeys = listOf("restaurants", "data", "results", "items")
        for (key in arrayKeys) {
            if (json.has(key) && json.get(key).isJsonArray) {
                return json.getAsJsonArray(key)
            }
        }

        val objectKeys = listOf("data", "result", "payload")
        for (key in objectKeys) {
            if (!json.has(key) || !json.get(key).isJsonObject) continue
            val nestedArray = findRestaurantArray(json.getAsJsonObject(key))
            if (nestedArray != null) return nestedArray
        }

        return null
    }

    private fun parseRestaurant(json: JsonObject): Restaurant? {
        val id = readInt(json, "id", "restaurant_id") ?: return null
        val name = readString(json, "name", "nombre", "restaurant_name") ?: "Restaurante #$id"
        val cuisine = readString(json, "cuisine", "tipo_cocina", "category", "categoria") ?: "Cocina internacional"
        val hours = readString(json, "attention_hours", "horario", "hours") ?: "Lunes a Domingo: 12:00 - 23:00"

        return Restaurant(id, name, cuisine, hours)
    }

    private fun readString(json: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            if (json.has(key) && !json.get(key).isJsonNull) {
                val value = json.get(key)
                if (value.isJsonPrimitive) {
                    val primitive: JsonPrimitive = value.asJsonPrimitive
                    return when {
                        primitive.isString -> primitive.asString
                        primitive.isNumber -> primitive.asNumber.toString()
                        primitive.isBoolean -> primitive.asBoolean.toString()
                        else -> null
                    }
                }
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
            if (primitive.isNumber) return primitive.asInt
            if (primitive.isString) return primitive.asString.toIntOrNull()
        }
        return null
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) rvRestaurants.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        rvRestaurants.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = message
    }
}
