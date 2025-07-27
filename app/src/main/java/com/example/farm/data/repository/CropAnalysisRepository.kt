package com.example.farm.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.farm.data.model.*
import com.example.farm.data.remote.ApiConfig
import com.example.farm.data.remote.CropAnalysisApi
import com.example.farm.data.remote.OpenAIApi
import com.example.farm.data.remote.OpenAIChatRequest
import com.example.farm.data.remote.OpenAIMessage
import com.example.farm.data.remote.WikipediaApi
import com.example.farm.data.remote.WikipediaSummaryResponse
import com.google.gson.JsonParser
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

    private val api: CropAnalysisApi,
    private val wikipediaApi: WikipediaApi,
    private val openAIApi: OpenAIApi,
    private val geminiApi: com.example.farm.data.remote.GeminiApi // Inject GeminiApi
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
                    organs = "leaf",
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
                throw Exception("No plant could be identified.")
            }
            val plantName = topSuggestion.plantName

            println("Plant name extracted for Gemini: $plantName")
            // Gemini API: Fetch plant details as summary
            var aiDetails: String? = null
            var plantInfoSections: PlantInfoSections? = null
            try {
                val geminiPrompt = """
                    Provide detailed information about the plant '$plantName' in the following format:

                    **LIFECYCLE:**
                    [Provide detailed information about the plant's growth cycle, stages, and timeline]

                    **CARE REQUIREMENTS:**
                    [Provide detailed care instructions including watering, sunlight, soil, and maintenance needs]

                    **IDEAL CLIMATE:**
                    [Provide information about temperature, humidity, and climate preferences]

                    **INTERESTING FACTS:**
                    [Provide 3-5 interesting facts about this plant]

                    **SUMMARY:**
                    [Provide a brief overview of the plant]

                    Make sure to provide comprehensive, accurate information for each section.
                """.trimIndent()
                
                println("Gemini prompt: $geminiPrompt")
                val geminiRequest = com.example.farm.data.remote.GeminiRequest(
                    contents = listOf(
                        com.example.farm.data.remote.GeminiContent(
                            parts = listOf(com.example.farm.data.remote.GeminiContentPart(text = geminiPrompt))
                        )
                    )
                )
                val geminiResponse = geminiApi.generateContent(
                    apiKey = "YOUR_GEMINI_API_KEY",
                    request = geminiRequest
                )
                val rawGeminiText = geminiResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                println("Raw Gemini response for $plantName: $rawGeminiText")
                aiDetails = rawGeminiText
                
                // Parse the response into sections
                if (!rawGeminiText.isNullOrBlank()) {
                    try {
                        plantInfoSections = parsePlantInfoSections(rawGeminiText)
                        println("Successfully parsed plant info sections")
                    } catch (e: Exception) {
                        println("Failed to parse plant info sections: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Gemini summary extraction failed: ${e.message}")
            }

            // Fallback: Use Wikipedia summary if Gemini fails
            var wikiSummary: WikipediaSummaryResponse? = null
            var fallbackDetails: String? = null
            var usedFallback = false
            if (aiDetails.isNullOrBlank()) {
                usedFallback = true
                try {
                    val wikiResponse = wikipediaApi.getSummary(plantName.replace(" ", "_")).body()
                    wikiSummary = wikiResponse
                    fallbackDetails = wikiSummary?.extract
                    println("Fallback to Wikipedia for $plantName: $fallbackDetails")
                } catch (e: Exception) {
                    println("Wikipedia fallback extraction failed: ${e.message}")
                }
            }
            val description = aiDetails ?: fallbackDetails ?: "No description available."
            println("Final detail shown to user for $plantName: $description")

            // -Improved keyword extraction for lifecycle and requirements
            fun parseLifecycleFromText(text: String): Pair<String, Int> {
                val lower = text.lowercase()
                return when {
                    "annual" in lower -> "Annual" to 365
                    "biennial" in lower -> "Biennial" to 730
                    "perennial" in lower -> "Perennial" to 1095
                    "germination" in lower && "harvest" in lower -> "Full cycle" to 120
                    else -> "Not available" to -1
                }
            }
            fun parseRequirementsFromText(text: String): CropRequirements {
                val lower = text.lowercase()
                val sunlight = when {
                    "full sun" in lower -> "Full sun"
                    "partial shade" in lower -> "Partial shade"
                    "shade" in lower -> "Shade"
                    else -> "Not available"
                }
                val water = when {
                    "moist soil" in lower -> "Keep soil moist"
                    "dry soil" in lower -> "Allow soil to dry between waterings"
                    "water regularly" in lower -> "Water regularly"
                    else -> "Not available"
                }
                val soilType = when {
                    "loamy" in lower -> "Loamy"
                    "sandy" in lower -> "Sandy"
                    "clay" in lower -> "Clay"
                    "well-drained" in lower -> "Well-drained"
                    else -> "Not available"
                }
                val temperature = if ("temperature" in lower) "See Wikipedia summary" else "Not available"
                val phLevel = if ("ph" in lower) "See Wikipedia summary" else "Not available"
                return CropRequirements(sunlight, water, soilType, temperature, phLevel)
            }
            fun parseAdditionalInfoFromText(text: String): AdditionalInfo {
                val lower = text.lowercase()
                val fertilizers = if ("fertilizer" in lower) listOf("See Wikipedia summary") else listOf()
                val idealClimate = if ("climate" in lower) "See Wikipedia summary" else ""
                val harvestingTips = if ("harvest" in lower) listOf("See Wikipedia summary") else listOf()
                val pestControl = if ("pest" in lower) listOf("See Wikipedia summary") else listOf()
                return AdditionalInfo(fertilizers, idealClimate, harvestingTips, pestControl)
            }

            // Debug: Print Wikipedia summary
            println("Wikipedia summary for $plantName: $description")

            var extractedLifecycle: CropLifecycle? = null
            var extractedRequirements: CropRequirements? = null
            var extractedAdditionalInfo: AdditionalInfo? = null
            if (description.isNotBlank() && description != "No description available.") {
                try {
                    val prompt = """
                        Extract the lifecycle, care requirements, and additional information for the plant '$plantName' from the following text. Return the result as JSON with fields: lifecycle (object with type and totalDays), requirements (object with sunlight, water, soilType, temperature, phLevel), additionalInfo (object with fertilizers, idealClimate, harvestingTips, pestControl).
                        Text: $description
                    """.trimIndent()
                    println("OpenAI prompt: $prompt")
                    val openAIRequest = OpenAIChatRequest(
                        messages = listOf(OpenAIMessage(content = prompt)),
                        max_tokens = 512
                    )
                    val openAIResponse = openAIApi.getChatCompletion(openAIRequest)
                    val aiContent = openAIResponse.body()?.choices?.firstOrNull()?.message?.content
                    println("OpenAI response: $aiContent")
                    if (aiContent != null && aiContent.contains("lifecycle")) {
                        val json = JsonParser.parseString(aiContent).asJsonObject
                        val lifecycleJson = json["lifecycle"].asJsonObject
                        val type = lifecycleJson["type"].asString
                        val totalDays = lifecycleJson["totalDays"].asInt
                        println("Parsed from OpenAI: type=$type, totalDays=$totalDays")
                        extractedLifecycle = if (totalDays > 0) CropLifecycle(
                            stages = listOf(
                                LifecycleStage("Seedling", totalDays / 6, "Germination and early growth"),
                                LifecycleStage("Vegetative", totalDays / 3, "Leaf and stem development"),
                                LifecycleStage("Flowering", totalDays / 6, "Flower formation and pollination"),
                                LifecycleStage("Fruiting", totalDays / 4, "Fruit development and ripening"),
                                LifecycleStage("Harvesting", totalDays / 12, "Ready for harvest")
                            ),
                            totalDays = totalDays
                        ) else CropLifecycle(stages = listOf(), totalDays = -1)
                        val reqJson = json["requirements"].asJsonObject
                        extractedRequirements = CropRequirements(
                            sunlight = reqJson["sunlight"].asString,
                            water = reqJson["water"].asString,
                            soilType = reqJson["soilType"].asString,
                            temperature = reqJson["temperature"].asString,
                            phLevel = reqJson["phLevel"].asString
                        )
                        val addJson = json["additionalInfo"].asJsonObject
                        extractedAdditionalInfo = AdditionalInfo(
                            fertilizers = addJson["fertilizers"].asJsonArray.mapNotNull { it?.asString },
                            idealClimate = addJson["idealClimate"].asString,
                            harvestingTips = addJson["harvestingTips"].asJsonArray.mapNotNull { it?.asString },
                            pestControl = addJson["pestControl"].asJsonArray.mapNotNull { it?.asString }
                        )
                    }
                } catch (e: Exception) {
                    println("OpenAI extraction failed: ${e.message}")
                }
            }
            // Fallback to improved parsing if OpenAI or Wikipedia fails
            val lifecycle: CropLifecycle = extractedLifecycle ?: run {
                val (type, totalDays) = parseLifecycleFromText(description)
                println("Parsed from Wikipedia: type=$type, totalDays=$totalDays")
                if (totalDays > 0) CropLifecycle(
                    stages = listOf(
                        LifecycleStage("Seedling", totalDays / 6, "Germination and early growth"),
                        LifecycleStage("Vegetative", totalDays / 3, "Leaf and stem development"),
                        LifecycleStage("Flowering", totalDays / 6, "Flower formation and pollination"),
                        LifecycleStage("Fruiting", totalDays / 4, "Fruit development and ripening"),
                        LifecycleStage("Harvesting", totalDays / 12, "Ready for harvest")
                    ),
                    totalDays = totalDays
                ) else {
                    // Use default values for common crops
                    val cropDefaults = mapOf(
                        "tomato" to 120,
                        "wheat" to 120,
                        "rice" to 150,
                        "maize" to 120,
                        "corn" to 120,
                        "potato" to 110,
                        "soybean" to 100,
                        "cucumber" to 60,
                        "carrot" to 75,
                        "onion" to 120,
                        "lettuce" to 65
                    )
                    val defaultDays = cropDefaults.entries.firstOrNull { plantName.lowercase().contains(it.key) }?.value
                    if (defaultDays != null) {
                        println("Using default lifecycle for $plantName: $defaultDays days")
                        CropLifecycle(
                            stages = listOf(
                                LifecycleStage("Seedling", defaultDays / 6, "Germination and early growth"),
                                LifecycleStage("Vegetative", defaultDays / 3, "Leaf and stem development"),
                                LifecycleStage("Flowering", defaultDays / 6, "Flower formation and pollination"),
                                LifecycleStage("Fruiting", defaultDays / 4, "Fruit development and ripening"),
                                LifecycleStage("Harvesting", defaultDays / 12, "Ready for harvest")
                            ),
                            totalDays = defaultDays
                        )
                    } else {
                        println("No lifecycle found for $plantName, using fallback 120 days")
                        CropLifecycle(
                            stages = listOf(
                                LifecycleStage("Seedling", 20, "Germination and early growth"),
                                LifecycleStage("Vegetative", 40, "Leaf and stem development"),
                                LifecycleStage("Flowering", 20, "Flower formation and pollination"),
                                LifecycleStage("Fruiting", 30, "Fruit development and ripening"),
                                LifecycleStage("Harvesting", 10, "Ready for harvest")
                            ),
                            totalDays = 120
                        )
                    }
                }
            }
            val requirements: CropRequirements = extractedRequirements ?: parseRequirementsFromText(description)
            val additionalInfo: AdditionalInfo = extractedAdditionalInfo ?: parseAdditionalInfoFromText(description)

            val isGeneric = lifecycle.totalDays == -1 || requirements.sunlight == "Not available" || requirements.water == "Not available" || requirements.soilType == "Not available" || requirements.temperature == "Not available" || requirements.phLevel == "Not available" || additionalInfo.fertilizers.isEmpty() || additionalInfo.idealClimate == "" || additionalInfo.harvestingTips.isEmpty() || additionalInfo.pestControl.isEmpty()
            val cropAnalysis = CropAnalysis(
                cropName = plantName,
                confidence = topSuggestion.probability.toFloat(),
                isHealthy = true,
                diseases = listOf(),
                lifecycle = lifecycle,
                requirements = requirements,
                additionalInfo = additionalInfo,
                aiDetails = description,
                plantInfoSections = plantInfoSections,
                isGeneric = isGeneric
            )
            return Result.success(cropAnalysis)
            
        } catch (e: Exception) {
            println("Error analyzing image: ${e.message}")
            println("Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
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
        
        // Try partial matches on scientific name
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
            else -> 120
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
    
    private fun parsePlantInfoSections(response: String): PlantInfoSections {
        return try {
            val sections = mutableMapOf<String, String>()
            
            // Define the section headers we're looking for
            val sectionHeaders = listOf(
                "**LIFECYCLE:**",
                "**CARE REQUIREMENTS:**", 
                "**IDEAL CLIMATE:**",
                "**INTERESTING FACTS:**",
                "**SUMMARY:**"
            )
            
            var currentSection = ""
            var currentContent = StringBuilder()
            
            // Split the response into lines
            val lines = response.split("\n")
            
            for (line in lines) {
                val trimmedLine = line.trim()
                
                // Check if this line is a section header
                val matchingHeader = sectionHeaders.find { header ->
                    trimmedLine.startsWith(header)
                }
                
                if (matchingHeader != null) {
                    // Save the previous section if we have one
                    if (currentSection.isNotEmpty() && currentContent.isNotEmpty()) {
                        sections[currentSection] = currentContent.toString().trim()
                    }
                    
                    // Start new section
                    currentSection = matchingHeader
                    currentContent = StringBuilder()
                } else if (currentSection.isNotEmpty() && trimmedLine.isNotEmpty()) {
                    // Add content to current section
                    if (currentContent.isNotEmpty()) {
                        currentContent.append("\n")
                    }
                    currentContent.append(trimmedLine)
                }
            }
            
            // Save the last section
            if (currentSection.isNotEmpty() && currentContent.isNotEmpty()) {
                sections[currentSection] = currentContent.toString().trim()
            }
            
            PlantInfoSections(
                lifecycle = sections["**LIFECYCLE:**"] ?: "",
                careRequirements = sections["**CARE REQUIREMENTS:**"] ?: "",
                idealClimate = sections["**IDEAL CLIMATE:**"] ?: "",
                interestingFacts = sections["**INTERESTING FACTS:**"] ?: "",
                summary = sections["**SUMMARY:**"] ?: ""
            )
        } catch (e: Exception) {
            println("Error parsing plant info sections: ${e.message}")
            PlantInfoSections()
        }
    }
} 
