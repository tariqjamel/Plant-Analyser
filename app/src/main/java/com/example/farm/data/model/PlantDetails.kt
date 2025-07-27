package com.example.farm.data.model

data class StructuredPlantDetails(
    val commonNames: List<String> = emptyList(),
    val scientificName: String = "",
    val family: String = "",
    val growthCycle: GrowthCycle = GrowthCycle(),
    val careRequirements: CareRequirements = CareRequirements(),
    val growingTips: List<String> = emptyList(),
    val interestingFacts: List<String> = emptyList(),
    val description: String = ""
)

data class GrowthCycle(
    val type: String = "", // Annual, Perennial, Biennial
    val totalDays: Int = 0,
    val stages: List<GrowthStage> = emptyList()
)

data class GrowthStage(
    val name: String,
    val duration: Int, // in days
    val description: String
)

data class CareRequirements(
    val sunlight: String = "",
    val water: String = "",
    val soilType: String = "",
    val temperature: String = "",
    val phLevel: String = "",
    val humidity: String = "",
    val spacing: String = ""
) 