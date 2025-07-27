package com.example.farm.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.farm.ui.viewmodel.CropAnalysisViewModel
import com.example.farm.ui.viewmodel.CropAnalysisUiState
import com.example.farm.ui.component.ImagePickerDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAnalysis: () -> Unit,
    navController: NavHostController,
    parentEntry: NavBackStackEntry
) {
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    // Use the shared ViewModel scoped to the Home route
    val viewModel: CropAnalysisViewModel = hiltViewModel(parentEntry)
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.analyzeCropImage(it)
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                viewModel.analyzeCropImage(uri)
            }
        }
    }
    
    // Function to create image file
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Pictures")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Debug logging for state changes and toast messages
    LaunchedEffect(uiState) {
        println("HomeScreen: UI State changed to: $uiState")
        when (val state = uiState) {
            is CropAnalysisUiState.Success -> {
                println("HomeScreen: Navigating to analysis screen with data: ${state.analysis.cropName}")
                Toast.makeText(context, "Analysis completed! Found: ${state.analysis.cropName}", Toast.LENGTH_SHORT).show()
                onNavigateToAnalysis()
            }
            is CropAnalysisUiState.Error -> {
                println("HomeScreen: Error state: ${state.message}")
                Toast.makeText(context, "Analysis failed: ${state.message}", Toast.LENGTH_LONG).show()
            }
            is CropAnalysisUiState.Loading -> {
                println("HomeScreen: Loading state")
                Toast.makeText(context, "Analyzing your crop image...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                println("HomeScreen: Initial state")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Crop Analyzer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon/Logo placeholder
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌱",
                        fontSize = 48.sp
                    )
                }
            }

            Text(
                text = "Welcome to Farm Crop Analyzer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Upload or take a photo of your crop to get detailed analysis",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            Button(
                onClick = { showImagePickerDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Upload Crop Photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "What you'll get:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    FeatureItem("🌿 Crop identification")
                    FeatureItem("🔍 Disease detection")
                    FeatureItem("📈 Growth lifecycle")
                    FeatureItem("💧 Care requirements")
                    FeatureItem("💡 Growing tips")
                }
            }
        }
    }
    
    if (uiState is CropAnalysisUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing your crop...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please wait while we identify your plant",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onCameraClick = {
                showImagePickerDialog = false
                try {
                    val photoFile = createImageFile()
                    currentPhotoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraLauncher.launch(currentPhotoUri)
                } catch (e: Exception) {
                    println("Error creating image file: ${e.message}")
                }
            },
            onGalleryClick = {
                showImagePickerDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 