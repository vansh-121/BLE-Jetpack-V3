# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keep class com.google.** { *; }
# Gemini API
# Preserve generic signatures and reflection metadata
#-keepattributes Signature
#-keepattributes *Annotation*
#-keepattributes Exceptions
#
## Gemini API SDK
#-keep class com.google.ai.client.** { *; }
#-dontwarn com.google.ai.client.**
#
## Coroutines
#-keep class kotlinx.coroutines.** { *; }
#-dontwarn kotlinx.coroutines.**
#
## Your app’s models (adjust package name)
#-keep class com.example.ble_jetpackcompose.models.** { *; }
#
## Reflection and proxies
#-keep class java.lang.reflect.** { *; }
#-dontwarn java.lang.reflect.**
#
## Gson (if used)
#-keep class com.google.gson.** { *; }
#-dontwarn com.google.gson.**

# Preserve generic signatures and reflection metadata
# Preserve generic signatures and reflection metadata
# Preserve generic signatures and reflection metadata
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses

# Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.http.** { *; }
-keep class retrofit2.converter.** { *; }

# OkHttp (used by Retrofit)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson (used by GsonConverterFactory)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Your API interface and models
-keep class com.example.ble_jetpackcompose.GeminiTranslationApi { *; }
-keep class com.example.ble_jetpackcompose.TranslationRequest { *; }
-keep class com.example.ble_jetpackcompose.TranslationResponse { *; }
-keep class com.example.ble_jetpackcompose.Content { *; }
-keep class com.example.ble_jetpackcompose.Part { *; }
-keep class com.example.ble_jetpackcompose.GenerationConfig { *; }
-keep class com.example.ble_jetpackcompose.TranslationCandidate { *; }

# Preserve suspend function return types and annotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Coroutines (used in suspend functions)
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**