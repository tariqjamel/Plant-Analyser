# 🌱 Farm - AI-Powered Crop Analysis App

A modern Android application that uses AI to analyze crop plants and provide comprehensive care information. The app combines multiple AI services to deliver detailed, structured plant information with a beautiful Material Design 3 interface.

## ✨ Features

### 🔍 **Smart Plant Identification**
- **Plant.id API Integration**: Advanced plant recognition using computer vision
- **High Accuracy**: Provides confidence scores for plant identification
- **Multiple Plant Support**: Identifies various crop types and plants

### 🤖 **AI-Powered Plant Analysis**
- **Gemini API Integration**: Comprehensive plant information using Google's Gemini AI
- **Structured Data**: Parses AI responses into organized, searchable information
- **Fallback Support**: Wikipedia integration when AI services are unavailable

### 📱 **Beautiful Modern UI**
- **Material Design 3**: Latest Android design guidelines
- **Responsive Layout**: Optimized for different screen sizes
- **Color-Coded Information**: Visual hierarchy for easy information scanning
- **Interactive Elements**: Smooth animations and transitions

### 🌿 **Comprehensive Plant Information**

#### **Scientific Information**
- Scientific name and classification
- Plant family and taxonomy
- Common names and synonyms

#### **Growth Cycle**
- Plant type (Annual, Perennial, Biennial)
- Total growth duration
- Detailed growth stages with timelines
- Stage-specific care requirements

#### **Care Requirements**
- **Sunlight**: Specific light requirements
- **Water**: Detailed watering instructions
- **Soil**: Soil type and pH preferences
- **Temperature**: Optimal temperature ranges
- **Humidity**: Humidity requirements
- **Spacing**: Plant spacing guidelines

#### **Growing Tips & Facts**
- Practical growing advice
- Interesting plant facts
- Care best practices
- Troubleshooting tips

### 🏥 **Health Assessment**
- Plant health status detection
- Disease identification and severity
- Treatment recommendations
- Preventive care tips

## 🛠 Technical Architecture

### **Frontend**
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: Latest design system
- **Navigation Compose**: Type-safe navigation
- **ViewModel**: State management and lifecycle awareness

### **Backend Services**
- **Plant.id API**: Plant identification service
- **Google Gemini AI**: Natural language processing for plant information
- **Wikipedia API**: Fallback information source
- **Retrofit**: HTTP client for API communication

### **Data Management**
- **Hilt**: Dependency injection
- **Repository Pattern**: Clean architecture
- **Structured Data Models**: Type-safe data handling
- **Error Handling**: Graceful fallbacks and user feedback

## 📋 Requirements

### **System Requirements**
- Android 7.0 (API level 24) or higher
- Internet connection for AI services
- Camera access for plant photos

### **Development Requirements**
- Android Studio Hedgehog or later
- Java 17 or higher
- Kotlin 1.9.0 or higher
- Android Gradle Plugin 8.0 or higher

## 🚀 Installation

### **For Users**
1. Download the APK file
2. Enable "Install from Unknown Sources" in Android settings
3. Install the APK
4. Grant camera and internet permissions
5. Start analyzing plants!

### **For Developers**
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Add your API keys to `ApiConfig.kt`:
   ```kotlin
   const val PLANT_ID_API_KEY = "your_plant_id_api_key"
   const val GEMINI_API_KEY = "your_gemini_api_key"
   ```
5. Build and run the project

## 🔧 API Configuration

### **Required API Keys**

#### **Plant.id API**
- Sign up at [plant.id](https://plant.id/)
- Get your API key from the dashboard
- Add to `ApiConfig.kt`

#### **Google Gemini AI**
- Get API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
- Add to the repository code

### **Optional APIs**
- **Wikipedia API**: No key required (fallback service)
- **OpenAI API**: For additional processing (optional)

## 📱 Usage Guide

### **Taking Plant Photos**
1. Open the app
2. Tap the camera button
3. Take a clear photo of the plant
4. Ensure good lighting and focus
5. Submit for analysis

### **Understanding Results**
- **Confidence Score**: How certain the AI is about the identification
- **Health Status**: Overall plant health assessment
- **Care Requirements**: Detailed care instructions
- **Growth Information**: Lifecycle and growth stages
- **Tips & Facts**: Additional helpful information

### **Best Practices**
- Take photos in good lighting
- Include leaves, flowers, or fruits for better identification
- Ensure the plant is clearly visible and in focus
- Avoid shadows or obstructions

## 🎨 UI Components

### **PlantDetailsCard**
- **Purpose**: Displays structured AI-generated plant information
- **Features**: 
  - Scientific information section
  - Growth cycle with stages
  - Care requirements grid
  - Growing tips and facts
  - Beautiful Material Design 3 styling

### **Enhanced Analysis Screen**
- **Crop Header**: Plant name, confidence, scientific name
- **Health Status**: Visual health indicators
- **Disease Cards**: Detailed disease information with remedies
- **Lifecycle Cards**: Growth stages with timelines
- **Requirements Cards**: Care requirements with icons
- **Additional Info**: Fertilizers, climate, harvesting tips

## 🔄 Data Flow

1. **Image Capture**: User takes plant photo
2. **Plant Identification**: Plant.id API identifies the plant
3. **AI Analysis**: Gemini API provides comprehensive information
4. **Data Parsing**: Structured data extraction and organization
5. **UI Display**: Beautiful, organized information presentation
6. **Fallback**: Wikipedia data if AI services fail

## 🐛 Troubleshooting

### **Build Issues**
- Ensure Java 17 is installed and configured
- Update Android Studio to latest version
- Clean and rebuild project
- Check API key configuration

### **Runtime Issues**
- Verify internet connection
- Check API key validity
- Ensure camera permissions are granted
- Clear app data if needed

### **API Issues**
- Monitor API usage limits
- Check API service status
- Verify API key permissions
- Implement proper error handling

## 📈 Future Enhancements

### **Planned Features**
- **Offline Mode**: Cache plant information for offline access
- **Plant Database**: Local database of common plants
- **Care Reminders**: Notification system for plant care
- **Community Features**: User plant sharing and tips
- **AR Integration**: Augmented reality plant identification
- **Weather Integration**: Local weather-based care recommendations

### **Technical Improvements**
- **Performance Optimization**: Faster image processing
- **Better Error Handling**: More user-friendly error messages
- **Accessibility**: Screen reader support and accessibility features
- **Internationalization**: Multi-language support
- **Analytics**: Usage tracking and insights

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Plant.id**: For plant identification API
- **Google Gemini AI**: For natural language processing
- **Wikipedia**: For fallback plant information
- **Material Design**: For design guidelines and components
- **Jetpack Compose**: For modern Android UI development

## 📞 Support

For support, questions, or feature requests:
- Create an issue on GitHub
- Contact the development team
- Check the troubleshooting section

---

**Made with ❤️ for plant lovers and farmers everywhere!** 