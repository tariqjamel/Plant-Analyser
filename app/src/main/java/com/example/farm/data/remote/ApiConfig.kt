package com.example.farm.data.remote

object ApiConfig {
    // Plant.id API Configuration
    const val PLANT_ID_BASE_URL = "https://api.plant.id/"
    const val PLANT_ID_API_KEY = "BB2RluFQ3DHxDNRiJsuCMozWJL2slDusc0EDMHtWnWWzq9GznP" // Your actual API key from Kindwise
    
    // Trefle API Configuration (Alternative for detailed plant info)
    const val TREFLE_BASE_URL = "https://trefle.io/api/v1/"
    const val TREFLE_API_KEY = "pEKzv-oGpISAOOa3Y2HbusHiMgdziXGOSrngqX60keM"
    
    // OpenWeatherMap API Configuration
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val WEATHER_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY_HERE" // Replace with your actual API key
    
    // API Headers
    const val PLANT_ID_API_KEY_HEADER = "Api-Key"
    const val TREFLE_API_KEY_HEADER = "Authorization"
    const val CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_MULTIPART = "multipart/form-data"
} 