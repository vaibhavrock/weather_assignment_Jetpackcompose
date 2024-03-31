package com.example.demo.weatherapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //https://api.openweathermap.org/data/2.5/weather?q=Noida&appid=fae7190d7e6433ec3a45285ffcf55c86
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_KEY = "fae7190d7e6433ec3a45285ffcf55c86"

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherService: WeatherService by lazy {
        retrofit.create(WeatherService::class.java)
    }

}
