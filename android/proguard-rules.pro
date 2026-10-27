# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jacobrein/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Kotlinx Serialization
-keepattributes *Annotation*, Enums
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Room
-keep class * extends androidx.room3.RoomDatabase
-keep class * extends androidx.room3.Entity
-keep class * extends androidx.room3.Dao

# Keep generic signatures and line numbers for better crash reports
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean