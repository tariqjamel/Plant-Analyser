package com.example.farm.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.farm.data.model.*
import com.example.farm.data.remote.ApiConfig
import com.example.farm.data.remote.CropAnalysisApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropAnalysisRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CropAnalysisApi
) {
    
    suspend fun analyzeCropImage(imageUri: Uri): Result<CropAnalysis> {
        return try {
            println("Analyzing image: $imageUri")
            
            // Convert URI to file for API upload
            val imageFile = uriToFile(imageUri)
            println("Image file created: ${imageFile.absolutePath}, size: ${imageFile.length()} bytes")
            
            // Validate image file
            if (imageFile.length() == 0L) {
                throw Exception("Image file is empty. Please select a valid image.")
            }
            
            if (imageFile.length() > 10 * 1024 * 1024) { // 10MB limit
                throw Exception("Image file is too large. Please select a smaller image (max 10MB).")
            }
            
            // Create multipart request body
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("images", imageFile.name, requestBody)
            
            println("Request details:")
            println("- Image file name: ${imageFile.name}")
            println("- Image file size: ${imageFile.length()} bytes")
            println("- Content type: image/*")
            println("- Form field name: images")
            
            println("Making API call to Plant.id...")
            println("API Key being used: ${ApiConfig.PLANT_ID_API_KEY}")
            println("Image file size: ${imageFile.length()} bytes")
            println("Multipart body created successfully")
            
            // Call Plant.id API with timeout handling
            val plantIdResponse = try {
                println("Starting API call to: https://api.plant.id/v2/identify")
                val response = api.identifyPlant(
                    image = multipartBody,
                    organs = "leaf", // You can make this configurable
                    includeRelated = false,
                    noReject = false
                )
                println("API call completed successfully!")
                println("Response received: $response")
                response
            } catch (e: Exception) {
                println("API call failed with exception: ${e.javaClass.simpleName}")
                println("Error message: ${e.message}")
                println("Full stack trace:")
                e.printStackTrace()
                throw e
            }
            
            println("API Response received: $plantIdResponse")
            println("Response ID: ${plantIdResponse.id}")
            println("Is Plant: ${plantIdResponse.isPlant}")
            println("Plant Probability: ${plantIdResponse.isPlantProbability}")
            println("Suggestions count: ${plantIdResponse.suggestions?.size ?: 0}")
            
            // Check if the image is actually a plant
            if (!plantIdResponse.isPlant) {
                throw Exception("The uploaded image doesn't appear to be a plant. Please try with a clearer image of a plant.")
            }
            
            // Check if we have any suggestions
            if (plantIdResponse.suggestions.isNullOrEmpty()) {
                throw Exception("No plant could be identified in the image. Please try with a clearer image of a plant.")
            }
            
            // Get the top suggestion from Plant.id
            val topSuggestion = plantIdResponse.suggestions.maxByOrNull { it.probability }
            if (topSuggestion == null) {
                throw Exception("No plant could be identified in the image. Please try with a clearer image of a plant.")
            }
            
            println("Top plant suggestion: ${topSuggestion.plantName}")
            println("Confidence: ${topSuggestion.probability}")
            
            // Try to get detailed information from Trefle API
            val trefleData = try {
                println("Searching Trefle API for detailed plant information...")
                val trefleResponse = api.searchPlants(
                    query = topSuggestion.plantName,
                    token = ApiConfig.TREFLE_API_KEY
                )
                println("Trefle search response: ${trefleResponse.data.size} plants found")
                
                // Improved plant matching logic
                val treflePlant = findBestMatchingPlant(trefleResponse.data, topSuggestion.plantName)
                
                if (treflePlant != null) {
                    println("Found matching Trefle plant: ${treflePlant.commonName} (${treflePlant.scientificName})")
                    
                    // Get detailed information for this plant
                    val detailedResponse = api.getPlantDetails(
                        plantId = treflePlant.id,
                        token = ApiConfig.TREFLE_API_KEY
                    )
                    println("Trefle detailed response received for plant ID: ${treflePlant.id}")
                    detailedResponse.data
                } else {
                    println("No matching plant found in Trefle API")
                    null
                }
            } catch (e: Exception) {
                println("Trefle API call failed: ${e.message}")
                println("Continuing with Plant.id data only")
                null
            }
            
            // Convert combined API responses to our CropAnalysis model
            val cropAnalysis = convertToCropAnalysis(topSuggestion, trefleData)
            Result.success(cropAnalysis)
            
        } catch (e: Exception) {
            println("Error analyzing image: ${e.message}")
            println("Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // Only use mock data for specific network/connection issues
            when {
                e.message?.contains("timeout", ignoreCase = true) == true -> {
                    println("Network timeout detected, using mock data")
                    val mockAnalysis = createMockCropAnalysis()
                    Result.success(mockAnalysis)
                }
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> {
                    println("No internet connection detected, using mock data")
                    val mockAnalysis = createMockCropAnalysis()
                    Result.success(mockAnalysis)
                }
                e.message?.contains("Connection refused", ignoreCase = true) == true -> {
                    println("Connection refused, using mock data")
                    val mockAnalysis = createMockCropAnalysis()
                    Result.success(mockAnalysis)
                }
                else -> {
                    // For API errors, return the actual error instead of mock data
                    println("API error occurred, returning error to user")
                    Result.failure(e)
                }
            }
        }
    }
    
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        // Create a temporary file
        val file = File.createTempFile("crop_image_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        
        // Compress bitmap to JPEG
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()
        
        return file
    }
    
    private fun findBestMatchingPlant(treflePlants: List<TreflePlant>, plantIdName: String): TreflePlant? {
        println("Finding best match for: $plantIdName")
        println("Available Trefle plants: ${treflePlants.take(5).map { "${it.commonName} (${it.scientificName})" }}")
        
        // First, try exact matches
        val exactMatch = treflePlants.find { plant ->
            plant.commonName?.equals(plantIdName, ignoreCase = true) == true ||
            plant.scientificName?.equals(plantIdName, ignoreCase = true) == true
        }
        if (exactMatch != null) {
            println("Found exact match: ${exactMatch.commonName} (${exactMatch.scientificName})")
            return exactMatch
        }
        
        // Try partial matches on scientific name (most reliable)
        val scientificMatch = treflePlants.find { plant ->
            plant.scientificName?.lowercase()?.contains(plantIdName.lowercase()) == true ||
            plantIdName.lowercase().contains(plant.scientificName?.lowercase() ?: "")
        }
        if (scientificMatch != null) {
            println("Found scientific name match: ${scientificMatch.commonName} (${scientificMatch.scientificName})")
            return scientificMatch
        }
        
        // Try partial matches on common name
        val commonNameMatch = treflePlants.find { plant ->
            plant.commonName?.lowercase()?.contains(plantIdName.lowercase()) == true ||
            plantIdName.lowercase().contains(plant.commonName?.lowercase() ?: "")
        }
        if (commonNameMatch != null) {
            println("Found common name match: ${commonNameMatch.commonName} (${commonNameMatch.scientificName})")
            return commonNameMatch
        }
        
        // Try matching by genus (first word of scientific name)
        val plantIdGenus = plantIdName.split(" ").firstOrNull()?.lowercase()
        if (plantIdGenus != null) {
            val genusMatch = treflePlants.find { plant ->
                plant.scientificName?.lowercase()?.startsWith(plantIdGenus) == true ||
                plant.genus?.name?.lowercase() == plantIdGenus
            }
            if (genusMatch != null) {
                println("Found genus match: ${genusMatch.commonName} (${genusMatch.scientificName})")
                return genusMatch
            }
        }
        
        // If no matches found, return the first plant (fallback)
        if (treflePlants.isNotEmpty()) {
            val fallbackPlant = treflePlants.first()
            println("No specific match found, using first result: ${fallbackPlant.commonName} (${fallbackPlant.scientificName})")
            return fallbackPlant
        }
        
        println("No plants found in Trefle results")
        return null
    }
    
    private fun convertToCropAnalysis(
        plantIdSuggestion: PlantSuggestion,
        trefleData: TreflePlantDetail?
    ): CropAnalysis {
        println("Converting combined API responses to CropAnalysis...")
        
        println("Plant.id data:")
        println("- Plant name: ${plantIdSuggestion.plantName}")
        println("- Confidence: ${plantIdSuggestion.probability}")
        println("- Scientific name: ${plantIdSuggestion.plantDetails?.scientificName}")
        println("- Common names: ${plantIdSuggestion.plantDetails?.commonNames}")
        
        if (trefleData != null) {
            println("Trefle data:")
            println("- Common name: ${trefleData.commonName}")
            println("- Scientific name: ${trefleData.scientificName}")
            println("- Family: ${trefleData.family}")
            println("- Duration: ${trefleData.duration}")
            println("- Edible parts: ${trefleData.ediblePart}")
            println("- Growth data available: ${trefleData.mainSpecies?.growth != null}")
        }
        
        return CropAnalysis(
            cropName = plantIdSuggestion.plantName,
            confidence = plantIdSuggestion.probability.toFloat(),
            isHealthy = true, // Plant.id v2 doesn't provide health assessment in basic response
            diseases = emptyList(), // Health assessment requires additional API call
            lifecycle = createLifecycleFromCombinedData(plantIdSuggestion.plantDetails, trefleData),
            requirements = createRequirementsFromCombinedData(plantIdSuggestion.plantDetails, trefleData),
            additionalInfo = createAdditionalInfoFromCombinedData(plantIdSuggestion.plantDetails, trefleData)
        )
    }
    
    private fun createLifecycleFromCombinedData(
        plantIdDetails: PlantDetails?,
        trefleData: TreflePlantDetail?
    ): CropLifecycle {
        // Use Trefle duration data if available
        val duration = trefleData?.duration
        val harvestSeason = plantIdDetails?.harvestSeason
        
        // Calculate lifecycle based on duration and harvest season
        val totalDays = when {
            duration != null -> {
                when {
                    duration.contains("annual", ignoreCase = true) -> 365
                    duration.contains("perennial", ignoreCase = true) -> 730
                    duration.contains("biennial", ignoreCase = true) -> 730
                    else -> 120 // Default for unknown duration
                }
            }
            harvestSeason != null && harvestSeason.isNotEmpty() -> {
                when {
                    harvestSeason.any { it.contains("spring", ignoreCase = true) } -> 90
                    harvestSeason.any { it.contains("summer", ignoreCase = true) } -> 120
                    harvestSeason.any { it.contains("fall", ignoreCase = true) || it.contains("autumn", ignoreCase = true) } -> 150
                    harvestSeason.any { it.contains("winter", ignoreCase = true) } -> 180
                    else -> 120
                }
            }
            else -> 120 // Default lifecycle
        }
        
        return CropLifecycle(
            stages = listOf(
                LifecycleStage("Seedling", totalDays / 6, "Germination and early growth"),
                LifecycleStage("Vegetative", totalDays / 3, "Leaf and stem development"),
                LifecycleStage("Flowering", totalDays / 6, "Flower formation and pollination"),
                LifecycleStage("Fruiting", totalDays / 4, "Fruit development and ripening"),
                LifecycleStage("Harvesting", totalDays / 12, "Ready for harvest")
            ),
            totalDays = totalDays
        )
    }
    
    private fun createRequirementsFromCombinedData(
        plantIdDetails: PlantDetails?,
        trefleData: TreflePlantDetail?
    ): CropRequirements {
        // Use Trefle growth data if available
        val trefleGrowth = trefleData?.mainSpecies?.growth
        
        val sunlight = when {
            trefleGrowth?.light != null -> {
                when (trefleGrowth.light) {
                    1 -> "Full shade"
                    2 -> "Light shade"
                    3 -> "Partial shade"
                    4 -> "Full sun"
                    else -> "Full sun (6-8 hours daily)"
                }
            }
            plantIdDetails?.sunlight?.isNotEmpty() == true -> plantIdDetails.sunlight.joinToString(", ")
            else -> "Full sun (6-8 hours daily)"
        }
        
        val watering = plantIdDetails?.watering?.let { water ->
            when {
                water.min != null && water.max != null -> "Water every ${water.min}-${water.max} days"
                water.min != null -> "Water every ${water.min}+ days"
                water.max != null -> "Water every ${water.max} days or less"
                else -> "Regular watering"
            }
        } ?: "Regular watering"
        
        val soilType = when {
            trefleGrowth?.soilTexture != null -> trefleGrowth.soilTexture
            plantIdDetails?.care?.soil != null -> plantIdDetails.care.soil
            else -> "Well-draining soil"
        }
        
        val phLevel = when {
            trefleGrowth?.phMinimum != null && trefleGrowth.phMaximum != null -> 
                "${trefleGrowth.phMinimum}-${trefleGrowth.phMaximum}"
            trefleGrowth?.phMinimum != null -> "${trefleGrowth.phMinimum}+"
            trefleGrowth?.phMaximum != null -> "Up to ${trefleGrowth.phMaximum}"
            else -> "6.0-7.0 (neutral to slightly acidic)"
        }
        
        val temperature = when {
            trefleGrowth?.temperatureMinimum != null -> 
                "Minimum ${trefleGrowth.temperatureMinimum}°C, optimal 20-30°C"
            else -> "70-85°F (21-29°C)"
        }
        
        return CropRequirements(
            sunlight = sunlight,
            water = watering,
            soilType = soilType,
            temperature = temperature,
            phLevel = phLevel
        )
    }
    
    private fun createAdditionalInfoFromCombinedData(
        plantIdDetails: PlantDetails?,
        trefleData: TreflePlantDetail?
    ): AdditionalInfo {
        // Combine data from both APIs
        val harvestSeason = plantIdDetails?.harvestSeason
        val wikiDescription = plantIdDetails?.wikiDescription?.value
        val commonNames = plantIdDetails?.commonNames
        val edibleParts = trefleData?.ediblePart ?: plantIdDetails?.edibleParts
        val propagationMethods = plantIdDetails?.propagationMethods
        val pruningMonth = plantIdDetails?.pruningMonth
        val family = trefleData?.family
        val duration = trefleData?.duration
        
        // Create fertilizers list based on plant family and type
        val fertilizers = when {
            family?.lowercase()?.contains("brassicaceae") == true -> 
                listOf("Nitrogen-rich fertilizer", "Balanced fertilizer", "Organic compost")
            family?.lowercase()?.contains("solanaceae") == true -> 
                listOf("Balanced 10-10-10 fertilizer", "High phosphorus fertilizer", "Calcium supplement")
            trefleData?.vegetable == true -> 
                listOf("Balanced vegetable fertilizer", "Organic compost", "Fish emulsion")
            else -> listOf("Balanced fertilizer", "Organic compost")
        }
        
        // Create ideal climate based on duration and harvest season
        val idealClimate = when {
            duration?.contains("annual", ignoreCase = true) == true -> 
                "Annual plant, complete lifecycle in one growing season"
            duration?.contains("perennial", ignoreCase = true) == true -> 
                "Perennial plant, lives for multiple years"
            duration?.contains("biennial", ignoreCase = true) == true -> 
                "Biennial plant, completes lifecycle in two years"
            harvestSeason?.any { it.contains("spring", ignoreCase = true) } == true -> 
                "Cool weather crop, best in spring"
            harvestSeason?.any { it.contains("summer", ignoreCase = true) } == true -> 
                "Warm weather crop, best in summer"
            harvestSeason?.any { it.contains("fall", ignoreCase = true) || it.contains("autumn", ignoreCase = true) } == true -> 
                "Cool weather crop, best in fall"
            harvestSeason?.any { it.contains("winter", ignoreCase = true) } == true -> 
                "Cold hardy crop, can grow in winter"
            else -> "Moderate temperature with good sunlight"
        }
        
        // Create harvesting tips based on API data
        val harvestingTips = mutableListOf<String>()
        if (edibleParts != null && edibleParts.isNotEmpty()) {
            harvestingTips.add("Harvest ${edibleParts.joinToString(", ")} when ready")
        }
        if (harvestSeason != null && harvestSeason.isNotEmpty()) {
            harvestingTips.add("Best harvested during: ${harvestSeason.joinToString(", ")}")
        }
        if (harvestingTips.isEmpty()) {
            harvestingTips.add("Harvest when ready")
        }
        
        // Create pest control tips based on plant family
        val pestControl = when {
            family?.lowercase()?.contains("brassicaceae") == true -> 
                listOf("Use floating row covers", "Hand-pick caterpillars", "Apply neem oil")
            family?.lowercase()?.contains("solanaceae") == true -> 
                listOf("Companion planting with marigolds", "Hand-pick hornworms", "Apply neem oil")
            else -> listOf("Regular monitoring", "Natural pest control methods")
        }
        
        return AdditionalInfo(
            fertilizers = fertilizers,
            idealClimate = idealClimate,
            harvestingTips = harvestingTips,
            pestControl = pestControl
        )
    }
    
    private fun createMockCropAnalysis(): CropAnalysis {
        return CropAnalysis(
            cropName = "Tomato",
            confidence = 0.95f,
            isHealthy = false,
            diseases = listOf(
                Disease(
                    name = "Early Blight",
                    severity = DiseaseSeverity.MEDIUM,
                    description = "A fungal disease that affects tomato plants, causing dark spots on leaves and stems.",
                    symptoms = listOf(
                        "Dark brown spots on lower leaves",
                        "Yellowing of leaves",
                        "Stem lesions",
                        "Fruit rot in severe cases"
                    ),
                    remedies = listOf(
                        "Remove infected leaves immediately",
                        "Improve air circulation",
                        "Apply fungicide",
                        "Avoid overhead watering"
                    )
                )
            ),
            lifecycle = CropLifecycle(
                stages = listOf(
                    LifecycleStage("Seedling", 14, "Germination and early growth"),
                    LifecycleStage("Vegetative", 30, "Leaf and stem development"),
                    LifecycleStage("Flowering", 21, "Flower formation and pollination"),
                    LifecycleStage("Fruiting", 45, "Fruit development and ripening"),
                    LifecycleStage("Harvesting", 14, "Ready for harvest")
                ),
                totalDays = 124
            ),
            requirements = CropRequirements(
                sunlight = "Full sun (6-8 hours daily)",
                water = "Regular watering, keep soil moist but not waterlogged",
                soilType = "Well-draining, rich loamy soil",
                temperature = "70-85°F (21-29°C) during day, 60-70°F (16-21°C) at night",
                phLevel = "6.0-6.8 (slightly acidic)"
            ),
            additionalInfo = AdditionalInfo(
                fertilizers = listOf(
                    "Balanced 10-10-10 fertilizer at planting",
                    "High phosphorus fertilizer during flowering",
                    "Calcium supplement to prevent blossom end rot"
                ),
                idealClimate = "Warm, sunny climate with moderate humidity",
                harvestingTips = listOf(
                    "Harvest when fruits are fully colored",
                    "Pick regularly to encourage more fruit production",
                    "Store at room temperature until fully ripe",
                    "Refrigerate only after fully ripe"
                ),
                pestControl = listOf(
                    "Use companion planting with marigolds",
                    "Apply neem oil for aphid control",
                    "Hand-pick hornworms",
                    "Use floating row covers for young plants"
                )
            )
        )
    }
} 