# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jacobrein/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Koin
-keep class org.koin.** { *; }

# Ktor
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, Enums
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Compose
-keep class androidx.compose.runtime.** { *; }

# Firebase (if used and not already covered by its own rules)
-keep class com.google.firebase.** { *; }

# Ksoup
-keep class com.fleeksoft.ksoup.** { *; }

# Kamel
-keep class io.kamel.** { *; }

# Datastore
-keep class androidx.datastore.** { *; }

# Keep generic signatures and line numbers for better crash reports
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean