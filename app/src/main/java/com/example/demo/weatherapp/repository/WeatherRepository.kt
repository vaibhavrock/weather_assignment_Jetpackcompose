package com.example.demo.weatherapp.repository

import android.util.Log
import com.example.demo.weatherapp.api.RetrofitClient
import com.example.demo.weatherapp.database.WeatherDao
import com.example.demo.weatherapp.database.WeatherEntity
import com.example.demo.weatherapp.model.Weather
import com.example.demo.weatherapp.model.WeatherResponse

sealed class ResultMe<out T> {
    data class Success<out T>(val data: T) : ResultMe<T>()
    data class Error(val exception: Exception) : ResultMe<Nothing>()
}

class WeatherRepository(private val weatherDao: WeatherDao) {
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
                            weatherDataList.add(weather)
                            // Save data to Room
                            saveWeatherToRoom(weather)
                        }
                    } else {
                        Log.e("API_REQUEST", "Error fetching weather data for $city: ${response.message()}")
                    }
                }
                return ResultMe.Success(weatherDataList)
            } catch (e: Exception) {
                Log.e("API_REQUEST", "Exception fetching weather data: ${e.message}", e)
                return ResultMe.Error(e)
            }
        } else {
            // Fetch data from Room if there's no internet connection
            val offlineWeather = fetchOfflineWeather()
            return if (offlineWeather.isNotEmpty()) {
                ResultMe.Success(offlineWeather)
            } else {
                ResultMe.Error(Exception("No internet connection and no offline data available"))
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        //val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //val networkInfo = connectivityManager.activeNetworkInfo
        //return networkInfo != null && networkInfo.isConnected
        return true
    }

    private suspend fun saveWeatherToRoom(weather: Weather) {
        val weatherEntity = mapWeatherToEntity(weather)
        weatherDao.insertWeather(weatherEntity)
    }

    private fun fetchOfflineWeather(): List<Weather> {
        val weatherEntities = weatherDao.getAllWeather().value ?: emptyList()
        return weatherEntities.map { mapEntityToWeather(it) }
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

