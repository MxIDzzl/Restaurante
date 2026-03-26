package com.example.apprestaurante   // ⚠️ CAMBIA si tu package es diferente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        btnLogin.setOnClickListener {
            loginUser()
        }
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {

        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        val loginRequest = LoginRequest(email, password)

        api.login(loginRequest).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

                if (response.isSuccessful) {

                    val token = response.body()?.access_token

                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().putString("token", token).apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Login correcto",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Credenciales incorrectas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String
)

interface ApiService {

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}
