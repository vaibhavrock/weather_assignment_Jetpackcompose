package com.example.demo.weatherapp.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.map
import com.example.demo.weatherapp.api.RetrofitClient
import com.example.demo.weatherapp.database.WeatherDao
import com.example.demo.weatherapp.database.WeatherEntity
import com.example.demo.weatherapp.model.Weather
import com.example.demo.weatherapp.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ResultMe<out T> {
    data class Success<out T>(val data: T) : ResultMe<T>()
    data class Error(val exception: Exception) : ResultMe<Nothing>()
}

class WeatherRepository(private val weatherDao: WeatherDao, private val context: Context) {
    suspend fun fetchWeatherData(stateCapitals: List<String>): ResultMe<List<Weather>> {
        // Check if there's internet connection
        if (isInternetAvailable()) {
            try {
                val weatherDataList = mutableListOf<Weather>()
                for (city in stateCapitals) {
                    val response = RetrofitClient.weatherService.getCurrentWeather(city, RetrofitClient.API_KEY)
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        weatherResponse?.let {
                            val weather = mapWeatherResponseToWeather(it)
                            // Check if the city already exists in the database
                            val existingWeather = weatherDao.getWeatherByCity(city)
                            if (existingWeather != null) {
                                // City already exists, update its data
                                updateWeatherData(existingWeather.id, weather)
                            } else {
                                // City doesn't exist, insert new data
                                saveWeatherToRoom(weather)
                            }
                            weatherDataList.add(weather)
                        }
                    } else {
                        return ResultMe.Error(Exception(response.message()))
                    }
                }
                return ResultMe.Success(weatherDataList)
            } catch (e: Exception) {
                return ResultMe.Error(e)
            }
        } else {
            // Fetch data from Room if there's no internet connection
            val offlineWeather = fetchOfflineWeather()
            return if (offlineWeather.isNotEmpty()) {
                ResultMe.Success(offlineWeather)
            } else {
                ResultMe.Error(Exception("No internet connection and No offline data available"))
            }
        }
    }

    private suspend fun updateWeatherData(id: Long, weather: Weather) {
        val weatherEntity = mapWeatherToEntity(weather)
        weatherEntity.id = id
        weatherDao.updateWeather(weatherEntity)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun saveWeatherToRoom(weather: Weather) {
        val weatherEntity = mapWeatherToEntity(weather)
        Log.d("Database", "Inserting weather data: $weatherEntity")
        weatherDao.insertWeather(weatherEntity)
    }

    private suspend fun fetchOfflineWeather(): List<Weather> {
        return withContext(Dispatchers.IO) {
            val weatherEntities = weatherDao.getAllWeather()
            weatherEntities.map { mapEntityToWeather(it) }
        }
    }

    private fun mapEntityToWeather(weatherEntity: WeatherEntity): Weather {
        return Weather(
            cityName = weatherEntity.cityName,
            state = weatherEntity.state,
            currentTemp = weatherEntity.currentTemp,
            feelsLike = weatherEntity.feelsLike,
            tempMin = weatherEntity.tempMin,
            tempMax = weatherEntity.tempMax,
            pressure = weatherEntity.pressure,
            humidity = weatherEntity.humidity,
            visibility = weatherEntity.visibility,
            windSpeed = weatherEntity.windSpeed,
            windDeg = weatherEntity.windDeg,
            clouds = weatherEntity.clouds,
            sunrise = weatherEntity.sunrise,
            sunset = weatherEntity.sunset,
            timestamp = weatherEntity.timestamp
        )
    }

    private fun mapWeatherToEntity(weather: Weather): WeatherEntity {
        return WeatherEntity(
            cityName = weather.cityName,
            state = weather.state,
            currentTemp = weather.currentTemp,
            feelsLike = weather.feelsLike,
            tempMin = weather.tempMin,
            tempMax = weather.tempMax,
            pressure = weather.pressure,
            humidity = weather.humidity,
            visibility = weather.visibility,
            windSpeed = weather.windSpeed,
            windDeg = weather.windDeg,
            clouds = weather.clouds,
            sunrise = weather.sunrise,
            sunset = weather.sunset,
            timestamp = weather.timestamp
        )
    }

    private fun mapWeatherResponseToWeather(weatherResponse: WeatherResponse): Weather {
        return Weather(
            cityName = weatherResponse.name,
            state = weatherResponse.sys.country,
            currentTemp = weatherResponse.main.temp,
            feelsLike = weatherResponse.main.feelsLike,
            tempMin = weatherResponse.main.tempMin,
            tempMax = weatherResponse.main.tempMax,
            pressure = weatherResponse.main.pressure,
            humidity = weatherResponse.main.humidity,
            visibility = weatherResponse.visibility,
            windSpeed = weatherResponse.wind.speed,
            windDeg = weatherResponse.wind.deg,
            clouds = weatherResponse.clouds.all,
            sunrise = weatherResponse.sys.sunrise,
            sunset = weatherResponse.sys.sunset,
            timestamp = weatherResponse.dt
        )
    }
}

