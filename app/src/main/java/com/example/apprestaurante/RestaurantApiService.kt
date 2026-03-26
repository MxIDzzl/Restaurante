package com.example.apprestaurante

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface RestaurantApiService {
    @GET("restaurants")
    fun getRestaurants(@Header("Authorization") token: String): Call<JsonElement>
}
