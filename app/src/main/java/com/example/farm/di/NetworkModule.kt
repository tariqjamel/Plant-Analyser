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
            .baseUrl("https://api.plant.id/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Trefle")
    fun provideTrefleRetrofit(@Named("Trefle") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.TREFLE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("Weather")
    fun provideWeatherRetrofit(@Named("Weather") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
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
        val plantIdApi = plantIdRetrofit.create(CropAnalysisApi::class.java)
        val trefleApi = trefleRetrofit.create(CropAnalysisApi::class.java)
        
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
                throw UnsupportedOperationException("Weather API not yet implemented")
            }
        }
    }

    @Provides
    @Singleton
    @Named("Wikipedia")
    fun provideWikipediaOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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
    @Named("Wikipedia")
    fun provideWikipediaRetrofit(@Named("Wikipedia") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.WIKIPEDIA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWikipediaApi(@Named("Wikipedia") wikipediaRetrofit: Retrofit): com.example.farm.data.remote.WikipediaApi {
        return wikipediaRetrofit.create(com.example.farm.data.remote.WikipediaApi::class.java)
    }

    @Provides
    @Singleton
    @Named("OpenAI")
    fun provideOpenAIOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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
    @Named("OpenAI")
    fun provideOpenAIRetrofit(@Named("OpenAI") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApi(@Named("OpenAI") openAIRetrofit: Retrofit): com.example.farm.data.remote.OpenAIApi {
        return openAIRetrofit.create(com.example.farm.data.remote.OpenAIApi::class.java)
    }

    @Provides
    @Singleton
    @Named("Gemini")
    fun provideGeminiOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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
    @Named("Gemini")
    fun provideGeminiRetrofit(@Named("Gemini") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(@Named("Gemini") geminiRetrofit: Retrofit): com.example.farm.data.remote.GeminiApi {
        return geminiRetrofit.create(com.example.farm.data.remote.GeminiApi::class.java)
    }
} 