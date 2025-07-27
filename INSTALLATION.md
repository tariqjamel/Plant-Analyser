# 📱 Installation Guide

## Quick Start

### For End Users

1. **Download the APK**
   - Download the latest APK file from the releases
   - Or build from source (see developer instructions below)

2. **Enable Unknown Sources**
   - Go to Settings > Security
   - Enable "Install from Unknown Sources"
   - Or enable it for your file manager app

3. **Install the App**
   - Open the APK file
   - Follow the installation prompts
   - Grant necessary permissions when prompted

4. **First Launch**
   - Open the Farm app
   - Grant camera and internet permissions
   - You're ready to analyze plants!

### For Developers

1. **Prerequisites**
   ```bash
   # Ensure you have Java 17 installed
   java -version
   # Should show Java 17 or higher
   ```

2. **Clone and Setup**
   ```bash
   git clone <repository-url>
   cd Farm
   ```

3. **Add API Keys**
   - Open `app/src/main/java/com/example/farm/data/remote/ApiConfig.kt`
   - Replace the placeholder API keys with your own:
   ```kotlin
   const val PLANT_ID_API_KEY = "your_actual_plant_id_api_key"
   const val GEMINI_API_KEY = "your_actual_gemini_api_key"
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   # Or use Android Studio to build and run
   ```

## API Key Setup

### Plant.id API
1. Visit [plant.id](https://plant.id/)
2. Sign up for a free account
3. Get your API key from the dashboard
4. Add it to `ApiConfig.kt`

### Google Gemini AI
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Add it to the repository code

## Troubleshooting

### Build Issues
- **Java Version**: Ensure Java 17 is installed and configured
- **Gradle Sync**: Try "File > Invalidate Caches and Restart" in Android Studio
- **Clean Build**: Run `./gradlew clean` before building

### Runtime Issues
- **Permissions**: Ensure camera and internet permissions are granted
- **API Keys**: Verify your API keys are correctly set
- **Network**: Check your internet connection

### Common Errors
- **"API key invalid"**: Check your API keys in `ApiConfig.kt`
- **"Network error"**: Verify internet connection and API service status
- **"Camera not working"**: Grant camera permissions in app settings

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Look at the error logs in Android Studio
3. Create an issue on GitHub with details
4. Contact the development team

---

**Happy plant analyzing! 🌱** 