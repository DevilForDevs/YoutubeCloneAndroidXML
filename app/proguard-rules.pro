# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepattributes JavascriptInterface
-keepattributes *Annotation*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.ranjan.expertclient.screens.browserscreen.WebAppInterface {
    public *;
}

# Keep the standard WebView classes
-keep class android.webkit.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# Jsoup rules
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# Glide rules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep class com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
-keepnames class com.bumptech.glide.features.glide.GlideApp
-dontwarn com.bumptech.glide.integration.okhttp3.**
