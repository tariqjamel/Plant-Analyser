package com.example.farm.data.remote

import com.example.farm.data.model.CropAnalysis
import com.example.farm.data.model.PlantIdResponse
import com.example.farm.data.model.TrefleResponse
import com.example.farm.data.model.TreflePlantDetailResponse
import com.example.farm.data.model.WeatherResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface CropAnalysisApi {
    
    // Plant.id API endpoint with detailed information
    @Multipart
    @POST("v2/identify")
    suspend fun identifyPlant(
        @Part image: MultipartBody.Part,
        @Part("organs") organs: String = "leaf",
        @Part("include_related_images") includeRelated: Boolean = false,
        @Part("no_reject") noReject: Boolean = false,
        @Part("details") details: String = "common_names,url,wiki_description,taxonomy,synonyms,edible_parts,watering,propagation_methods,sunlight,pruning_month,harvest_season,care"
    ): PlantIdResponse
    
    @GET("plants")
    suspend fun searchPlants(
        @Query("q") query: String,
        @Query("token") token: String
    ): TrefleResponse
    
    @GET("plants/{id}")
    suspend fun getPlantDetails(
        @Path("id") plantId: Int,
        @Query("token") token: String
    ): TreflePlantDetailResponse
    
    // Weather API endpoint for crop recommendations
    @GET("weather")
    suspend fun getWeatherData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
} 