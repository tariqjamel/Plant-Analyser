package com.example.farm.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.farm.data.model.*
import com.example.farm.ui.viewmodel.CropAnalysisUiState
import com.example.farm.ui.viewmodel.CropAnalysisViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    parentEntry: NavBackStackEntry
) {
    // Use the shared ViewModel scoped to the Home route
    val viewModel: CropAnalysisViewModel = hiltViewModel(parentEntry)
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        viewModel.resetState()
        onNavigateBack()
    }
    
    // Debug logging for state changes
    LaunchedEffect(uiState) {
        println("AnalysisScreen: UI State changed to: $uiState")
        when (val state = uiState) {
            is CropAnalysisUiState.Success -> {
                println("AnalysisScreen: Received analysis data: ${state.analysis.cropName}")
            }
            is CropAnalysisUiState.Error -> {
                println("AnalysisScreen: Error state: ${state.message}")
            }
            is CropAnalysisUiState.Loading -> {
                println("AnalysisScreen: Loading state")
            }
            else -> {
                println("AnalysisScreen: Initial state - waiting for data")
                // Don't navigate back automatically, let the user use the back button
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Analysis") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        when (val currentState = uiState) {
            is CropAnalysisUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing your crop...")
                    }
                }
            }
            
            is CropAnalysisUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CropHeaderCard(currentState.analysis)
                    }
                    
                    item {
                        HealthStatusCard(currentState.analysis)
                    }
                    
                    if (currentState.analysis.diseases.isNotEmpty()) {
                        item {
                            DiseasesCard(currentState.analysis.diseases)
                        }
                    }
                    
                    item {
                        LifecycleCard(currentState.analysis.lifecycle)
                    }
                    
                    item {
                        RequirementsCard(currentState.analysis.requirements)
                    }
                    
                    item {
                        AdditionalInfoCard(currentState.analysis.additionalInfo)
                    }
                }
            }
            
            is CropAnalysisUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "❌ Analysis Failed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentState.message,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = onNavigateBack) {
                                Text("Go Back")
                            }
                            Button(
                                onClick = {
                                    viewModel.retryAnalysis()
                                }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
            
            else -> {
                // Initial state - show loading or empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading analysis data...")
                    }
                }
            }
        }
    }
}

@Composable
private fun CropHeaderCard(analysis: CropAnalysis) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = analysis.cropName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Confidence: ${(analysis.confidence * 100).toInt()}%",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthStatusCard(analysis: CropAnalysis) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (analysis.isHealthy) "✅ Healthy" else "⚠️ Issues Detected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (analysis.isHealthy) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DiseasesCard(diseases: List<Disease>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Detected Diseases",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            diseases.forEach { disease ->
                DiseaseItem(disease)
                if (disease != diseases.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DiseaseItem(disease: Disease) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = disease.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            SeverityChip(disease.severity)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = disease.description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Symptoms:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        disease.symptoms.forEach { symptom ->
            Text(
                text = "• $symptom",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Remedies:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        disease.remedies.forEach { remedy ->
            Text(
                text = "• $remedy",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeverityChip(severity: DiseaseSeverity) {
    val (color, text) = when (severity) {
        DiseaseSeverity.LOW -> MaterialTheme.colorScheme.primary to "Low"
        DiseaseSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary to "Medium"
        DiseaseSeverity.HIGH -> MaterialTheme.colorScheme.error to "High"
        DiseaseSeverity.CRITICAL -> MaterialTheme.colorScheme.error to "Critical"
    }
    
    SuggestionChip(
        onClick = { },
        label = { Text(text) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        )
    )
}

@Composable
private fun LifecycleCard(lifecycle: CropLifecycle) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Growth Lifecycle (${lifecycle.totalDays} days total)",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            lifecycle.stages.forEach { stage ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stage.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stage.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${stage.duration} days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementsCard(requirements: CropRequirements) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Care Requirements",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            RequirementItem("☀️ Sunlight", requirements.sunlight)
            RequirementItem("💧 Water", requirements.water)
            RequirementItem("🌱 Soil Type", requirements.soilType)
            RequirementItem("🌡️ Temperature", requirements.temperature)
            RequirementItem("🧪 pH Level", requirements.phLevel)
        }
    }
}

@Composable
private fun RequirementItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdditionalInfoCard(info: AdditionalInfo) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            InfoSection("Fertilizers", info.fertilizers)
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection("Ideal Climate", listOf(info.idealClimate))
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection("Harvesting Tips", info.harvestingTips)
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection("Pest Control", info.pestControl)
        }
    }
}

@Composable
private fun InfoSection(title: String, items: List<String>) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    items.forEach { item ->
        Text(
            text = "• $item",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 