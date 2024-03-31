package com.example.demo.weatherapp.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo.weatherapp.database.WeatherDao
import com.example.demo.weatherapp.database.WeatherDatabase
import com.example.demo.weatherapp.model.Weather
import com.example.demo.weatherapp.repository.WeatherRepository
import com.example.demo.weatherapp.ui.theme.WeatherAppTheme
import com.example.demo.weatherapp.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var weatherDao: WeatherDao
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instantiate WeatherDatabase
        val weatherDatabase = WeatherDatabase.getDatabase(applicationContext)
        // Get WeatherDao from WeatherDatabase
        weatherDao = weatherDatabase.weatherDao()
        // Pass WeatherDao to WeatherRepository
        val repository = WeatherRepository(weatherDao)
        viewModel = ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)

        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp(viewModel = viewModel)
                }
            }
        }
        val cities = listOf("Noida", "New York", "London")
        viewModel.refreshWeatherData(cities)
    }
}

class MainViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun WeatherApp(viewModel: MainViewModel) {
    val weatherList by viewModel.weatherData.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Weather for State Capitals",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            weatherList?.let { list ->
                items(list) { weather ->
                    WeatherItem(weather = weather)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun WeatherItem(weather: Weather) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(text = weather.cityName)
        Text(text = "State: ${weather.state}")
        Text(text = "Current Temp: ${weather.currentTemp} °C")
        Text(text = "High Temp: ${weather.tempMax} °C")
        Text(text = "Low Temp: ${weather.tempMin} °C")
        Text(text = "Updated at: ${weather.timestamp}")
    }
}