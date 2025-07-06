# Farm Crop Analyzer

A comprehensive Android application built with Kotlin and Jetpack Compose that helps farmers analyze their crops through image recognition and provides detailed information about crop health, lifecycle, and care requirements.

## Features

### 🌿 Crop Identification
- Upload or take photos of crops
- AI-powered crop recognition
- Confidence scoring for identification accuracy

### 🔍 Disease Detection
- Automatic detection of common crop diseases
- Severity assessment (Low, Medium, High, Critical)
- Detailed symptoms and descriptions
- Recommended remedies and treatments

### 📈 Growth Lifecycle
- Complete growth stages from seeding to harvesting
- Duration estimates for each stage
- Visual representation of crop development

### 💧 Care Requirements
- Sunlight requirements
- Watering guidelines
- Soil type recommendations
- Temperature and pH level preferences

### 💡 Additional Information
- Fertilizer recommendations
- Ideal climate conditions
- Harvesting tips and best practices
- Pest control strategies

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

## Setup and Installation

### Building the Project

1. Clone the repository:
```bash
git clone <repository-url>
cd Farm
```

2. Open the project in Android Studio

3. Sync Gradle files and build the project

4. Run the app on an emulator or physical device

### Permissions

The app requires the following permissions:
- Camera: For taking photos of crops
- Storage: For accessing gallery images
- Internet: For API communication (future implementation)
  
## Future Enhancements

### Planned Features
- [ ] Real ML model integration
- [ ] Offline crop database
- [ ] Historical analysis tracking
- [ ] Weather integration
- [ ] Community features
- [ ] Multi-language support

## Screenshots

The app includes two main screens:

1. **Home Screen**: Welcome interface with upload functionality
2. **Analysis Screen**: Detailed crop analysis results with:
   - Crop identification and confidence
   - Health status
   - Disease information
   - Growth lifecycle
   - Care requirements
   - Additional tips and recommendations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request
