# Farm Crop Analyzer

A comprehensive Android application built with Kotlin and Jetpack Compose that helps farmers analyze their crops through image recognition and provides detailed information about crop health, lifecycle, and care requirements.

## Features

### 🌿 Crop Identification
- Upload or take photos of crops
- AI-powered crop recognition using Plant.id API
- Confidence scoring for identification accuracy
- Scientific and common name identification

### 🔍 Disease Detection
- Automatic detection of common crop diseases
- Severity assessment (Low, Medium, High, Critical)
- Detailed symptoms and descriptions
- Recommended remedies and treatments

### 📈 Growth Lifecycle
- Complete growth stages from seeding to harvesting
- Duration estimates for each stage
- Visual representation of crop development
- Detailed plant information from Trefle API

### 💧 Care Requirements
- Sunlight requirements
- Watering guidelines
- Soil type recommendations
- Temperature and pH level preferences
- Climate zone information

### 💡 Additional Information
- Fertilizer recommendations
- Ideal climate conditions
- Harvesting tips and best practices
- Pest control strategies
- Plant family and genus information

## Architecture

The app follows Clean Architecture principles with the following structure:

```
app/src/main/java/com/example/farm/
├── data/
│   ├── model/           # Data models (Plant.id & Trefle)
│   ├── remote/          # API interfaces
│   └── repository/      # Data repositories
├── di/                  # Dependency injection modules
├── ui/
│   ├── component/       # Reusable UI components
│   ├── navigation/      # Navigation setup
│   ├── screen/          # UI screens
│   ├── theme/           # App theming
│   ├── util/            # UI utilities
│   └── viewmodel/       # ViewModels
└── FarmApplication.kt   # Application class
```

### Key Components

- **ViewModel**: Manages UI state and business logic
- **Repository**: Handles data operations and API calls
- **Hilt**: Dependency injection for clean architecture
- **Navigation**: Single Activity with Compose Navigation
- **Material 3**: Modern Material Design components

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Navigation**: Compose Navigation
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Async Programming**: Coroutines + Flow
- **APIs**: Plant.id (crop identification) + Trefle (plant details)

## API Integration

### Plant.id API
- **Purpose**: Crop identification from images
- **Features**: 
  - Image-based plant recognition
  - Confidence scoring
  - Disease detection
  - Health assessment
- **Authentication**: API key required

### Trefle API
- **Purpose**: Detailed plant information
- **Features**:
  - Comprehensive plant details
  - Growth lifecycle information
  - Care requirements
  - Climate and soil preferences
- **Authentication**: Token-based (v1 API)

### Data Flow
1. User uploads crop image
2. Plant.id API identifies the crop and detects diseases
3. Trefle API provides detailed plant information
4. Data is combined and displayed to user

## Setup and Installation

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24+
- Kotlin 2.0.21+

### API Keys Setup

1. **Plant.id API Key**:
   - Sign up at [Plant.id](https://web.plant.id/)
   - Get your API key from the dashboard
   - Add to `ApiConfig.kt`:
   ```kotlin
   const val PLANT_ID_API_KEY = "your_plant_id_api_key_here"
   ```

2. **Trefle API Token**:
   - Sign up at [Trefle](https://trefle.io/)
   - Get your access token
   - Add to `ApiConfig.kt`:
   ```kotlin
   const val TREFLE_API_TOKEN = "your_trefle_token_here"
   ```

### Building the Project

1. Clone the repository:
```bash
git clone <repository-url>
cd Farm
```

2. Add your API keys to `app/src/main/java/com/example/farm/data/remote/ApiConfig.kt`

3. Open the project in Android Studio

4. Sync Gradle files and build the project

5. Run the app on an emulator or physical device

### Permissions

The app requires the following permissions:
- Camera: For taking photos of crops
- Storage: For accessing gallery images
- Internet: For API communication with Plant.id and Trefle

## Current Implementation

### Real API Integration
The app now uses real APIs for comprehensive crop analysis:

#### Plant.id Integration
- Image-based crop identification
- Disease detection with severity levels
- Health assessment and confidence scoring
- Scientific and common name identification

#### Trefle Integration
- Detailed plant information
- Growth lifecycle stages
- Care requirements (sunlight, water, soil)
- Climate preferences and hardiness zones
- Plant family and taxonomy information

#### Data Combination
- Repository combines data from both APIs
- Intelligent plant matching between APIs
- Fallback mechanisms for missing data
- Loading states and error handling

### Error Handling
- Network error handling with user-friendly messages
- API rate limiting considerations
- Graceful degradation when APIs are unavailable
- Toast notifications for user feedback

## Future Enhancements

### Planned Features
- [ ] Offline crop database caching
- [ ] Historical analysis tracking
- [ ] Weather integration for care recommendations
- [ ] Community features and sharing
- [ ] Multi-language support
- [ ] Advanced filtering and search
- [ ] Export analysis reports

### Performance Optimizations
- [ ] Image compression before upload
- [ ] Response caching
- [ ] Background processing for large images
- [ ] Progressive loading of detailed information

## Screenshots

The app includes two main screens:

1. **Home Screen**: Welcome interface with upload functionality
2. **Analysis Screen**: Detailed crop analysis results with:
   - Crop identification and confidence
   - Health status and disease detection
   - Growth lifecycle with stages
   - Care requirements and recommendations
   - Climate and soil preferences
   - Additional tips and recommendations

## Troubleshooting

### Common Issues

1. **API Key Errors**: Ensure both Plant.id and Trefle API keys are correctly set in `ApiConfig.kt`

2. **Network Errors**: Check internet connection and API service status

3. **Image Upload Issues**: Verify camera and storage permissions are granted

4. **Build Errors**: Ensure all dependencies are synced and Gradle cache is cleared

### Debug Information
- Check logcat for API response details
- Verify API endpoints in `NetworkModule.kt`
- Monitor network requests in Android Studio's Network Inspector

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request
