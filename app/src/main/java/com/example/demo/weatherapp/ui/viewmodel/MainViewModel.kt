package com.example.demo.weatherapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo.weatherapp.model.Weather
import com.example.demo.weatherapp.repository.ResultMe
import com.example.demo.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weatherData = MutableLiveData<List<Weather>>(emptyList())
    val weatherData: LiveData<List<Weather>> = _weatherData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun refreshWeatherData(stateCapitals: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.fetchWeatherData(stateCapitals)) {
                is ResultMe.Success -> {
                    _weatherData.value = result.data
                    _errorMessage.value = null
                }
                is ResultMe.Error -> {
                    _errorMessage.value = result.exception.message ?: "Something went wrong. Please try again later."
                }
            }
            _isLoading.value = false
        }
    }
}

