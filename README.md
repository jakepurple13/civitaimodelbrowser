# CivitAI Model Browser

A Kotlin Multiplatform application built with Compose Multiplatform to browse and interact with
models from [CivitAI](https://civitai.com/).

## 🚀 Features

- **Multiplatform Support**: Full support for Android, iOS, and Desktop (JVM).
- **Adaptive UI**: Built with Material 3 Adaptive API, providing optimized layouts for different
  screen sizes (List/Detail and Supporting Pane strategies).
- **Model Browsing**: View the latest, most downloaded, and top-rated models from CivitAI.
- **Search**: Advanced search for models by name, tags, or creators.
- **Model Details**: Comprehensive model information including versions, images, and descriptions.
- **Favorites**: Save both models and individual images for quick access.
- **Custom Lists**: Create and manage personalized collections of models.
- **Advanced NSFW Handling**:
  - Toggle NSFW content globally.
  - Customizable blur strength for NSFW images.
  - Blur support powered by Haze.
- **Dynamic & Glassmorphism UI**:
  - Material 3 dynamic color support.
  - Advanced glassmorphism effects with customizable blur types (Haze, HazeProgressive) and levels.
  - Shared element transitions for a fluid navigation experience.
- **Backup & Restore**: Securely export and import your favorites, blacklist, custom lists,
  settings, and search history.
- **Blacklist**: Filter out specific models or creators from your feed.
- **Sharing**: Easily share models via generated QR codes or direct URLs.
- **Stats**: Detailed insights into your usage, including favorite counts, blacklist size, and
  search history.

## 🛠 Technologies Used

- **[Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)**: Shared business
  logic across platforms.
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)**: Modern,
  declarative UI for Android, iOS, and Desktop.
- **[Navigation3](https://github.com/android/architecture-samples/tree/main/Navigation3)**:
  Cutting-edge navigation library for Compose Multiplatform.
- **[Ktor](https://ktor.io/)**: Powerful asynchronous HTTP client for API communication.
- **[Koin](https://insert-koin.io/)**: Pragmatic dependency injection.
- **[Room (KMP)](https://developer.android.com/training/data-storage/room)**: Robust local database
  for favorites and blacklist with multiplatform support.
- **[Kamel](https://github.com/Kamel-Media/Kamel)**: Efficient asynchronous image loading.
- **[Haze](https://github.com/chrisbanes/haze)**: Beautiful glassmorphism and blur effects.
- **[BuildKonfig](https://github.com/yshrsmz/BuildKonfig)**: Compile-time project configuration and
  API key management.
- **[DataStore](https://developer.android.com/topic/libraries/architecture-datastore)**: Persistent
  key-value storage for application settings.
- **[Sonner](https://github.com/dokar3/sonner)**: Elegant toast notifications.
- **[AboutLibraries](https://github.com/mikepenz/AboutLibraries)**: Automatic license management and
  attribution.
- **[FileKit](https://github.com/vinceglb/FileKit)**: Multiplatform file picker and management.
- **[QRose](https://github.com/alexzhirkevich/qrose)**: QR code generation for Compose
  Multiplatform.
- **[MaterialKolor](https://github.com/jordond/MaterialKolor)**: Dynamic Material 3 color palettes
  from any color.
- **[Firebase](https://firebase.google.com/)**: Analytics, Crashlytics, and Performance monitoring (
  supported via GitLive SDK for KMP).

## ⚙️ Setup for Development

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
  or [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- JDK 17 or higher
- [Xcode](https://developer.apple.com/xcode/) (for iOS development)
- A CivitAI API Key

### Configuration (API Key)

This project requires a CivitAI API key to function correctly.

1. Go to your [CivitAI Settings](https://civitai.com/user/settings) and generate a new API Key.
2. In the root directory of the project, create or open the `local.properties` file.
3. Add your API key to the `local.properties` file:

```properties
api_key=YOUR_CIVITAI_API_KEY_HERE
```

The project uses BuildKonfig to pull this key into the shared code during compilation.

### Running the Application

#### Android

1. Open the project in Android Studio.
2. Select the `android` run configuration.
3. Click **Run**.

#### Desktop (JVM)

1. You can run the desktop application from the terminal:

```bash
./gradlew :desktop:run
```

2. Or use the `desktop` run configuration in your IDE.

#### iOS

1. Open the `iosApp/iosApp.xcworkspace` in Xcode.
2. Select a simulator or physical device.
3. Click **Run**.
4. Alternatively, if you are using Fleet or IntelliJ with the KMP plugin, you can run it directly
   from the IDE.

## 📄 License

This project is for educational/personal use. Please refer to CivitAI's terms of service when using
their API.
