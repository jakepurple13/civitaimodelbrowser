# CivitAI Model Browser

A Kotlin Multiplatform application built with Compose Multiplatform to browse and interact with
models from [CivitAI](https://civitai.com/).

## 🚀 Features

- **Multiplatform Support**: Android, iOS, and Desktop (JVM) support.
- **Model Browsing**: View the latest, most downloaded, and top-rated models from CivitAI.
- **Search**: Find specific models by name or tags.
- **Model Details**: View detailed information about models, including versions, images, and
  descriptions.
- **Favorites**: Save your favorite models for quick access.
- **Blacklist**: Hide models or creators you're not interested in.
- **QR Code Sharing**: Share models easily via QR codes.
- **NSFW Content Handling**: Option to toggle NSFW content with blur support.
- **Dynamic Theming**: Beautiful Material 3 UI with dynamic color support (where applicable).

## 🛠 Technologies Used

- **[Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)**: Shared business
  logic across platforms.
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)**: Declarative UI
  for Android, iOS, and Desktop.
- **[Ktor](https://ktor.io/)**: Asynchronous HTTP client for API requests.
- **[Koin](https://insert-koin.io/)**: Dependency injection.
- **[Room](https://developer.android.com/training/data-storage/room)**: Local database for favorites
  and blacklist (using KMP Room).
- **[Kamel](https://github.com/Kamel-Media/Kamel)**: Asynchronous image loading.
- **[Haze](https://github.com/chrisbanes/haze)**: Glassmorphism and blur effects.
- **[BuildKonfig](https://github.com/yshrsmz/BuildKonfig)**: Multiplatform project configuration (
  for API keys).
- **[DataStore](https://developer.android.com/topic/libraries/architecture/datastore)**: Persistent
  key-value storage for settings.
- **[Sonner](https://github.com/dokar3/sonner)**: Toast notifications for Compose.

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
