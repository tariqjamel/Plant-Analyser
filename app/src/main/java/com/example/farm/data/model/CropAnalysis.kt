package com.example.farm.data.model

import androidx.annotation.DrawableRes

data class CropAnalysis(
    val cropName: String,
    val confidence: Float,
    val isHealthy: Boolean,
    val diseases: List<Disease>,
    val lifecycle: CropLifecycle,
    val requirements: CropRequirements,
    val additionalInfo: AdditionalInfo,
    val aiDetails: String? = null,
    val plantDetails: StructuredPlantDetails? = null, // Structured plant details from Gemini
    val plantInfoSections: PlantInfoSections? = null,
    val isGeneric: Boolean = false
)

data class Disease(
    val name: String,
    val severity: DiseaseSeverity,
    val description: String,
    val symptoms: List<String>,
    val remedies: List<String>
)

enum class DiseaseSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class CropLifecycle(
    val stages: List<LifecycleStage>,
    val totalDays: Int
)

data class LifecycleStage(
    val name: String,
    val duration: Int, // in days
    val description: String,
    @DrawableRes val iconRes: Int? = null
)

data class CropRequirements(
    val sunlight: String,
    val water: String,
    val soilType: String,
    val temperature: String,
    val phLevel: String
)

data class AdditionalInfo(
    val fertilizers: List<String>,
    val idealClimate: String,
    val harvestingTips: List<String>,
    val pestControl: List<String>
) 