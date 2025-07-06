package com.example.farm.di

import com.example.farm.data.remote.ApiConfig
import com.example.farm.data.remote.CropAnalysisApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    @Named("PlantId")
    fun providePlantIdOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                println("Making Plant.id HTTP request to: ${original.url}")
                println("Request method: ${original.method}")
                println("Request headers: ${original.headers}")
                
                val requestBuilder = original.newBuilder()
                    .header("Api-Key", ApiConfig.PLANT_ID_API_KEY)
                    .method(original.method, original.body)
                
                val request = requestBuilder.build()
                println("Final Plant.id request URL: ${request.url}")
                println("Final Plant.id request headers: ${request.headers}")
                
                val response = chain.proceed(request)
                println("Plant.id response code: ${response.code}")
                println("Plant.id response message: ${response.message}")
                println("Plant.id response headers: ${response.headers}")
                
                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Trefle")
    fun provideTrefleOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                println("Making Trefle HTTP request to: ${original.url}")
                println("Request method: ${original.method}")
                println("Request headers: ${original.headers}")
                
                // Trefle v1 API uses token as query parameter, no Authorization header needed
                val request = original
                println("Final Trefle request URL: ${request.url}")
                println("Final Trefle request headers: ${request.headers}")
                
                val response = chain.proceed(request)
                println("Trefle response code: ${response.code}")
                println("Trefle response message: ${response.message}")
                println("Trefle response headers: ${response.headers}")
                
                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Weather")
    fun provideWeatherOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
    
    @Provides
    @Singleton
    @Named("PlantId")
    fun providePlantIdRetrofit(@Named("PlantId") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.plant.id/") // Plant.id API base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Trefle")
    fun provideTrefleRetrofit(@Named("Trefle") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.TREFLE_BASE_URL) // Trefle API base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Weather")
    fun provideWeatherRetrofit(@Named("Weather") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/") // OpenWeatherMap API
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCropAnalysisApi(
        @Named("PlantId") plantIdRetrofit: Retrofit,
        @Named("Trefle") trefleRetrofit: Retrofit
    ): CropAnalysisApi {
        // Create separate API instances for each service
        val plantIdApi = plantIdRetrofit.create(CropAnalysisApi::class.java)
        val trefleApi = trefleRetrofit.create(CropAnalysisApi::class.java)
        
        // Return a combined API that delegates to the appropriate service
        return object : CropAnalysisApi {
            override suspend fun identifyPlant(
                image: okhttp3.MultipartBody.Part,
                organs: String,
                includeRelated: Boolean,
                noReject: Boolean,
                details: String
            ): com.example.farm.data.model.PlantIdResponse {
                return plantIdApi.identifyPlant(image, organs, includeRelated, noReject, details)
            }
            
            override suspend fun searchPlants(
                query: String,
                token: String
            ): com.example.farm.data.model.TrefleResponse {
                return trefleApi.searchPlants(query, token)
            }
            
            override suspend fun getPlantDetails(
                plantId: Int,
                token: String
            ): com.example.farm.data.model.TreflePlantDetailResponse {
                return trefleApi.getPlantDetails(plantId, token)
            }
            
            override suspend fun getWeatherData(
                latitude: Double,
                longitude: Double,
                apiKey: String,
                units: String
            ): com.example.farm.data.model.WeatherResponse {
                // For now, return a mock weather response since we don't have weather API configured
                throw UnsupportedOperationException("Weather API not yet implemented")
            }
        }
    }
} 