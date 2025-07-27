package com.example.farm.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farm.data.model.PlantInfoSections

@Composable
fun PlantInfoSectionsCard(plantInfoSections: PlantInfoSections) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (plantInfoSections.summary.isNotEmpty()) {
            InfoCard(
                title = "Summary",
                content = plantInfoSections.summary,
                icon = Icons.Default.Info,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
        
        if (plantInfoSections.lifecycle.isNotEmpty()) {
            InfoCard(
                title = "Lifecycle",
                content = plantInfoSections.lifecycle,
                icon = Icons.Default.Timeline,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        
        if (plantInfoSections.careRequirements.isNotEmpty()) {
            InfoCard(
                title = "Care Requirements",
                content = plantInfoSections.careRequirements,
                icon = Icons.Default.WaterDrop,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        
        if (plantInfoSections.idealClimate.isNotEmpty()) {
            InfoCard(
                title = "Ideal Climate",
                content = plantInfoSections.idealClimate,
                icon = Icons.Default.WbSunny,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        if (plantInfoSections.interestingFacts.isNotEmpty()) {
            InfoCard(
                title = "Interesting Facts",
                content = plantInfoSections.interestingFacts,
                icon = Icons.Default.Star,
                containerColor = MaterialTheme.colorScheme.inversePrimary
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
} 