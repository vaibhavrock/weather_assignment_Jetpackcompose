package com.example.demo.weatherapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    fun getAllWeather(): List<WeatherEntity>

    @Query("SELECT * FROM weather WHERE cityName = :city")
    suspend fun getWeatherByCity(city: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Update
    suspend fun updateWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather")
    suspend fun deleteAllWeather()
}