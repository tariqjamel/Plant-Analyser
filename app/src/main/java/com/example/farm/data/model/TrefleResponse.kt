package com.example.farm.data.model

import com.google.gson.annotations.SerializedName

data class TrefleResponse(
    @SerializedName("data")
    val data: List<TreflePlant>
)

data class TreflePlant(
    @SerializedName("id")
    val id: Int,
    @SerializedName("common_name")
    val commonName: String?,
    @SerializedName("scientific_name")
    val scientificName: String?,
    @SerializedName("family")
    val family: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("bibliography")
    val bibliography: String?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("genus")
    val genus: TrefleGenus?,
    @SerializedName("observations")
    val observations: String?,
    @SerializedName("vegetable")
    val vegetable: Boolean?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("edible_part")
    val ediblePart: List<String>?,
    @SerializedName("edible")
    val edible: Boolean?
)

data class TrefleGenus(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("slug")
    val slug: String?
)

data class TreflePlantDetailResponse(
    @SerializedName("data")
    val data: TreflePlantDetail
)

data class TreflePlantDetail(
    @SerializedName("id")
    val id: Int,
    @SerializedName("common_name")
    val commonName: String?,
    @SerializedName("scientific_name")
    val scientificName: String?,
    @SerializedName("family")
    val family: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("bibliography")
    val bibliography: String?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("genus")
    val genus: TrefleGenus?,
    @SerializedName("observations")
    val observations: String?,
    @SerializedName("vegetable")
    val vegetable: Boolean?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("edible_part")
    val ediblePart: List<String>?,
    @SerializedName("edible")
    val edible: Boolean?,
    @SerializedName("main_species")
    val mainSpecies: TrefleSpecies?
)

data class TrefleSpecies(
    @SerializedName("growth")
    val growth: TrefleGrowth?
)

data class TrefleGrowth(
    @SerializedName("light")
    val light: Int?,
    @SerializedName("atmospheric_humidity")
    val atmosphericHumidity: Int?,
    @SerializedName("soil_nutriments")
    val soilNutriments: Int?,
    @SerializedName("soil_salinity")
    val soilSalinity: Int?,
    @SerializedName("soil_texture")
    val soilTexture: String?,
    @SerializedName("soil_humidity")
    val soilHumidity: Int?,
    @SerializedName("ph_maximum")
    val phMaximum: Double?,
    @SerializedName("ph_minimum")
    val phMinimum: Double?,
    @SerializedName("temperature_minimum")
    val temperatureMinimum: Double?
) 