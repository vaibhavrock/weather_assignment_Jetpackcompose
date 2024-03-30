package com.example.demo.weatherapp.model

data class Weather(
    val cityName: String,
    val state: String,
    val currentTemp: Double,
    val highTemp: Double,
    val lowTemp: Double,
    val precipitation: Double,
    val timestamp: Long
)
