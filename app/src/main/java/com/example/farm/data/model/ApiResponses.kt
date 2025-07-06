package com.example.farm.data.model

import com.google.gson.annotations.SerializedName

// Plant.id API v2 Response (actual format based on logs)
data class PlantIdResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("custom_id")
    val customId: String?,
    @SerializedName("meta_data")
    val metaData: MetaData?,
    @SerializedName("uploaded_datetime")
    val uploadedDatetime: Double,
    @SerializedName("finished_datetime")
    val finishedDatetime: Double,
    @SerializedName("images")
    val images: List<PlantImage>?,
    @SerializedName("suggestions")
    val suggestions: List<PlantSuggestion>?,
    @SerializedName("modifiers")
    val modifiers: List<String>?,
    @SerializedName("secret")
    val secret: String?,
    @SerializedName("fail_cause")
    val failCause: String?,
    @SerializedName("countable")
    val countable: Boolean,
    @SerializedName("feedback")
    val feedback: String?,
    @SerializedName("is_plant")
    val isPlant: Boolean,
    @SerializedName("is_plant_probability")
    val isPlantProbability: Double
)

data class MetaData(
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("datetime")
    val datetime: String?
)

data class PlantImage(
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("url")
    val url: String
)

data class PlantSuggestion(
    @SerializedName("id")
    val id: Long,
    @SerializedName("plant_name")
    val plantName: String,
    @SerializedName("probability")
    val probability: Double,
    @SerializedName("confirmed")
    val confirmed: Boolean,
    @SerializedName("plant_details")
    val plantDetails: PlantDetails?
)

data class PlantDetails(
    @SerializedName("language")
    val language: String,
    @SerializedName("scientific_name")
    val scientificName: String,
    @SerializedName("structured_name")
    val structuredName: StructuredName?,
    @SerializedName("common_names")
    val commonNames: List<String>?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("wiki_description")
    val wikiDescription: WikiDescription?,
    @SerializedName("taxonomy")
    val taxonomy: Taxonomy?,
    @SerializedName("synonyms")
    val synonyms: List<String>?,
    @SerializedName("edible_parts")
    val edibleParts: List<String>?,
    @SerializedName("watering")
    val watering: Watering?,
    @SerializedName("propagation_methods")
    val propagationMethods: List<String>?,
    @SerializedName("sunlight")
    val sunlight: List<String>?,
    @SerializedName("pruning_month")
    val pruningMonth: List<String>?,
    @SerializedName("harvest_season")
    val harvestSeason: List<String>?,
    @SerializedName("care")
    val care: Care?
)

data class StructuredName(
    @SerializedName("genus")
    val genus: String,
    @SerializedName("species")
    val species: String
)

data class WikiDescription(
    @SerializedName("value")
    val value: String,
    @SerializedName("citation")
    val citation: String?
)

data class Taxonomy(
    @SerializedName("class")
    val className: String?,
    @SerializedName("family")
    val family: String?,
    @SerializedName("genus")
    val genus: String?,
    @SerializedName("kingdom")
    val kingdom: String?,
    @SerializedName("order")
    val order: String?,
    @SerializedName("phylum")
    val phylum: String?
)

data class Watering(
    @SerializedName("max")
    val max: Int?,
    @SerializedName("min")
    val min: Int?
)

data class Care(
    @SerializedName("pruning")
    val pruning: String?,
    @SerializedName("soil")
    val soil: String?
)

data class SimilarImage(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("license_name")
    val licenseName: String?,
    @SerializedName("license_url")
    val licenseUrl: String?
)

// Weather API Response
data class WeatherResponse(
    @SerializedName("main")
    val main: WeatherMain,
    @SerializedName("weather")
    val weather: List<WeatherDescription>,
    @SerializedName("wind")
    val wind: Wind?,
    @SerializedName("sys")
    val sys: Sys?
)

data class WeatherMain(
    @SerializedName("temp")
    val temperature: Double,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("pressure")
    val pressure: Int
)

data class WeatherDescription(
    @SerializedName("main")
    val main: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
)

data class Wind(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("deg")
    val degree: Int?
)

data class Sys(
    @SerializedName("country")
    val country: String?,
    @SerializedName("sunrise")
    val sunrise: Long?,
    @SerializedName("sunset")
    val sunset: Long?
) 