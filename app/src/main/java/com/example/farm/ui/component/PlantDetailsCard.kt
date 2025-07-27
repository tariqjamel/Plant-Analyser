package com.example.farm.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farm.data.model.StructuredPlantDetails

@Composable
fun PlantDetailsCard(plantDetails: StructuredPlantDetails) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Plant Guide",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Plant Information
            if (plantDetails.scientificName.isNotEmpty()) {
                InfoSection(
                    title = "Scientific Information",
                    icon = Icons.Default.Science
                ) {
                    if (plantDetails.scientificName.isNotEmpty()) {
                        InfoRow("Scientific Name", plantDetails.scientificName)
                    }
                    if (plantDetails.family.isNotEmpty()) {
                        InfoRow("Family", plantDetails.family)
                    }
                    if (plantDetails.commonNames.isNotEmpty()) {
                        InfoRow("Common Names", plantDetails.commonNames.joinToString(", "))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Growth Cycle
            if (plantDetails.growthCycle.type.isNotEmpty()) {
                InfoSection(
                    title = "Growth Cycle",
                    icon = Icons.Default.Timeline
                ) {
                    InfoRow("Type", plantDetails.growthCycle.type)
                    if (plantDetails.growthCycle.totalDays > 0) {
                        InfoRow("Total Duration", "${plantDetails.growthCycle.totalDays} days")
                    }
                    if (plantDetails.growthCycle.stages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Growth Stages:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        plantDetails.growthCycle.stages.forEach { stage ->
                            GrowthStageItem(stage)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Care Requirements
            if (plantDetails.careRequirements.sunlight.isNotEmpty() || 
                plantDetails.careRequirements.water.isNotEmpty()) {
                InfoSection(
                    title = "Care Requirements",
                    icon = Icons.Default.WaterDrop
                ) {
                    if (plantDetails.careRequirements.sunlight.isNotEmpty()) {
                        InfoRow("☀️ Sunlight", plantDetails.careRequirements.sunlight)
                    }
                    if (plantDetails.careRequirements.water.isNotEmpty()) {
                        InfoRow("💧 Water", plantDetails.careRequirements.water)
                    }
                    if (plantDetails.careRequirements.soilType.isNotEmpty()) {
                        InfoRow("🌱 Soil Type", plantDetails.careRequirements.soilType)
                    }
                    if (plantDetails.careRequirements.temperature.isNotEmpty()) {
                        InfoRow("🌡️ Temperature", plantDetails.careRequirements.temperature)
                    }
                    if (plantDetails.careRequirements.phLevel.isNotEmpty()) {
                        InfoRow("🧪 pH Level", plantDetails.careRequirements.phLevel)
                    }
                    if (plantDetails.careRequirements.humidity.isNotEmpty()) {
                        InfoRow("💨 Humidity", plantDetails.careRequirements.humidity)
                    }
                    if (plantDetails.careRequirements.spacing.isNotEmpty()) {
                        InfoRow("📏 Spacing", plantDetails.careRequirements.spacing)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Growing Tips
            if (plantDetails.growingTips.isNotEmpty()) {
                InfoSection(
                    title = "Growing Tips",
                    icon = Icons.Default.Lightbulb
                ) {
                    plantDetails.growingTips.forEach { tip ->
                        TipItem(tip)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Interesting Facts
            if (plantDetails.interestingFacts.isNotEmpty()) {
                InfoSection(
                    title = "Interesting Facts",
                    icon = Icons.Default.Star
                ) {
                    plantDetails.interestingFacts.forEach { fact ->
                        FactItem(fact)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Description
            if (plantDetails.description.isNotEmpty()) {
                InfoSection(
                    title = "Description",
                    icon = Icons.Default.Info
                ) {
                    Text(
                        text = plantDetails.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GrowthStageItem(stage: com.example.farm.data.model.GrowthStage) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stage.duration} days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (stage.description.isNotEmpty()) {
                    Text(
                        text = stage.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TipItem(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tip,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun FactItem(fact: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = fact,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
    }
} 