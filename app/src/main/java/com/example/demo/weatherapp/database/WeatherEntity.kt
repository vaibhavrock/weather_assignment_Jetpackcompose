package com.example.demo.weatherapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val state: String,
    val currentTemp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val visibility: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val clouds: Int,
    val sunrise: Long,
    val sunset: Long,
    val timestamp: Long
)

