package com.example.demo.weatherapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val state: String,
    val currentTemp: Double,
    val highTemp: Double,
    val lowTemp: Double,
    val precipitation: Double,
    val timestamp: Long
)
